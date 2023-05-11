package app.quranhub.ui.mushaf.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.entity.Aya
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.data.model.ReciterModel
import app.quranhub.data.service.QuranAudioDownloaderService.DownloadFinishEvent
import app.quranhub.databinding.FragmentMushafBinding
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment.ReciterSelectionListener
import app.quranhub.ui.main.MainActivity
import app.quranhub.ui.mushaf.adapter.QuranViewPagerAdapter
import app.quranhub.ui.mushaf.audio_manager.AudioStateEvent
import app.quranhub.ui.mushaf.audio_manager.AyaAudioService
import app.quranhub.ui.mushaf.audio_manager.SharedRepeatModel
import app.quranhub.ui.mushaf.dialogs.AyaAudioPopup
import app.quranhub.ui.mushaf.dialogs.AyaAudioPopup.AyaAudioListener
import app.quranhub.ui.mushaf.dialogs.AyaRecorderDialog
import app.quranhub.ui.mushaf.dialogs.AyaRecorderDialog.StopRecordingListener
import app.quranhub.ui.mushaf.dialogs.AyaRecorderPlayerDialog
import app.quranhub.ui.mushaf.dialogs.AyaRecorderPlayerDialog.AyaRecorderPlayerListener
import app.quranhub.ui.mushaf.dialogs.AyaRepeatDialog.AyaRepeatListener
import app.quranhub.ui.mushaf.dialogs.AyaRepeatDialog.Companion.getInstance
import app.quranhub.ui.mushaf.dialogs.TranslationsDialogFragment.Companion.newInstance
import app.quranhub.ui.mushaf.events.QuranPageClickEvent
import app.quranhub.ui.mushaf.fragments.MushafBottomBarFragment.QuranFooterCallbacks
import app.quranhub.ui.mushaf.fragments.TranslationsDataFragment.TranslationSelectionListener
import app.quranhub.ui.mushaf.model.QuranPageInfo
import app.quranhub.ui.mushaf.model.RepeatModel
import app.quranhub.ui.mushaf.model.SuraVersesNumber
import app.quranhub.ui.mushaf.presenter.Mus7fPresenter
import app.quranhub.ui.mushaf.presenter.Mus7fPresenterImp
import app.quranhub.ui.mushaf.view.MushafView
import app.quranhub.util.LocaleUtils.formatNumber
import app.quranhub.util.ScreenUtils.isLandscape
import app.quranhub.util.ScreenUtils.isPortrait
import app.quranhub.util.ScreenUtils.keepScreenOn
import app.quranhub.util.SharedPrefsUtils.getBoolean
import app.quranhub.util.SharedPrefsUtils.saveInteger
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView
import me.toptas.fancyshowcase.FocusShape
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Locale

