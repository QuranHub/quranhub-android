package app.quranhub.ui.mushaf.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.R
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.databinding.DialogBookmarkFilterBinding
import app.quranhub.ui.mushaf.adapter.BookmarkTypeAdapter
import app.quranhub.ui.mushaf.listener.ItemSelectionListener
import app.quranhub.util.DialogUtils.adjustDialogSize

class BookmarkEditDialog : DialogFragment(), ItemSelectionListener<Int> {

    private var dialog: Dialog? = null
    private var listener: BookmarkFilterListener? = null
    private var selectedFilter = 0
    private var bookmarkColorIndex = 0
    private var adapter: BookmarkTypeAdapter? = null
    private var bookmarkTypes: List<BookmarkType>? = null
    private var editDialog = false
    private var binding: DialogBookmarkFilterBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            parentFragment as BookmarkFilterListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(
                "The parent fragment of BookmarkEditDialog (${requireParentFragment().javaClass.simpleName}) must implement the BookmarkFilterListener interface"
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogBookmarkFilterBinding.inflate(layoutInflater)
        readArgs()
        initializeDialog()
        setDialogTypeViews()
        return dialog!!
    }

    private fun setDialogTypeViews() {
        if (editDialog) {
            binding!!.btnShow.text = getString(R.string.edit)
            binding!!.allBookmarkCheckbox.visibility = View.GONE
            binding!!.allBookmark.visibility = View.GONE
        }
    }

    private fun readArgs() {
        arguments?.let {
            selectedFilter = it.getInt(FILTER_TYPE, 0)
            bookmarkTypes = it.getParcelableArrayList(BOOKMARK_TYPES_ARGS)
            editDialog = it.getBoolean(DIALOG_TYPE)
        }
    }

    override fun onResume() {
        super.onResume()
        adjustDialogSize(this)
    }

    fun initializeDialog() {
        dialog = Dialog(requireActivity())
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setContentView(binding!!.root)
        dialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
        adapter = BookmarkTypeAdapter(bookmarkTypes, requireContext(), this)
        binding!!.bookmarkTypesRv.layoutManager = LinearLayoutManager(activity)
        binding!!.bookmarkTypesRv.adapter = adapter
        if (selectedFilter == 0) {
            adapter!!.hideCheck()
        } else {
            binding!!.allBookmarkCheckbox.visibility = View.GONE
            adapter!!.setTypeCheck(selectedFilter)
        }
        attachListeners()
    }

    private fun attachListeners() {
        binding!!.allBookmark.setOnClickListener { onSelectAllBookmark() }
        binding!!.btnShow.setOnClickListener { onShowFilterList() }
        binding!!.btnBack.setOnClickListener { onBackDialog() }
    }

    private fun onSelectAllBookmark() {
        binding!!.allBookmarkCheckbox.visibility = View.VISIBLE
        adapter!!.hideCheck()
        selectedFilter = ALL_BOOKMARK_FILTER
    }

    private fun onShowFilterList() {
        listener!!.onBookmarkFilter(selectedFilter, bookmarkColorIndex)
        dialog!!.dismiss()
    }

    private fun onBackDialog() {
        dialog!!.dismiss()
    }

    override fun onSelectItem(bookmarkType: Int) {
        binding!!.allBookmarkCheckbox.visibility = View.GONE
        selectedFilter = bookmarkType
        bookmarkColorIndex = bookmarkTypes!![selectedFilter - 1].colorIndex
    }

    interface BookmarkFilterListener {
        fun onBookmarkFilter(filter: Int, colorIndex: Int)
    }

    companion object {
        private const val BOOKMARK_TYPES_ARGS = "BOOKMARK_TYPES_ARGS"
        private const val FILTER_TYPE = "FILTER_TYPE"
        private const val DIALOG_TYPE = "DIALOG_TYPE"
        const val ALL_BOOKMARK_FILTER = 0

        @JvmStatic
        fun getInstance(
            types: List<BookmarkType?>?,
            type: Int,
            editDialog: Boolean
        ): BookmarkEditDialog {
            val bundle = Bundle()
            bundle.putParcelableArrayList(BOOKMARK_TYPES_ARGS, types as ArrayList<out Parcelable?>?)
            bundle.putBoolean(DIALOG_TYPE, editDialog)
            bundle.putInt(FILTER_TYPE, type)
            val dialog = BookmarkEditDialog()
            dialog.arguments = bundle
            return dialog
        }
    }
}