package app.quranhub.ui.mushaf.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.R
import app.quranhub.databinding.DialogSuraListBinding
import app.quranhub.ui.mushaf.adapter.FilterAdapter
import app.quranhub.ui.mushaf.adapter.FilterAdapter.OptionClickListener
import app.quranhub.util.DialogUtils.adjustDialogSize

class OptionDialog : DialogFragment(), OptionClickListener {

    private var dialog: Dialog? = null
    private var listener: ItemClickListener? = null
    private var suraName: String? = null
    private var adapter: FilterAdapter? = null
    private var options: ArrayList<String>? = null
    private var requestCode = 0

    private var binding: DialogSuraListBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? ItemClickListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSuraListBinding.inflate(layoutInflater)
        initializeDialog()
        setRecyclerList()
        observeOnInputSearch()
        return dialog!!
    }

    override fun onResume() {
        super.onResume()

        //DialogUtils.adjustDialogSize(this);
        adjustDialogSize(
            this, 0.8f, 0.7f, 0.5f, 0.9f
        )
    }

    private fun observeOnInputSearch() {
        binding!!.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                adapter!!.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun setRecyclerList() {
        adapter = FilterAdapter(options!!, suraName!!, this, requestCode)
        binding!!.suraRv.layoutManager = LinearLayoutManager(activity)
        binding!!.suraRv.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        binding!!.suraRv.adapter = adapter
    }

    private fun initializeDialog() {
        dialog = Dialog(requireActivity())
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setContentView(binding!!.root)
        dialog!!.window?.setBackgroundDrawableResource(R.color.transparent_color)
        arguments?.let {
            suraName = it.getString(SURA_NAME_ARGS)
            options = it.getStringArrayList(ALL_ITEMS_ARGS)
            requestCode = it.getInt(CODE_ARGS, 1)
            binding!!.tvTitle.text = it.getString(HEADER_ARGS)
        }
    }

    override fun onOptionClick(suraName: String, suraIndex: Int) {
        listener!!.onItemClick(suraName, suraIndex, requestCode)
        dialog!!.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    interface ItemClickListener {
        fun onItemClick(optionName: String?, optionIndex: Int, requestCode: Int)
    }

    companion object {

        const val SURA_NAME_ARGS = "SURA_NAME_ARGS"
        const val ALL_ITEMS_ARGS = "ALL_ITEMS_ARGS"
        const val CODE_ARGS = "CODE_ARGS"
        const val HEADER_ARGS = "HEADER_ARGS"

        @JvmStatic
        fun getInstance(
            options: List<String?>?,
            suraName: String?,
            requestCode: Int,
            headerText: String?
        ): DialogFragment {
            val fragment: DialogFragment = OptionDialog()
            val bundle = Bundle()
            bundle.putString(SURA_NAME_ARGS, suraName)
            bundle.putStringArrayList(ALL_ITEMS_ARGS, options as ArrayList<String?>?)
            bundle.putInt(CODE_ARGS, requestCode)
            bundle.putString(HEADER_ARGS, headerText)
            fragment.arguments = bundle
            return fragment
        }

        fun getInstance(
            options: List<String?>?,
            suraName: String?,
            headerText: String?
        ): DialogFragment {
            val fragment: DialogFragment = OptionDialog()
            val bundle = Bundle()
            bundle.putString(SURA_NAME_ARGS, suraName)
            bundle.putStringArrayList(ALL_ITEMS_ARGS, options as ArrayList<String?>?)
            bundle.putString(HEADER_ARGS, headerText)
            fragment.arguments = bundle
            return fragment
        }
    }
}