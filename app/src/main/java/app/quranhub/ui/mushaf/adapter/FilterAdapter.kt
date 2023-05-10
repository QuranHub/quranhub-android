package app.quranhub.ui.mushaf.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.R
import app.quranhub.databinding.ItemOptionBinding
import java.util.Locale

class FilterAdapter(
    private val optionsList: List<String>,
    selectedOption: String,
    listener: OptionClickListener,
    requestCode: Int
) : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    private var filteredOptionsList: List<String>
    private var selectedOption: String
    private val itemClickListener: OptionClickListener
    private val requestCode: Int

    init {
        filteredOptionsList = optionsList
        this.selectedOption = selectedOption
        itemClickListener = listener
        this.requestCode = requestCode
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_option, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = filteredOptionsList[position]
        holder.binding.tvOptionName.text = option
        if (option == selectedOption) {
            holder.binding.ivCheckBox.visibility = View.VISIBLE
        } else {
            holder.binding.ivCheckBox.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return filteredOptionsList.size
    }

    fun filter(inputQuery: String) {
        filteredOptionsList = if (inputQuery.isEmpty()) {
            optionsList
        } else {
            val filteredList: MutableList<String> = ArrayList()
            for (row in optionsList) {
                if (row.lowercase(Locale.getDefault())
                        .contains(inputQuery.lowercase(Locale.getDefault()))
                ) {
                    filteredList.add(row)
                }
            }
            filteredList
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemOptionBinding

        init {
            binding = ItemOptionBinding.bind(itemView)
            attachListeners()
        }

        private fun attachListeners() {
            binding.tvOptionName.setOnClickListener { v: View? -> onClickSura() }
        }

        private fun onClickSura() {
            selectedOption = binding.tvOptionName.text.toString()
            notifyDataSetChanged()
            getSelectedOptionIndex(binding.tvOptionName.text.toString())
        }
    }

    // get selected sura index in original list (not filtered list)
    private fun getSelectedOptionIndex(option: String) {
        var selectedIndex = 0
        for (i in optionsList.indices) {
            if (optionsList[i] == option) {
                selectedIndex = i
                break
            }
        }
        itemClickListener.onOptionClick(option, selectedIndex)
    }

    interface OptionClickListener {
        fun onOptionClick(optionName: String, optionIndex: Int)
    }
}