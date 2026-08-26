package app.quranhub.ui.mushaf.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.entity.Aya
import app.quranhub.data.local.entity.AyaBookmark
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.data.local.entity.Note
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.data.model.ReciterModel
import app.quranhub.databinding.FragmentQuranPageBinding
import app.quranhub.ui.downloads_manager.dialogs.AudioDownloadAmountDialogFragment
import app.quranhub.ui.downloads_manager.dialogs.AudioDownloadAmountDialogFragment.AudioDownloadListener
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment.ReciterSelectionListener
import app.quranhub.ui.mushaf.dialogs.AddBookmarkDialog
import app.quranhub.ui.mushaf.dialogs.AddBookmarkDialog.AddBookmarkListener
import app.quranhub.ui.mushaf.dialogs.AddNoteDialog
import app.quranhub.ui.mushaf.dialogs.AddNoteDialog.AddNoteListener
import app.quranhub.ui.mushaf.dialogs.AyaActionsDialog
import app.quranhub.ui.mushaf.dialogs.AyaActionsDialog.AyaPropertiesListener
import app.quranhub.ui.mushaf.model.BookmarkModel
import app.quranhub.ui.mushaf.presenter.QuranPagePresenter
import app.quranhub.ui.mushaf.presenter.QuranPagePresenterImp
import app.quranhub.ui.mushaf.view.QuranPageView
import app.quranhub.util.FragmentUtils.isSafeFragment
import app.quranhub.util.ImageUtil
import app.quranhub.util.IntentUtils
import app.quranhub.util.ScreenUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlin.math.abs

