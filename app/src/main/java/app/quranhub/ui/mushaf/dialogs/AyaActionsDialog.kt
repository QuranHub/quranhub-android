package app.quranhub.ui.mushaf.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.databinding.DialogAyaPropertiesBinding
import app.quranhub.ui.mushaf.model.BookmarkModel
import app.quranhub.ui.mushaf.viewmodel.QuranPageViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AyaActionsDialog : DialogFragment() {

    private var yLocation = 0
    private var dialog: Dialog? = null
    private var ayaPropertiesListener: AyaPropertiesListener? = null
    private var binding: DialogAyaPropertiesBinding? = null

    // Shares the host page's ViewModel (UI shell over one ViewModel per feature)
    private val viewModel: QuranPageViewModel by lazy {
        ViewModelProvider(requireParentFragment())[QuranPageViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ayaPropertiesListener = if (parentFragment is AyaPropertiesListener) {
            parentFragment as AyaPropertiesListener?
        } else {
            throw ClassCastException(
                requireParentFragment().javaClass.simpleName + " must implement AyaActionsDialog#AyaPropertiesListener"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            yLocation = it.getInt(ARG_Y_LOCATION)
        } ?: Log.w(TAG, "AyaActionsDialog : No arguments specified")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAyaPropertiesBinding.inflate(layoutInflater)
        initializeDialog()
        observeAyaState()
        return dialog!!
    }

    // Bookmark type and note of the selected aya come from the shared page
    // ViewModel state (formerly pushed by the host fragment's presenter)
    private fun observeAyaState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .map { it.selectedAyaBookmarkType to it.selectedAyaNote }
                    .distinctUntilChanged()
                    .collect { (bookmarkType, note) ->
                        bookmarkType?.let { setBookmarkTypeIcon(it) }
                        if (note != null) {
                            setAyaHasNote()
                        }
                    }
            }
        }
    }

    fun initializeDialog() {
        dialog = Dialog(requireActivity())
        dialog!!.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        val layoutParams = dialog!!.window!!.attributes
        layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        layoutParams.y = yLocation
        dialog!!.setContentView(binding!!.root)
        dialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
        attachListeners()
    }

    fun setBookmarkTypeIcon(bookmarkModel: BookmarkModel) {
        // handle if set image after orientation change
        when (bookmarkModel.bookmarkTypeId) {
            Constants.BookmarkType.NOTE -> {
                binding!!.bookmarkIv.setImageResource(R.drawable.bookmark_green_selected)
            }

            Constants.BookmarkType.MEMORIZE -> {
                binding!!.bookmarkIv.setImageResource(R.drawable.bookmark_red_selected)
            }

            Constants.BookmarkType.RECITING -> {
                binding!!.bookmarkIv.setImageResource(R.drawable.bookmark_gold_selected)
            }

            Constants.BookmarkType.FAVORITE -> {
                binding!!.bookmarkIv.setImageResource(R.drawable.fav_added__gold_ic)
            }

            else -> {    // CUSTOM BOOKMARK
                binding!!.bookmarkIv.setImageResource(R.drawable.bookmark_green_selected)
                binding!!.bookmarkIv.setColorFilter(requireActivity().resources.getIntArray(R.array.bookmark_colors)[bookmarkModel.colorIndex])
            }
        }
    }

    private fun attachListeners() {
        binding!!.shareContainer.setOnClickListener { onShareClick() }
        binding!!.faselContainer.setOnClickListener { onFasilClick() }
        binding!!.listenContainer.setOnClickListener { onListenClick() }
        binding!!.tafseerContainer.setOnClickListener { onTafserClick() }
        binding!!.notesContainer.setOnClickListener { onNotesClick() }
    }

    private fun onShareClick() {
        dialog!!.dismiss()
        ayaPropertiesListener!!.onShareClick()
    }

    private fun onFasilClick() {
        dialog!!.dismiss()
        ayaPropertiesListener!!.onFasilClick()
    }

    private fun onListenClick() {
        dialog!!.dismiss()
        ayaPropertiesListener!!.onListenClick()
    }

    fun onTafserClick() {
        dialog!!.dismiss()
        ayaPropertiesListener!!.onTafseerClick()
    }

    private fun onNotesClick() {
        dialog!!.dismiss()
        ayaPropertiesListener!!.onNoteClick()
    }

    fun setAyaHasNote() {
        binding!!.noteIv.setImageResource(R.drawable.notes_gold_sidemenu_ic)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    interface AyaPropertiesListener {
        fun onShareClick()
        fun onFasilClick()
        fun onListenClick()
        fun onTafseerClick()
        fun onNoteClick()
    }

    companion object {
        private val TAG = AyaActionsDialog::class.java.simpleName

        const val ARG_Y_LOCATION = "ARG_Y_LOCATION"
    }
}