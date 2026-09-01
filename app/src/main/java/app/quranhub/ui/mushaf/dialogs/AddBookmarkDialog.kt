package app.quranhub.ui.mushaf.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.R
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.databinding.DialogAddBookmarkBinding
import app.quranhub.ui.mushaf.adapter.BookmarkTypeAdapter
import app.quranhub.ui.mushaf.listener.ItemSelectionListener
import app.quranhub.ui.mushaf.viewmodel.QuranPageViewModel
import app.quranhub.util.DialogUtils.wrapDialogHeight

class AddBookmarkDialog : DialogFragment(), ItemSelectionListener<Int> {

    private var binding: DialogAddBookmarkBinding? = null
    private var dialog: Dialog? = null
    private var selectedType = 0
    private var bookmarkTypes: List<BookmarkType>? = null
    private var isAddCustom = false
    private var adapter: BookmarkTypeAdapter? = null
    private var colorIndex = 0

    // Shares the host page's ViewModel (UI shell over one ViewModel per feature)
    private val viewModel: QuranPageViewModel by lazy {
        ViewModelProvider(requireParentFragment())[QuranPageViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        wrapDialogHeight(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddBookmarkBinding.inflate(layoutInflater)
        bookmarkTypes = viewModel.uiState.value.bookmarkTypes
        initializeDialog()
        observeOnSelectedColor()
        return dialog!!
    }

    private fun observeOnSelectedColor() {
        val colors = requireActivity().resources.getIntArray(R.array.bookmark_colors)
        binding!!.palette.setSelectedColor(colors[0])
        binding!!.palette.setOnColorSelectedListener { color: Int ->
            for (i in colors.indices) {
                if (color == colors[i]) {
                    colorIndex = i
                    break
                }
            }
            Log.d("yy8", "observeOnSelectedColor: $color")
        }
    }

    fun initializeDialog() {
        dialog = Dialog(requireActivity())
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setContentView(binding!!.root)
        dialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
        adapter = BookmarkTypeAdapter(bookmarkTypes, requireActivity(), this)
        binding!!.bookmarkTypesRv.layoutManager = LinearLayoutManager(activity)
        binding!!.bookmarkTypesRv.adapter = adapter
        selectedType = 1
        attachListeners()
    }

    private fun attachListeners() {
        binding!!.addCustomGroup.setOnClickListener { onAddCustomBookmark() }
        binding!!.btnShow.setOnClickListener { onShowFilterList() }
        binding!!.btnBack.setOnClickListener { onBackDialog() }
    }

    private fun onAddCustomBookmark() {
        binding!!.addCustomCheckIv.visibility = View.VISIBLE
        binding!!.customBookmarkGroup.visibility = View.VISIBLE
        adapter!!.hideCheck()
        isAddCustom = true
    }

    private fun onShowFilterList() {
        if (isAddCustom) {
            if (binding!!.bookmarkTitleEt.text.toString().isEmpty()) {
                Toast.makeText(
                    activity,
                    getString(R.string.enter_bookmark_title),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                viewModel.addCustomBookmark(
                    binding!!.bookmarkTitleEt.text.toString(), colorIndex
                )
                dismiss()
            }
        } else {
            viewModel.insertAyaBookmark(selectedType)
            dismiss()
        }
    }

    private fun onBackDialog() {
        dialog!!.dismiss()
    }

    override fun onSelectItem(type: Int) {
        selectedType = type
        isAddCustom = false
        binding!!.addCustomCheckIv.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}