class QuranPageFragment : Fragment(), AyaPropertiesListener, AddNoteListener, QuranPageView,
    AddBookmarkListener, ReciterSelectionListener, AudioDownloadListener {

    private var _binding: FragmentQuranPageBinding? = null
    private val binding get() = _binding!!

    private var mushafFragment: MushafFragment? = null

    private var quranImageUrl: String? = null

    private var quranPageNum = 0

    private var initSelectedAyaId = 0

    private var nightMode = false

    private var zoomScaleFactor = 1f

    var numOfAyaInPage = 0
        private set

    private var isPageShown = false

    private var noteDialogOpen = false

    private var quranImageContainerHeight = 0

    private var quranImageContainerWidth = 0

    private var ayaActionsArgs: Bundle? = null

    private var ayaActionsDialog: AyaActionsDialog? = null

    private var bookmarkDialog: AddBookmarkDialog? = null

    private var longClickXLocation = 0

    private var longClickYLocation = 0

    var currentAya: Aya? = null
        private set

    private var previousAya: Aya? = null

    var currentAyaIndex = 0
        private set

    private var imageScaleFactor = 0.0

    private var pageAyasList: MutableList<Aya>? = null

    private var ayaShadowsViews: MutableList<View>? = null

    private var presenter: QuranPagePresenter<QuranPageView>? = null

    private var recitationId = 0

    private var margin = 0
    private var lineHeight = 0.0
    private var top = 0.0
    private var start = 0.0
    private var end = 0.0

    private var isAyaBookmark = false

    private var playFirstAyaAudio = false

    private var playMiddleAyaAudio = false

    var isAyaAudioDownloaded = false

    private var selectedAyaNote: Note? = null

    private var isVisibleToUser = false

    private var drawShadowFromNotification = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            quranImageUrl = requireArguments().getString(ARG_QURAN_IMAGE_URL)
            quranPageNum = requireArguments().getInt(ARG_QURAN_PAGE_NUM)
            initSelectedAyaId = requireArguments().getInt(ARG_INIT_SELECTED_AYA_ID, -1)
            nightMode = requireArguments().getBoolean(ARG_NIGHT_MODE, false)
            zoomScaleFactor = requireArguments().getFloat(ARG_ZOOM_SCALE_FACTOR, 1f)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuranPageBinding.inflate(inflater, container, false)
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (nightMode) {
            binding.root.setBackgroundColor(Color.BLACK)
        }
        setZoomScale()
        setParentFragment()
        recitationId = AppPreferencesManager.getRecitationSetting(requireContext())
        getCurrentPageAyas()

        binding.containerSv.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (_binding == null) return

                if (_binding?.containerSv?.viewTreeObserver != null) {
                    binding.containerSv.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }

                // get quran iv container width and lineHeight
                quranImageContainerWidth = binding.containerSv.width
                quranImageContainerHeight = binding.containerSv.height
                Log.d(
                    TAG,
                    "quran image container: $quranImageContainerWidth , $quranImageContainerHeight"
                )
                scaleQuranImage()
                calculateImageMetrics()
                ayaActionsArgs = Bundle()
                initOnLongClickQuranPage()
                if (savedInstanceState == null) {
                    initBookmarkDialog()
                } else {
                    getPrevSavedInstance(savedInstanceState)
                }
                showQuranPage()
            }
        })
        binding.pageIv.setOnClickListener { onQuranPageClick() }
    }

    private fun setZoomScale() {
        binding.quranPageContainer.scaleX = zoomScaleFactor
        binding.quranPageContainer.scaleY = zoomScaleFactor
    }

    private fun setParentFragment() {
        if (mushafFragment == null) {
            mushafFragment = parentFragment as? MushafFragment
        }
    }

    override fun drawInitAyaShadow(aya: Aya, previousAya: Aya?) {
        isAyaBookmark = false
        currentAya = aya
        this.previousAya = previousAya
        drawShadow()
    }

    private fun getPrevSavedInstance(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            numOfAyaInPage = savedInstanceState.getInt("num_of_ayas")
            currentAya = savedInstanceState.getParcelable("current_aya")
            previousAya = savedInstanceState.getParcelable("prev_aya")
            currentAyaIndex = savedInstanceState.getInt("CURRENT_AYA_INDEX")
            isAyaAudioDownloaded = savedInstanceState.getBoolean("AYA_AUDIO_DOWNLOADED")
            ayaActionsDialog =
                childFragmentManager.findFragmentByTag("AyaActionsDialog") as AyaActionsDialog?
            bookmarkDialog =
                childFragmentManager.findFragmentByTag("AddBookmarkDialog") as AddBookmarkDialog?
            if (currentAya != null) {
                presenter!!.getAyaBookmarkType(currentAya!!.id)
                presenter!!.checkAyaHasNote(currentAya!!.id)
                val currentAyaY: Int = when (recitationId) {
                    Constants.Recitation.HAFS_ID -> currentAya!!.y
                    Constants.Recitation.WARSH_ID -> currentAya!!.yw
                    else -> throw RuntimeException("Cannot identify recitation")
                }

                // scroll to selected aya after landscape orientation
                _binding?.containerSv?.post {
                    _binding?.containerSv?.scrollTo(0, (currentAyaY * imageScaleFactor).toInt())
                }
            }
            noteDialogOpen = savedInstanceState.getBoolean("open_dialog")
            if (noteDialogOpen) {
                noteDialogOpen = false
                selectedAyaNote = savedInstanceState.getParcelable("selected_note")
                openAddNoteDialog()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("current_aya", currentAya)
        outState.putParcelable("prev_aya", previousAya)
        outState.putParcelable("selected_note", selectedAyaNote)
        outState.putBoolean("open_dialog", noteDialogOpen)
        outState.putInt("CURRENT_AYA_INDEX", currentAyaIndex)
        outState.putInt("num_of_ayas", numOfAyaInPage)
        outState.putBoolean("AYA_AUDIO_DOWNLOADED", isAyaAudioDownloaded)
    }

    private fun calculateImageMetrics() {
        when (recitationId) {
            Constants.Recitation.HAFS_ID -> {
                lineHeight = (if (quranPageNum in 1..2) 50 else 70) * imageScaleFactor
                end = (if (quranPageNum in 1..2) 628 else 729) * imageScaleFactor
                start = (if (quranPageNum in 1..2) 218 else 79) * imageScaleFactor
                top = (if (quranPageNum in 1..2) 390 else 75) * imageScaleFactor
                margin = if (quranPageNum in 1..2) 5 else 10
            }

            Constants.Recitation.WARSH_ID -> {
                lineHeight = (if (quranPageNum in 1..2) 37 else 63) * imageScaleFactor
                end = (if (quranPageNum in 1..2) 454 else 580) * imageScaleFactor
                start =
                    (if (quranPageNum == 1) 170 else if (quranPageNum == 2) 148 else 41) * imageScaleFactor
                top = (if (quranPageNum in 1..2) 378 else 42) * imageScaleFactor
                margin = if (quranPageNum in 1..2) 8 else 5
            }

            else -> {
                error("Cannot identify recitation")
            }
        }
    }

    private fun getScaledY(y: Int, withHeight: Boolean) =
        (y - margin) * imageScaleFactor + if (withHeight) lineHeight else 0.0

    private fun getScaledX(x: Int) = x * imageScaleFactor

    private fun getCurrentPageAyas() {
        ayaShadowsViews = ArrayList()
        presenter = QuranPagePresenterImp(requireContext())
        presenter!!.onAttach(this)
        presenter!!.getPageAyas(quranPageNum)
    }

    private fun initBookmarkDialog() {
        bookmarkDialog = AddBookmarkDialog()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOnLongClickQuranPage() {
        if (_binding == null) return
        binding.pageIv.setOnTouchListener { _: View?, event: MotionEvent ->
            longClickXLocation = event.x.toInt()
            longClickYLocation = event.y.toInt()
            false
        }
        binding.pageIv.setOnLongClickListener {
            if (!isPageShown) return@setOnLongClickListener false
            isAyaBookmark = false
            isAyaAudioDownloaded = false
            selectedAyaNote = null
            selectedAya
            if (currentAya != null) {
                drawShadow()
                showActionsDialog()
            } else {
                removePrevAyaShadows()
            }
            true
        }
    }

    private fun showActionsDialog() {/* the dialog coordinates itself to the window origin,
           instead we want it to coordinate to the quran image origin */
        var yLocation =
            ScreenUtils.getStatusBarHeight(requireContext(), binding.pageIv) // base yLocation
        val y: Int = when (recitationId) {
            Constants.Recitation.HAFS_ID -> currentAya!!.y
            Constants.Recitation.WARSH_ID -> currentAya!!.yw
            else -> throw RuntimeException("Cannot identify recitation")
        }
        yLocation += (getScaledY(y, false) + 1.2 * lineHeight).toInt()
        ayaActionsDialog = AyaActionsDialog()
        ayaActionsArgs!!.putInt(AyaActionsDialog.ARG_Y_LOCATION, yLocation)
        ayaActionsDialog!!.arguments = ayaActionsArgs
        ayaActionsDialog!!.show(childFragmentManager, "AyaActionsDialog")
        presenter!!.getAyaBookmarkType(currentAya!!.id)
        presenter!!.checkAyaHasNote(currentAya!!.id)
    }

    private fun removePrevAyaShadows() {
        for (view in ayaShadowsViews!!) {
            binding.quranPageContainer.removeView(view)
        }
        ayaShadowsViews!!.clear()
    }

    // We have found the clicked Aya
    private val selectedAya: Unit
        get() {
            currentAya = null
            previousAya = null
            if (pageAyasList != null && pageAyasList!!.size > 0) {
                var ayaX: Double
                var ayaY: Double
                var prev: Aya? = null
                for (i in pageAyasList!!.indices) {
                    val aya = pageAyasList!![i]
                    var x: Int
                    var y: Int
                    when (recitationId) {
                        Constants.Recitation.HAFS_ID -> {
                            x = aya.x
                            y = aya.y
                        }

                        Constants.Recitation.WARSH_ID -> {
                            x = aya.xw
                            y = aya.yw
                        }

                        else -> throw RuntimeException("Cannot identify recitation")
                    }
                    ayaX = getScaledX(x)
                    ayaY = getScaledY(y, true)
                    if (longClickYLocation <= ayaY) {
                        if (ayaY - longClickYLocation <= lineHeight && longClickXLocation < ayaX) {
                            prev = aya
                            continue
                        }
                        // We have found the clicked Aya
                        currentAyaIndex = i
                        currentAya = aya
                        previousAya = prev
                        Log.d(TAG, "currentAya: $currentAya")
                        Log.d(TAG, "previousAya: $previousAya")
                        break
                    }
                    prev = aya
                }
            } else {
                Toast.makeText(activity, R.string.select_aya_fail, Toast.LENGTH_LONG).show()
            }
        }

    private fun scaleQuranImage() {
        val quranPageOriginalWidth: Int
        val quranPageOriginalHeight: Int
        when (recitationId) {
            Constants.Recitation.HAFS_ID -> {
                if (quranPageNum in 1..2) {
                    quranPageOriginalWidth = Constants.Quran.HAFS_PAGE_IMG_FIRST_TWO_ORIGINAL_WIDTH
                    quranPageOriginalHeight =
                        Constants.Quran.HAFS_PAGE_IMG_FIRST_TWO_ORIGINAL_HEIGHT
                } else {
                    quranPageOriginalWidth = Constants.Quran.HAFS_PAGE_IMG_ORIGINAL_WIDTH
                    quranPageOriginalHeight = Constants.Quran.HAFS_PAGE_IMG_ORIGINAL_HEIGHT
                }
            }

            Constants.Recitation.WARSH_ID -> {
                quranPageOriginalWidth = Constants.Quran.WARSH_PAGE_IMG_ORIGINAL_WIDTH
                quranPageOriginalHeight = Constants.Quran.WARSH_PAGE_IMG_ORIGINAL_HEIGHT
            }

            else -> error("Cannot identify recitation")
        }

        // make width fit in mobile screen and lineHeight scale
        if (_binding == null) return
        val params = binding.pageIv.layoutParams as RelativeLayout.LayoutParams
        if (quranPageOriginalWidth != quranImageContainerWidth) {
            imageScaleFactor =
                (quranImageContainerWidth.toFloat() / quranPageOriginalWidth).toDouble()
            params.width = quranImageContainerWidth
            params.height = (quranPageOriginalHeight * imageScaleFactor).toInt()
            binding.pageIv.layoutParams = params
        }

        // handle if lineHeight will be bigger than container lineHeight when above "if" is true
        if (context != null && ScreenUtils.isPortrait(requireContext()) && params.height > quranImageContainerHeight) {
            Log.d(TAG, "TRUE: portrait && params.lineHeight > quranImageContainerHeight")
            val ratioEdit = quranImageContainerHeight.toFloat() / params.height
            imageScaleFactor *= ratioEdit.toDouble() // the total scale done on the image
            params.height = quranImageContainerHeight
            params.width *= ratioEdit.toInt()
            binding.pageIv.layoutParams = params
        }
        Log.d(TAG, "final ImageScaleFactor: $imageScaleFactor")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        presenter!!.onDetach()
    }

    override fun onShareClick() {
        if (currentAya != null) {
            startActivity(IntentUtils.getShareIntent(currentAya!!.text, requireActivity()))
        }
    }

    override fun onFasilClick() {
        ayaActionsDialog!!.dismiss()
        if (isAyaBookmark) {                // remove from bookmark
            presenter!!.removeBookmark(currentAya!!.id)
        } else {                        // add to bookmark
            presenter!!.getBookmarkTypes()
        }
    }

    override fun onGetAyaBookmarkTypes(bookmarkTypes: List<BookmarkType>) {
        bookmarkDialog = AddBookmarkDialog.getInstance(bookmarkTypes, true)
        bookmarkDialog!!.show(childFragmentManager, "AddBookmarkDialog")
    }

    override fun addNormalBookmark(bookmarkType: Int) {
        if (currentAya != null) {
            presenter!!.insertAyaBookmark(AyaBookmark(currentAya!!.id, bookmarkType, currentAya!!))
        }
    }

    override fun addCustomBookmark(type: BookmarkType?) {
        val aya = currentAya
        if (aya != null && type != null) {
            presenter!!.insertCustomBookmark(aya, type)
        }
    }

    override fun onTafseerClick() {
        val fragment = parentFragment
        if (fragment is MushafFragment) {
            fragment.openTranslationDialog(
                currentAya!!
            )
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onNoteClick() {
        if (ScreenUtils.getOrientationState(requireActivity()) == ScreenUtils.PORTRAIT_STATE) {
            openAddNoteDialog()
        } else {
            noteDialogOpen = true
        }
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun openAddNoteDialog() {
        val dialog: AddNoteDialog = if (selectedAyaNote != null) {
            AddNoteDialog.getInstance(selectedAyaNote!!)
        } else {
            AddNoteDialog.getInstance(currentAya!!.id)
        }
        dialog.show(childFragmentManager, "AddNoteDialog")
    }

    override fun onGetPageAya(ayaList: List<Aya>) {
        pageAyasList = mutableListOf()
        pageAyasList?.addAll(ayaList)
        numOfAyaInPage = pageAyasList?.size ?: 0
        if (playFirstAyaAudio) checkPlayFirstAyaAudio()
        else if (playMiddleAyaAudio) checkPlayMiddleAyaAudio()
        else if (drawShadowFromNotification) drawAyaNotificationShadow()
    }

    override fun onGetAyaBookmarkType(bookmarkModel: BookmarkModel) {
        if (ayaActionsDialog != null) {
            isAyaBookmark = true
            ayaActionsDialog!!.setBookmarkTypeIcon(bookmarkModel)
        }
    }

    override fun onSuccessRemoveBookmark() {
        ayaActionsDialog!!.dismiss()
        Toast.makeText(activity, getString(R.string.bookmark_removed), Toast.LENGTH_SHORT).show()
    }

    override fun onAyaHasNote(note: Note) {
        if (ayaActionsDialog != null) {
            selectedAyaNote = note
            ayaActionsDialog!!.setAyaHasNote()
        }
    }

    // draw shadow when user make prev or next action on aya
    fun drawActionShadow(isClickPrev: Boolean) {
        if (pageAyasList == null) return
        if (isClickPrev) {
            previousAya = if (currentAyaIndex - 1 > 0) pageAyasList!![currentAyaIndex - 2] else null
            currentAya = pageAyasList!![currentAyaIndex - 1]
            currentAyaIndex--
        } else {
            previousAya = pageAyasList!![currentAyaIndex]
            currentAya = pageAyasList!![currentAyaIndex + 1]
            currentAyaIndex++
        }
        drawShadow()
    }

    // draw shadow on the selected aya
    private fun drawShadow() {
        if (!isSafeFragment(this)) return

        removePrevAyaShadows() // remove any if exists
        var shadowView: View
        var params: RelativeLayout.LayoutParams
        val prevAyaY: Double
        val prevAyaX: Double
        if (previousAya != null) {
            val prevX: Int
            val prevY: Int
            when (recitationId) {
                Constants.Recitation.HAFS_ID -> {
                    prevY = previousAya!!.y
                    prevX = previousAya!!.x
                }

                Constants.Recitation.WARSH_ID -> {
                    prevY = previousAya!!.yw
                    prevX = previousAya!!.xw
                }

                else -> throw RuntimeException("Cannot identify recitation")
            }
            prevAyaY = getScaledY(prevY, false)
            prevAyaX = getScaledX(prevX)
        } else {
            prevAyaY = top
            prevAyaX = end
        }
        val curX: Int
        val curY: Int
        when (recitationId) {
            Constants.Recitation.HAFS_ID -> {
                curX = currentAya!!.x
                curY = currentAya!!.y
            }

            Constants.Recitation.WARSH_ID -> {
                curX = currentAya!!.xw
                curY = currentAya!!.yw
            }

            else -> throw RuntimeException("Cannot identify recitation")
        }
        var currentAyaY = getScaledY(curY, false)
        val currentAyaX = getScaledX(curX)
        var endWidth = end
        var startWidth = currentAyaX
        var firstLine = true
        // draw line from current aya line to prev aya
        while (currentAyaY >= prevAyaY - lineHeight * 0.66) {
            shadowView = View(requireContext())
            ayaShadowsViews!!.add(shadowView)
            binding.quranPageContainer.addView(shadowView)
            params = shadowView.layoutParams as RelativeLayout.LayoutParams
            if (nightMode) {
                shadowView.setBackgroundColor(resources.getColor(R.color.aya_shadow_color_night_mode))
            } else {
                shadowView.setBackgroundColor(resources.getColor(R.color.aya_shadow_color))
            }
            if (!firstLine) {
                startWidth = start.toInt().toDouble()
            }
            if (currentAyaY - prevAyaY < lineHeight * 0.66) {
                endWidth = prevAyaX
            }
            params.leftMargin = startWidth.toInt()
            params.topMargin = currentAyaY.toInt()
            params.width = abs(endWidth - startWidth).toInt()
            params.height = lineHeight.toInt()
            shadowView.layoutParams = params
            currentAyaY -= lineHeight
            firstLine = false
        }
    }

    override fun showMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {
        binding.progreesBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        binding.progreesBar.visibility = View.GONE
    }

    private fun onQuranPageClick() {
        presenter!!.handleQuranPageClick()
    }

    val currentAyaId: Int
        get() = currentAya!!.id

    override fun onAddNote(note: Note?, isEditable: Boolean) {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        if (note != null) {
            presenter!!.addNote(note)
        }
        if (isEditable) {
            Toast.makeText(activity, getString(R.string.note_edited), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(activity, getString(R.string.note_added), Toast.LENGTH_LONG).show()
        }
    }

    override fun onDismissDialog() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun showQuranPage() {
        Glide.with(requireActivity()).load(quranImageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL).listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d(TAG, "onLoadFailed: GlideApp")
                    if (isSafeFragment(this@QuranPageFragment)) {
                        binding.progreesBar.visibility = View.GONE
                        binding.loadFailedContainer.root.visibility = View.VISIBLE
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    if (isSafeFragment(this@QuranPageFragment)) {
                        binding.progreesBar.visibility = View.GONE
                        binding.loadFailedContainer.root.visibility = View.GONE
                        isPageShown = true
                        if (nightMode) {
                            resource?.let { ImageUtil.invertDrawable(it) }
                        }

                        // auto play aya audio if audio player is playing  after image is loaded
                        if (playFirstAyaAudio && isVisibleToUser) checkPlayFirstAyaAudio() else if (playMiddleAyaAudio && isVisibleToUser) checkPlayMiddleAyaAudio() else if (drawShadowFromNotification && isVisibleToUser) drawAyaNotificationShadow() else {
                            if (currentAya == null) {
                                presenter!!.drawInitAyaShadow(quranPageNum, initSelectedAyaId)
                            } else {
                                // draw shadow if it is exist before orientation changed
                                drawShadow()
                            }
                        }
                    }
                    return false
                }
            }).into(binding.pageIv)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        this.isVisibleToUser = isVisibleToUser
        val activity: Activity? = activity
        if (activity != null && !activity.isChangingConfigurations && !isVisibleToUser) {
            isAyaAudioDownloaded = false
        }
    }

    fun onAyaAudioNotFound() {
        mushafFragment?.togglePauseState(false)
        isAyaAudioDownloaded = false
        val reciterId = AppPreferencesManager.getReciterSheikhSetting(requireContext())
        if (reciterId != null) {
            openDownloadAmountDialog(reciterId)
        } else {
            // user didn't choose any reciter yet
            openRecitersDialog()
        }
    }

    private fun openDownloadAmountDialog(reciterId: String) {
        val downloadAmountDialogFragment: AudioDownloadAmountDialogFragment =
            if (currentAya != null) {
                AudioDownloadAmountDialogFragment.newInstance(
                    recitationId, reciterId, currentAya!!.sura
                )
            } else {
                AudioDownloadAmountDialogFragment.newInstance(
                    recitationId, reciterId
                )
            }
        downloadAmountDialogFragment.show(childFragmentManager, "AudioDownloadAmountDialogFragment")
    }

    private fun openRecitersDialog() {
        val recitersDialogFragment = QuranRecitersDialogFragment.newInstance(recitationId)
        recitersDialogFragment.show(childFragmentManager, "QuranRecitersDialogFragment")
    }

    override fun onListenClick() {
        mushafFragment?.openAyaAudioDialog()
        currentAya?.let { mushafFragment?.checkAyaRecorderState(it.id) }
        mushafFragment?.playAudioService()
    }

    // get aya position of start repeat interval in current page
    private val firstAyaNumberInPage: Int
        get() {
            if (pageAyasList!![0].sura != mushafFragment!!.fromSuraDownloaded) {
                for (i in pageAyasList!!.indices) {
                    if (pageAyasList!![i].sura == mushafFragment!!.fromSuraDownloaded && pageAyasList!![i].suraAya == mushafFragment!!.firstAyaInRepeatGroup) {
                        return i
                    }
                }
            }
            return mushafFragment!!.firstAyaInRepeatGroup - pageAyasList!![0].suraAya
        }

    // draw shadow of current aya played in notification audio when launch app from notification
    private fun drawAyaNotificationShadow() {
        if (pageAyasList != null && drawShadowFromNotification && isPageShown) {
            drawShadowFromNotification = false
            if (currentAya!!.id != pageAyasList!![0].id) {
                for (i in 1 until pageAyasList!!.size) {
                    if (pageAyasList!![i].id == currentAya!!.id) {
                        previousAya = pageAyasList!![i - 1]
                        currentAyaIndex = i
                        break
                    }
                }
            } else {
                currentAyaIndex = 0
                previousAya = null
            }
            drawShadow()
        }
    }

    private fun checkPlayFirstAyaAudio() {
        if (isPageShown && pageAyasList != null && playFirstAyaAudio) {
            currentAyaIndex = 0
            currentAya = pageAyasList!![currentAyaIndex]
            previousAya = null
            drawShadow()
            mushafFragment!!.checkAyaRecorderState(currentAya!!.id)
            playFirstAyaAudio = false
            mushafFragment!!.playAudioService()
        }
    }

    private fun checkPlayMiddleAyaAudio() {
        if (playMiddleAyaAudio && isPageShown && pageAyasList != null) {
            currentAyaIndex = firstAyaNumberInPage
            currentAya = pageAyasList!![currentAyaIndex]
            previousAya = if (currentAyaIndex > 0) pageAyasList!![currentAyaIndex - 1] else null
            drawShadow()
            mushafFragment!!.checkAyaRecorderState(currentAya!!.id)
            playMiddleAyaAudio = false
            mushafFragment!!.playAudioService()
        }
    }

    fun playFirstAyaAudio() {
        playFirstAyaAudio = true
        checkPlayFirstAyaAudio()
    }

    fun playMiddleAyaAudio() {
        playMiddleAyaAudio = true
        checkPlayMiddleAyaAudio()
    }

    override fun onReciterSelected(recitationId: Int, reciter: ReciterModel) {
        openDownloadAmountDialog(reciter.id)
    }

    override fun onClickDownload() {
        mushafFragment!!.setSelectedAyaAudio(currentAya)
    }

    fun setCurrentAyaFromNotification(aya: Aya?) {
        currentAya = aya
        drawShadowFromNotification = true
        drawAyaNotificationShadow()
    }

    companion object {
        private val TAG = QuranPageFragment::class.java.simpleName
        private const val ARG_QURAN_PAGE_NUM = "ARG_QURAN_PAGE_NUM"
        private const val ARG_QURAN_IMAGE_URL = "ARG_QURAN_IMAGE_URL"
        private const val ARG_INIT_SELECTED_AYA_ID = "ARG_INIT_SELECTED_AYA_ID"
        private const val ARG_NIGHT_MODE = "ARG_NIGHT_MODE"
        private const val ARG_ZOOM_SCALE_FACTOR = "ARG_ZOOM_SCALE_FACTOR"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param quranImageUrl
         * @param quranPageNum
         * @param initSelectedAyaId
         * @param nightMode
         * @return A new instance of fragment @[QuranPageFragment]
         */
        @JvmStatic
        fun getInstance(
            quranImageUrl: String,
            quranPageNum: Int,
            initSelectedAyaId: Int,
            nightMode: Boolean,
            zoomScaleFactor: Float
        ): QuranPageFragment {
            Log.d(TAG, "Loading page number: $quranPageNum")
            val fragment = QuranPageFragment()
            val bundle = Bundle()
            bundle.putString(ARG_QURAN_IMAGE_URL, quranImageUrl)
            bundle.putInt(ARG_QURAN_PAGE_NUM, quranPageNum)
            bundle.putInt(ARG_INIT_SELECTED_AYA_ID, initSelectedAyaId)
            bundle.putBoolean(ARG_NIGHT_MODE, nightMode)
            bundle.putFloat(ARG_ZOOM_SCALE_FACTOR, zoomScaleFactor)
            fragment.arguments = bundle
            return fragment
        }
    }
}