class MushafFragment : Fragment(), MushafView, QuranFooterCallbacks, TranslationSelectionListener,
    AyaAudioListener, StopRecordingListener, ReciterSelectionListener, AyaRecorderPlayerListener,
    AyaRepeatListener {

    private var binding: FragmentMushafBinding? = null

    private var quranPageIndex = 0
    private var initAyaId = 0
    private var ayaNumber = 0
    private var suraNumber = 0
    private var ayaId = 0
    private var currentSuraName: String? = null
    private var book: TranslationBook? = null
    private var currentTafseerId: String? = null
    private var currentTafseerLang: String? = null
    private var seekbarPageHandler: Handler? = null
    private var seekbarPageRunnable: Runnable? = null
    private var pageSuras: ArrayList<ArrayList<Int>>? =
        null // 2D list include suras numbers in each page
    private var surasName: Array<String> = arrayOf()
    private var ayaAudioPopup: AyaAudioPopup? = null
    private var quranPageFragment: QuranPageFragment? = null
    private var ayaHasRecorder = false
    private var suraVersesNumberArrayList: ArrayList<SuraVersesNumber>? = null
    private var selectedAyaAudio: Aya? = null
    private var recitationId = 0
    private var isOriented = false
    private var bookDbName = "default"
    private var bookName: String? = null
    private var sheetBehavior: BottomSheetBehavior<*>? = null
    private var pagerAdapter: QuranViewPagerAdapter? = null
    private var footerbarFragment: MushafBottomBarFragment? = null
    private var headerbarFragment: MushafTopBarFragment? = null
    private var presenter: Mus7fPresenter<MushafView>? = null
    private var isBottomSheetVisible = false
    private var isAudioDialogOpen = false
    private var isAudioPlay = false
    private var initAudioOnFirstAya = false
    private var initAudioInRepeatGroup = false
    private var initAyaFromNotification = false
    private var startGroupRepeatPage = 0
    var firstAyaInRepeatGroup = 0
        private set
    var fromSuraDownloaded = 0
        private set
    private var notificationCurrentAya: Aya? = null
    private var audioServiceIntent: Intent? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("auido_dialog_state", isAudioDialogOpen)
        outState.putBoolean("auido_playing_state", isAudioPlay)
        outState.putBoolean("aya_has_recorder", ayaHasRecorder)
        outState.putInt("aya_id", ayaId)
        outState.putInt("start_group_page", startGroupRepeatPage)
        outState.putInt("first_repeat_aya", firstAyaInRepeatGroup)
        outState.putInt("from_sura_downloaded", fromSuraDownloaded)
        outState.putInt("page_index", quranPageIndex)
        if (sheetBehavior != null && sheetBehavior!!.state == BottomSheetBehavior.STATE_EXPANDED) {
            onCloseTranslationDiaog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMushafBinding.inflate(inflater, container, false)
        initPresenter()
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachListeners()

        quranPageIndex = Constants.Quran.NUM_OF_PAGES - 1
        initAyaId = -1 // No aya selected
        initFragments()
        getFragmentArguments(savedInstanceState != null)
        getPrevState(savedInstanceState)
        listenViewPagerSwipe()
        initViewPager(savedInstanceState != null)
        observeOnBottomSheetChanged()
        observeOnQuranSeekbarChange()
        if (AppPreferencesManager.getScreenReadingBacklightSetting(requireContext())) {
            // disable the screen timeout
            keepScreenOn(requireActivity(), true)
        }
        checkOrientationType()
    }

    private fun observeOnBottomSheetChanged() {
        sheetBehavior!!.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(view: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    isBottomSheetVisible = false
                }
            }

            override fun onSlide(view: View, v: Float) {}
        })
    }

    private fun initPresenter() {
        presenter = Mus7fPresenterImp(requireActivity())
        presenter!!.onAttach(this)
        presenter!!.getSurasInPage()
        presenter!!.getSuraNumofVerses()
    }

    private fun getPrevState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            isOriented = true
            isAudioPlay = it.getBoolean("auido_playing_state")
            isAudioDialogOpen = it.getBoolean("auido_dialog_state")
            ayaHasRecorder = it.getBoolean("aya_has_recorder")
            firstAyaInRepeatGroup = it.getInt("first_repeat_aya")
            fromSuraDownloaded = it.getInt("from_sura_downloaded")
            ayaId = it.getInt("aya_id")
            quranPageIndex = it.getInt("page_index")
            startGroupRepeatPage = it.getInt("start_group_page")
            checkAudioDialogState()
        }
    }

    private fun getFragmentArguments(withPrevState: Boolean) {
        surasName = resources.getStringArray(R.array.sura_name)
        arguments?.let {
            val selectedPage = it.getInt(ARG_INIT_PAGE, 1)
            quranPageIndex = Constants.Quran.NUM_OF_PAGES - selectedPage
            initAyaId = it.getInt(ARG_INIT_AYA, -1)
            if (it.getBoolean(ARG_FROM_NOTFICATION, false) &&
                initAyaId != -1 && !withPrevState
            ) {
                selectNotificationAya()
            }
            saveInteger(requireActivity(), "last_open_page", quranPageIndex)
        }
    }

    private fun selectNotificationAya() {
        presenter!!.getNotificationAya(initAyaId)
        openAyaAudioDialog()
        checkAyaRecorderState(initAyaId)
        if (getBoolean(requireActivity(), AyaAudioService.AUDIO_PLAYING, false)) {
            ayaAudioPopup!!.setPlayState()
        }
    }

    private fun checkAudioDialogState() {
        if (isAudioDialogOpen) {
            binding!!.quranViewpager.post { ayaAudioPopup!!.showPopup(binding!!.quranViewpager) }
            if (isAudioPlay) {
                ayaAudioPopup!!.setPlayState()
            }
            ayaAudioPopup!!.setRecordState(ayaHasRecorder)
        }
    }

    // hide quran seekbar in landscape mode
    private fun checkOrientationType() {
        if (isLandscape(requireActivity())) {
            binding!!.quranSeekbar.visibility = View.GONE
        }
    }

    // observe on seek bar changes from user to show its progress for page number and sura name
    private fun observeOnQuranSeekbarChange() {
        seekbarPageHandler = Handler()
        seekbarPageRunnable = Runnable { binding!!.pageSeekTv.visibility = View.GONE }
        binding!!.quranSeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                seekbarPageHandler!!.removeCallbacks(seekbarPageRunnable!!)
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    setSeekPageInfo(progress)
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                binding!!.quranViewpager.currentItem =
                    Constants.Quran.NUM_OF_PAGES - seekBar.progress
                seekbarPageHandler!!.postDelayed(seekbarPageRunnable!!, 1000)
            }
        })
    }

    // set page and its suras when change progress of fast Quran seekbar
    private fun setSeekPageInfo(progress: Int) {
        var progress1 = progress
        if (progress1 == 0) progress1 = 1
        var surahsInPage = ""
        val progressStr = formatNumber(progress1)
        binding!!.pageSeekTv.visibility = View.VISIBLE
        if (pageSuras != null) {
            val numOfSurasInPage = pageSuras!![progress1].size
            for (i in 0 until numOfSurasInPage) {
                surahsInPage += surasName[pageSuras!![progress1][i] - 1]
                if (i != numOfSurasInPage - 1) {
                    surahsInPage += " , "
                }
            }
        }
        binding!!.pageSeekTv.text = """
             $surahsInPage
             $progressStr
             """.trimIndent()
    }

    private fun setupMus7afShowcase() {
        if (isPortrait(requireActivity())) { // FancyShowCaseView works very bad on landscape
            // Showcase to notify the user about the clicking action on the sura name in `toolbarFragment`
            val suraNameShowCaseView: FancyShowCaseView =
                FancyShowCaseView.Builder(requireActivity())
                    .focusOn(headerbarFragment!!.requireView())
                    .title(getString(R.string.showcase_title_sura_index))
                    .enableAutoTextPosition()
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .roundRectRadius(90)
                    .fitSystemWindows(true)
                    .delay(300)
                    .showOnce("sura_name_showcase")
                    .build()

            // Showcase to notify the user about long clicking ayas in screen
            val ayaShowCaseView: FancyShowCaseView = FancyShowCaseView.Builder(requireActivity())
                .focusOn(binding!!.quranViewpager)
                .title(getString(R.string.showcase_title_aya_longclick))
                .focusCircleRadiusFactor(0.4)
                .fitSystemWindows(true)
                .showOnce("aya_longclick_showcase")
                .build()
            FancyShowCaseQueue()
                .add(suraNameShowCaseView)
                .add(ayaShowCaseView)
                .show()
        }
    }

    override fun onStart() {
        super.onStart()
        registerEventBus()
        setupMus7afShowcase()
    }

    override fun onPause() {
        super.onPause()
        saveInteger(requireActivity(), "last_open_page", quranPageIndex)
    }

    override fun onStop() {
        super.onStop()
        unRegisterEventBus()
    }

    override fun onDestroyView() {
        presenter!!.onDetach()
        super.onDestroyView()
        binding = null
        if (AppPreferencesManager.getScreenReadingBacklightSetting(requireContext())) {
            // re-enable the screen timeout
            keepScreenOn(requireActivity(), false)
        }
        dismissAudioPopup()
    }

    private fun listenViewPagerSwipe() {
        binding!!.quranViewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                setSelectedPageViews(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun setSelectedPageViews(position: Int) {
        quranPageFragment = null
        quranPageIndex = position
        setPageNumber(position)
        setSeekbarProgress(position)
        setPageDir()
        presenter!!.getQuranPageInfo(position)
        onCloseTranslationDiaog()
        if (!isOriented) {
            checkAudioAutoPlay()
        } else {
            isOriented = false
        }
    }

    private fun checkAudioAutoPlay() {
        setCurrentQuranPageFragment()
        // auto play audio when user swipe to page and the audio is playing OR last aya in page is finished and will start audio for first aya in new paeg
        if (initAudioOnFirstAya || isAudioDialogOpen && isAudioPlay) {
            quranPageFragment!!.playFirstAyaAudio()
            initAudioOnFirstAya = false
        }

        // play audio for from-aya in repeating interval
        if (initAudioInRepeatGroup) {
            initAudioInRepeatGroup = false
            quranPageFragment!!.playMiddleAyaAudio()
        }

        // set current aya with draw shadow when open mushaf from audio notification
        if (initAyaFromNotification) {
            initAyaFromNotification = false
            if (getBoolean(requireActivity(), AyaAudioService.AUDIO_PLAYING, false)) {
                isAudioPlay = true
            }
            quranPageFragment!!.setCurrentAyaFromNotification(notificationCurrentAya)
        }
    }

    private fun setCurrentQuranPageFragment() {
        if (quranPageFragment == null) {
            quranPageFragment = binding!!.quranViewpager.adapter?.instantiateItem(
                binding!!.quranViewpager,
                binding!!.quranViewpager.currentItem
            ) as? QuranPageFragment
        }
    }

    private fun setPageDir() {
        if (quranPageIndex % 2 == 0) { // quranPageIndex is even
            headerbarFragment!!.setPageDir(MushafTopBarFragment.PAGE_DIR_LEFT)
        } else { // quranPageIndex is odd
            headerbarFragment!!.setPageDir(MushafTopBarFragment.PAGE_DIR_RIGHT)
        }
    }

    private fun setPageNumber(position: Int) {
        val page = formatNumber(Constants.Quran.NUM_OF_PAGES - position)
        footerbarFragment!!.setCurrentPage(page)
    }

    private fun setSeekbarProgress(position: Int) {
        binding!!.quranSeekbar.progress = Constants.Quran.NUM_OF_PAGES - position
    }

    private fun initFragments() {
        currentTafseerId = AppPreferencesManager.getQuranTranslationBook(requireActivity())
        currentTafseerLang = AppPreferencesManager.getQuranTranslationLanguage(requireActivity())
        recitationId = AppPreferencesManager.getRecitationSetting(requireContext())
        ayaAudioPopup = AyaAudioPopup(requireActivity(), this)
        bookName = getString(R.string.translation_muyassar)
        binding!!.translationBottomSheet.translationSheetTv.movementMethod =
            ScrollingMovementMethod()
        sheetBehavior = BottomSheetBehavior.from(binding!!.translationBottomSheet.bottomSheet)
        val fragmentManager = childFragmentManager
        footerbarFragment =
            fragmentManager.findFragmentById(R.id.footerbar_fragment) as MushafBottomBarFragment?
        headerbarFragment =
            fragmentManager.findFragmentById(R.id.top_bar_fragment) as MushafTopBarFragment?
        presenter!!.getQuranPageInfo(quranPageIndex)
    }

    private fun initViewPager(isInstanceSaved: Boolean) {
        val quranImageBaseUrl: String = when (recitationId) {
            Constants.Recitation.HAFS_ID -> Constants.Quran.HAFS_IMG_BASE_URL
            Constants.Recitation.WARSH_ID -> Constants.Quran.WARSH_IMG_BASE_URL
            else -> throw RuntimeException("Cannot identify recitation")
        }
        val quranPageImages: MutableList<String> = ArrayList()
        for (i in Constants.Quran.NUM_OF_PAGES downTo 1) {
            val imageName = when (recitationId) {
                Constants.Recitation.HAFS_ID -> String.format(Locale.US, "%d.jpg", i)
                Constants.Recitation.WARSH_ID -> String.format(Locale.US, "%d.png", i)
                else -> throw RuntimeException("Cannot identify recitation")
            }
            quranPageImages.add(quranImageBaseUrl + imageName)
        }
        pagerAdapter = QuranViewPagerAdapter(
            childFragmentManager,
            quranPageImages,
            presenter!!.nightMode,
            presenter!!.quranPageZoomScaleFactor,
            initAyaId
        )
        binding!!.quranViewpager.adapter = pagerAdapter
        if (!isInstanceSaved) {
            binding!!.quranViewpager.currentItem = quranPageIndex
        }
        setSeekbarProgress(quranPageIndex)
        // handle bug in viewpager onSelectedPage callback not called when position is 0
        if (quranPageIndex == 0) {
            setSelectedPageViews(0)
        }
    }

    override fun showQuranPageInfo(quranPageInfo: QuranPageInfo) {
        val suraName = resources.getStringArray(R.array.sura_name)[quranPageInfo.sura - 1]
        val guz2Name = resources.getStringArray(R.array.agza2_name)[quranPageInfo.juz - 1]
        headerbarFragment!!.setSuraText(suraName)
        headerbarFragment!!.setGuz2Text(guz2Name)
    }

    override fun showMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    fun dismissAudioPopup() {
        if (ayaAudioPopup != null && isAudioDialogOpen) {
            ayaAudioPopup!!.dismissPopup()
            isAudioDialogOpen = false
        }
    }

    override fun showLoading() {}
    override fun hideLoading() {}
    private fun toggleQuranBars() {
        if (binding!!.barsGroup.visibility == View.VISIBLE) {
            binding!!.barsGroup.visibility = View.GONE
        } else {
            binding!!.barsGroup.visibility = View.VISIBLE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onQuranPageClick(event: QuranPageClickEvent?) {
        toggleQuranBars()
    }

    override fun openSearchFragment() {
        val activity = activity as MainActivity?
        activity?.openSearchFragment()
    }

    override fun toggleNightMode(): Boolean {
        val newNightMode = presenter!!.toggleNightMode()
        pagerAdapter!!.setNightMode(newNightMode) // update current session
        return newNightMode
    }

    override fun updateQuranPageZoomScale(zoomScaleFactor: Float) {
        AppPreferencesManager.persistQuranPageZoomScaleSetting(requireActivity(), zoomScaleFactor)
        pagerAdapter!!.setZoomScaleFactor(zoomScaleFactor)
    }

    private fun attachListeners() {
        binding!!.translationBottomSheet.closeBtn.setOnClickListener { v: View? -> onCloseTranslationDiaog() }
        binding!!.translationBottomSheet.tvBookName.setOnClickListener { v: View? -> onBookNameClicked() }
        binding!!.translationBottomSheet.moreBtn.setOnClickListener { v: View? -> onClickMore() }
        binding!!.translationBottomSheet.nextIv.setOnClickListener { v: View? -> onGetNextAyaTafseer() }
        binding!!.translationBottomSheet.prevIv.setOnClickListener { v: View? -> onClickPrevAya() }
    }

    private fun onCloseTranslationDiaog() {
        if (sheetBehavior!!.peekHeight > 0) {
            sheetBehavior!!.peekHeight = 0
            sheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
            isBottomSheetVisible = false
        }
    }

    fun openTranslationDialog(selectedAya: Aya) {
        dismissAudioPopup()
        binding!!.translationBottomSheet.sheetProgreesBar.visibility = View.VISIBLE
        // show default tafseer from locale db when tafseer lang is arabic
        if (currentTafseerId == null && currentTafseerLang == Constants.Language.ARABIC_CODE) {         // get aya tafseer from default book ("EL-Meyser")
            onGetAyaTafseer(selectedAya.tafseer)
            binding!!.translationBottomSheet.nextIv.visibility = View.VISIBLE
            binding!!.translationBottomSheet.prevIv.visibility = View.VISIBLE
        } else if (book == null) {
            // load translation book from TranslationDB if exist to get aya translation from it
            presenter!!.getCurrentTafseerBook(currentTafseerId)
        } else {
            presenter!!.getAyaTafseer(selectedAya.id)
        }
        ayaId = selectedAya.id
        ayaNumber = selectedAya.suraAya
        suraNumber = selectedAya.sura
        currentSuraName = resources.getStringArray(R.array.sura_name)[selectedAya.sura - 1]
    }

    override fun onGetAyaTafseer(tafseer: String) {
        binding!!.translationBottomSheet.sheetProgreesBar.visibility = View.GONE
        binding!!.translationBottomSheet.translationSheetTv.scrollTo(0, 0)
        if (!isBottomSheetVisible) {
            sheetBehavior!!.peekHeight =
                resources.getDimension(com.intuit.sdp.R.dimen._105sdp).toInt()
        }
        binding!!.translationBottomSheet.translationSheetTv.text = tafseer
    }

    // load translation book successfully from TranslationDB
    override fun onGetTafseerBook(book: TranslationBook) {
        bookDbName = book.databaseName
        binding!!.translationBottomSheet.tvBookName.text = book.name
        presenter!!.getAyaTafseer(ayaId)
        this.book = book
        bookName = book.name
        binding!!.translationBottomSheet.nextIv.visibility = View.VISIBLE
        binding!!.translationBottomSheet.prevIv.visibility = View.VISIBLE
    }

    // selected translation book is not exist in TranslationDB
    override fun onNoBooksExist() {
        onGetAyaTafseer(getString(R.string.no_downloaded_books))
        binding!!.translationBottomSheet.tvBookName.text = getString(R.string.choose_book)
    }

    // get Suras for each page in quran to show when user use bottom seekbar
    override fun onGetPageSuras(suras: ArrayList<ArrayList<Int>>) {
        pageSuras = suras
    }

    // when selected aya has recorder
    override fun onGetAyaRecorder(recorderPath: String) {
        if (ayaAudioPopup != null) {
            ayaAudioPopup!!.setRecordState(true)
            ayaHasRecorder = true
        }
    }

    override fun onGetSuraVersesNumber(suraVersesNumbers: ArrayList<SuraVersesNumber>) {
        suraVersesNumberArrayList = suraVersesNumbers
        setAudioService()
    }

    // show list of available books for translation language
    private fun onBookNameClicked() {
        val transLang = AppPreferencesManager.getQuranTranslationLanguage(requireContext())
        val translationsDialog = newInstance(
            transLang, this
        )
        translationsDialog.show(parentFragmentManager, "trans_dialog")
    }

    // open tafseer screen to show its Ayas with thier translation
    private fun onClickMore() {
        val activity = activity as MainActivity?
        activity?.openTafseerScreen(currentSuraName, suraNumber, bookDbName, bookName, ayaNumber)
    }

    // User choose book from available downloaded translation books
    override fun onTranslationSelected(translationBook: TranslationBook) {
        presenter!!.onGetTafsserBook(translationBook)
        currentTafseerId = translationBook.id
    }

    private fun setTafseerUserAction(isClickPrev: Boolean) {
        quranPageFragment!!.drawActionShadow(isClickPrev)
        val selectedAya = quranPageFragment!!.currentAya
        if (book != null) {
            presenter!!.getAyaTafseer(selectedAya!!.id)
        } else {
            onGetAyaTafseer(selectedAya!!.tafseer)
        }
    }

    // get next aya tafseer
    private fun onGetNextAyaTafseer() {
        setCurrentQuranPageFragment()
        val selectedAyaIndex = quranPageFragment!!.currentAyaIndex
        val numOfAyaInPage = quranPageFragment!!.numOfAyaInPage
        if (selectedAyaIndex != numOfAyaInPage - 1) {
            setTafseerUserAction(false)
        }
    }

    // get prev aya tafseer
    private fun onClickPrevAya() {
        setCurrentQuranPageFragment()
        val selectedAyaIndex = quranPageFragment!!.currentAyaIndex
        if (selectedAyaIndex != 0) {
            setTafseerUserAction(true)
        }
    }

    // open audio dailog actions for selected aya
    fun openAyaAudioDialog() {
        onCloseTranslationDiaog()
        binding!!.barsGroup.visibility = View.GONE
        if (!isAudioDialogOpen) {
            binding!!.quranViewpager.post { ayaAudioPopup!!.showPopup(binding!!.quranViewpager) }
            isAudioDialogOpen = true
        }
    }

    // check if selected aya has user recorder voice
    fun checkAyaRecorderState(ayaId: Int) {
        this.ayaId = ayaId
        presenter!!.checkAyaHasRecorder(ayaId)
        ayaHasRecorder = false
        ayaAudioPopup!!.setRecordState(false)
    }

    override fun checkPlayPauseState() {
        setCurrentQuranPageFragment()
        // if play audio with no selected aya => the audio play on first aya in page
        if (isAudioPlay) {
            togglePauseState(true)
        } else {
            if (quranPageFragment!!.currentAya == null) quranPageFragment!!.playFirstAyaAudio() else togglePlayState(
                true
            )
        }
    }

    override fun onClickRepeat() {
        setCurrentQuranPageFragment()
        val currentAya = quranPageFragment!!.currentAya
        val repeatDialog = getInstance(suraVersesNumberArrayList, currentAya)
        repeatDialog.show(childFragmentManager, "AyaRepeatDialog")
    }

    fun playNextAyaAudio() {
        if (quranPageFragment!!.currentAya != null) {
            if (quranPageFragment!!.currentAyaIndex != quranPageFragment!!.numOfAyaInPage - 1) {
                audioServiceIntent!!.action = AyaAudioService.ACTION_NEXT
                audioServiceIntent!!.putExtra(
                    AyaAudioService.AYA_ID_KEY,
                    quranPageFragment!!.currentAyaId + 1
                )
                requireActivity().startService(audioServiceIntent)
            } else {
                swipToNextQuranPage()
            }
        }
    }

    fun playPrevAyaAudio() {
        if (quranPageFragment!!.currentAya != null) {
            if (quranPageFragment!!.currentAyaIndex != 0) {
                audioServiceIntent!!.action = AyaAudioService.ACTION_PREVIOUS
                audioServiceIntent!!.putExtra(
                    AyaAudioService.AYA_ID_KEY,
                    quranPageFragment!!.currentAyaId - 1
                )
                requireActivity().startService(audioServiceIntent)
            } else {
                swipToPrevQuranPage()
            }
        }
    }

    // handle user click to play next aya audio
    override fun onPlayNextAya() {
        setCurrentQuranPageFragment()
        playNextAyaAudio()
    }

    // handle user click to play prev aya audio
    override fun onPlayPrevAya() {
        setCurrentQuranPageFragment()
        playPrevAyaAudio()
    }

    override fun onPressRecord() {
        if (isAudioPlay) {
            togglePauseState(true)
        }
        ayaAudioPopup!!.dismissPopup()
        // open aya recorder player if selected aya has recorder voice
        if (ayaHasRecorder) {
            openAyaRecorderPlayer()
        } else {
            // get permission to access microphone and mobile storage to save user voice recorder on phone
            getRecordingPerm()
        }
    }

    override fun onClickReciter() {
        val recitersDialogFragment = QuranRecitersDialogFragment
            .newInstance(recitationId)
        recitersDialogFragment.show(childFragmentManager, "QuranRecitersDialogFragment")
    }

    override fun onClickStop() {
        setCurrentQuranPageFragment()
        isAudioPlay = false
        ayaHasRecorder = false
        isAudioDialogOpen = false
        ayaAudioPopup!!.dismissPopup()
        requireActivity().stopService(audioServiceIntent)
    }

    private fun openAyaRecorderPlayer() {
        isAudioDialogOpen = false
        val dialog = AyaRecorderPlayerDialog.getInstance(ayaId)
        dialog.show(childFragmentManager, "AyaRecorderPlayerDialog")
    }

    private fun getRecordingPerm() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    permissions[0]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startAyaRecording()
            } else {
                requestPermissions(permissions, REQUEST_RECORDING_PERM)
            }
        } else {
            startAyaRecording()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var isGranted = true
        if (requestCode == REQUEST_RECORDING_PERM) {
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false
                    break
                }
            }
            if (isGranted) {
                startAyaRecording()
            } else {
                Toast.makeText(activity, getString(R.string.accept_perm), Toast.LENGTH_LONG).show()
            }
        }
    }

    // start voice recording of selected aya
    private fun startAyaRecording() {
        isAudioDialogOpen = false
        val recorderDialog = AyaRecorderDialog.getInstance(ayaId)
        recorderDialog.show(childFragmentManager, "AyaRecorderDialog")
    }

    override fun onStopRecording(filePath: String?) {
        presenter!!.saveRecorderPath(ayaId, filePath)
        ayaAudioPopup!!.showPopup(binding!!.quranViewpager)
        ayaAudioPopup!!.setRecordState(true)
        ayaHasRecorder = true
        isAudioDialogOpen = true
    }

    override fun onClickDeleteRecorder() {
        ayaAudioPopup!!.showPopup(binding!!.quranViewpager)
        isAudioDialogOpen = true
        ayaHasRecorder = false
        ayaAudioPopup!!.setRecordState(false)
        presenter!!.deleteAyaVoiceRecorder(ayaId)
    }

    private fun autoSwipPage(page: Int) {
        isOriented = false
        isAudioPlay = false
        ayaAudioPopup!!.setPauseState()
        binding!!.quranViewpager.setCurrentItem(page, true)
    }

    // start audio of selected aya after it downloaded its sura audios
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onDownloadAudioFinished(event: DownloadFinishEvent?) {
        if (!isAudioDialogOpen) return
        firstAyaInRepeatGroup = selectedAyaAudio!!.suraAya
        fromSuraDownloaded = selectedAyaAudio!!.sura
        Log.d("ww9", "onDownloadAudioFinished: $firstAyaInRepeatGroup , $fromSuraDownloaded")
        if (selectedAyaAudio != null && quranPageIndex != Constants.Quran.NUM_OF_PAGES - selectedAyaAudio!!.page) {
            initAudioInRepeatGroup = true
            autoSwipPage(Constants.Quran.NUM_OF_PAGES - selectedAyaAudio!!.page)
        } else {
            setCurrentQuranPageFragment()
            quranPageFragment!!.playMiddleAyaAudio()
        }
    }

    // go to next quran page after last aya audio was finished in current page
    fun swipToNextQuranPage() {
        if (quranPageIndex != 0) {
            initAudioOnFirstAya = true
            autoSwipPage(quranPageIndex - 1)
        }
    }

    fun swipToPrevQuranPage() {
        if (quranPageIndex != Constants.Quran.NUM_OF_PAGES) {
            initAudioOnFirstAya = true
            autoSwipPage(quranPageIndex + 1)
        }
    }

    // swip to from-aya interval when repeating group is finsihed
    fun swipToFirstAyaInRepeatGroup() {
        setCurrentQuranPageFragment()
        if (startGroupRepeatPage != quranPageIndex) {
            initAudioInRepeatGroup = true
            autoSwipPage(startGroupRepeatPage)
        } else {
            quranPageFragment!!.playMiddleAyaAudio()
        }
    }

    /**
     * when select start repeat
     * set first aya of repeating to shadow it and play its audio after group is finished (if repeating group > 1)
     * get first aya of repeating page to swip to it and start play audio
     *
     * @param repeatModel
     */
    override fun onAyasRepeat(repeatModel: RepeatModel?) {
        SharedRepeatModel.repeatModel = repeatModel
        SharedRepeatModel.isRepeatModelChanged = true
        firstAyaInRepeatGroup = repeatModel!!.fromAya
        fromSuraDownloaded = repeatModel.fromSura
        presenter!!.getFromAyaPage(repeatModel.fromAyaId)
    }

    // swipe to page which contain from-aya interval in repeating
    override fun onGetAyaPage(page: Int) {
        var page = page
        setCurrentQuranPageFragment()
        page = Constants.Quran.NUM_OF_PAGES - page
        startGroupRepeatPage = page
        if (page != quranPageIndex) {
            initAudioInRepeatGroup = true
            autoSwipPage(page)
        } else {
            quranPageFragment!!.playMiddleAyaAudio()
        }
    }

    fun setSelectedAyaAudio(selectedAyaAudio: Aya?) {
        this.selectedAyaAudio = selectedAyaAudio
    }

    override fun onReciterSelected(recitationId: Int, reciter: ReciterModel) {
        setCurrentQuranPageFragment()
        quranPageFragment!!.onListenClick()
    }

    private fun setAudioService() {
        if (audioServiceIntent == null) {
            audioServiceIntent = Intent(activity, AyaAudioService::class.java)
            audioServiceIntent!!.putExtra(
                AyaAudioService.SURA_VERSES_KEY,
                suraVersesNumberArrayList
            )
        }
    }

    // play audio for selected aya
    fun resumeAyaAudio() {
        audioServiceIntent!!.action = AyaAudioService.ACTION_RESUME
        requireActivity().startService(audioServiceIntent)
    }

    // pause audio of selected aya
    fun pauseAyaAudio() {
        audioServiceIntent!!.action = AyaAudioService.ACTION_PAUSE
        requireActivity().startService(audioServiceIntent)
    }

    fun playAudioService() {
        setCurrentQuranPageFragment()
        if (quranPageFragment!!.currentAya != null) {
            audioServiceIntent!!.putExtra(
                AyaAudioService.AYA_ID_KEY,
                quranPageFragment!!.currentAyaId
            )
            audioServiceIntent!!.action = AyaAudioService.ACTION_PLAY
            requireActivity().startService(audioServiceIntent)
        }
    }

    fun togglePlayState(checkPlayAudio: Boolean) {
        setCurrentQuranPageFragment()
        isAudioPlay = true
        ayaAudioPopup!!.setPlayState()
        if (checkPlayAudio) {
            if (quranPageFragment!!.isAyaAudioDownloaded) resumeAyaAudio() else playAudioService()
        }
    }

    fun togglePauseState(checkPauseAudio: Boolean) {
        isAudioPlay = false
        ayaAudioPopup!!.setPauseState()
        if (checkPauseAudio) pauseAyaAudio()
    }

    // get current aya info which it playing in audio notification
    override fun onGetCurrentAyaFromNotification(aya: Aya) {
        initAyaFromNotification = true
        notificationCurrentAya = aya
        binding!!.quranViewpager.currentItem = Constants.Quran.NUM_OF_PAGES - aya.page
    }

    private fun registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
    }

    private fun unRegisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this)
    }

    // Eventbus subscriber to recieve audio states from audio foreground service
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onAudioStateChanged(audioStateEvent: AudioStateEvent) {
        setCurrentQuranPageFragment()
        if (audioStateEvent.audioState == AudioStateEvent.State.PAUSED) {
            togglePauseState(false)
        } else if (audioStateEvent.audioState == AudioStateEvent.State.RESUME || audioStateEvent.audioState == AudioStateEvent.State.PLAYING) {
            quranPageFragment!!.isAyaAudioDownloaded = true
            togglePlayState(false)
        } else if (audioStateEvent.audioState == AudioStateEvent.State.PLAY_NEXT) {
            if (quranPageFragment!!.currentAyaIndex != quranPageFragment!!.numOfAyaInPage - 1) {
                quranPageFragment!!.drawActionShadow(false)
                checkAyaRecorderState(quranPageFragment!!.currentAyaId)
            } else {
                swipToNextQuranPage()
            }
        } else if (audioStateEvent.audioState == AudioStateEvent.State.PLAY_PREV) {
            if (quranPageFragment!!.currentAyaIndex != 0) {
                quranPageFragment!!.drawActionShadow(true)
                checkAyaRecorderState(quranPageFragment!!.currentAyaId)
            } else {
                swipToPrevQuranPage()
            }
        } else if (audioStateEvent.audioState == AudioStateEvent.State.STOP) {
            onClickStop()
        } else if (audioStateEvent.audioState == AudioStateEvent.State.COMPLETED) {
            if (quranPageFragment!!.currentAyaIndex == quranPageFragment!!.numOfAyaInPage - 1) {
                swipToNextQuranPage()
            } // play audio of next aya after current aya audio was finished
            else {
                quranPageFragment!!.drawActionShadow(false)
                checkAyaRecorderState(quranPageFragment!!.currentAyaId)
            }
        } else if (audioStateEvent.audioState == AudioStateEvent.State.NOT_DOWNLOADED) {
            quranPageFragment!!.onAyaAudioNotFound()
        } else if (audioStateEvent.audioState == AudioStateEvent.State.GROUP_REPEAT_COMPLETED) {
            swipToFirstAyaInRepeatGroup()
        }
    }

    companion object {
        private val TAG = MushafFragment::class.java.simpleName

        private const val ARG_INIT_PAGE = "ARG_INIT_PAGE"
        private const val ARG_FROM_NOTFICATION = "ARG_FROM_NOTIFICATION"
        private const val ARG_INIT_AYA = "ARG_INIT_AYA"

        private const val REQUEST_RECORDING_PERM = 1

        fun newInstance(): MushafFragment {
            return MushafFragment()
        }

        /**
         * Create a MushafFragment instance initialized at the given page.
         *
         * @param initPageNumber
         * @return
         */
        fun newInstance(initPageNumber: Int): MushafFragment {
            val mushafFragment = MushafFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_INIT_PAGE, initPageNumber)
            mushafFragment.arguments = bundle
            return mushafFragment
        }

        /**
         * Create a MushafFragment instance initialized at the given page & highlighting the given aya.
         *
         * @param initPageNumber
         * @param initAyaId
         * @return
         */
        fun newInstance(initPageNumber: Int, initAyaId: Int): MushafFragment {
            val mushafFragment = MushafFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_INIT_PAGE, initPageNumber)
            bundle.putInt(ARG_INIT_AYA, initAyaId)
            mushafFragment.arguments = bundle
            return mushafFragment
        }

        fun newNotificationInstance(ayaId: Int): MushafFragment {
            val mushafFragment = MushafFragment()
            val bundle = Bundle()
            bundle.putBoolean(ARG_FROM_NOTFICATION, true)
            bundle.putInt(ARG_INIT_AYA, ayaId)
            mushafFragment.arguments = bundle
            return mushafFragment
        }
    }
}