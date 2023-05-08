package app.quranhub.ui.mushaf.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.R
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.databinding.DialogAddBookmarkBinding
import app.quranhub.ui.mushaf.adapter.BookmarkTypeAdapter
import app.quranhub.ui.mushaf.listener.ItemSelectionListener
import app.quranhub.util.DialogUtils.wrapDialogHeight

class AddBookmarkDialog : DialogFragment(), ItemSelectionListener<Int> {

    private var binding: DialogAddBookmarkBinding? = null
    private var dialog: Dialog? = null
    private var listener: AddBookmarkListener? = null
    private var selectedType = 0
    private var bookmarkTypes: List<BookmarkType>? = null
    private var isAddCustom = false
    private var adapter: BookmarkTypeAdapter? = null
    private var colorIndex = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            parentFragment as AddBookmarkListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(
                "The parent fragment of BookmarkEditDialog (${requireParentFragment().javaClass.simpleName}) must implement the BookmarkFilterListener interface"
            )
        }
    }

    override fun onResume() {
        super.onResume()
        wrapDialogHeight(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddBookmarkBinding.inflate(layoutInflater)
        args
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

    private val args: Unit
        get() {
            arguments?.let {
                bookmarkTypes = it.getParcelableArrayList(BOOKMARK_TYPES_ARGS)
                if (!it.getBoolean(IS_EDITABLE)) {
                    binding!!.addCustomGroup.visibility = View.GONE
                    binding!!.customBookmarkGroup.visibility = View.GONE
                    binding!!.btnShow.text = getString(R.string.show)
                }
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
                val type = BookmarkType(
                    bookmarkTypes!!.size + 1, binding!!.bookmarkTitleEt.text.toString(), colorIndex
                )
                listener!!.addCustomBookmark(type)
                dismiss()
            }
        } else {
            listener!!.addNormalBookmark(selectedType)
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

    interface AddBookmarkListener {
        fun addNormalBookmark(bookmarkType: Int)
        fun addCustomBookmark(type: BookmarkType?)
    }

    companion object {
        private const val BOOKMARK_TYPES_ARGS = "BOOKMARK_TYPES_ARGS"

        private const val IS_EDITABLE = "IS_EDITABLE"

        fun getInstance(types: List<BookmarkType?>?, isEditable: Boolean): AddBookmarkDialog {
            val bundle = Bundle()
            bundle.putParcelableArrayList(BOOKMARK_TYPES_ARGS, types as ArrayList<out Parcelable?>?)
            bundle.putBoolean(IS_EDITABLE, isEditable)
            val dialog = AddBookmarkDialog()
            dialog.arguments = bundle
            return dialog
        }
    }
}