package app.quranhub.ui.mushaf.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.R
import app.quranhub.databinding.DialogNoteFilterBinding
import app.quranhub.ui.mushaf.adapter.FilterAdapter
import app.quranhub.ui.mushaf.adapter.FilterAdapter.OptionClickListener
import app.quranhub.ui.mushaf.listener.ItemSelectionListener
import java.util.Arrays

class NotesFilterDialog : DialogFragment(), OptionClickListener {

    private var dialog: Dialog? = null
    private var listener: ItemSelectionListener<Int>? = null
    private var selectedOption = 0
    private var adapter: FilterAdapter? = null
    private var options: Array<String> = arrayOf()
    private var binding: DialogNoteFilterBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? ItemSelectionListener<Int>
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogNoteFilterBinding.inflate(layoutInflater)
        initializeDialog()
        setFilterOptions()
        initViews()
        return dialog!!
    }

    private fun setFilterOptions() {
        options = arrayOf(
            getString(R.string.all_types),
            getString(R.string.general_comment),
            getString(R.string.momerize_mistake),
            getString(R.string.tajweed_mistake)
        )
    }

    private fun initViews() {
        binding!!.noteFilterRv.layoutManager = LinearLayoutManager(activity)
        adapter = FilterAdapter(Arrays.asList(*options), options[selectedOption], this, 0)
        binding!!.noteFilterRv.adapter = adapter
    }

    fun initializeDialog() {
        dialog = Dialog(requireActivity())
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setContentView(binding!!.root)
        dialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
        arguments?.let {
            selectedOption = it.getInt(NOTE_TYPE_ARGS)
        }
    }

    /*@OnClick(R.id.btn_back)
    public void onClickBack() {
        dismiss();
    }

    @OnClick(R.id.btn_show)
    public void onShowFilter() {
        listener.onSelectItem(selectedOption);
        dismiss();
    }*/

    override fun onOptionClick(optionName: String, optionIndex: Int) {
        //selectedOption = optionIndex;
        listener!!.onSelectItem(optionIndex)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {

        private const val NOTE_TYPE_ARGS = "NOTE_TYPE_ARGS"

        @JvmStatic
        fun getInstance(type: Int): NotesFilterDialog {
            val bundle = Bundle()
            bundle.putInt(NOTE_TYPE_ARGS, type)
            val dialog = NotesFilterDialog()
            dialog.arguments = bundle
            return dialog
        }
    }
}