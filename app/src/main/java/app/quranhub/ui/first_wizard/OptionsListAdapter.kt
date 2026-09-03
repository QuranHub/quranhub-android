package app.quranhub.ui.first_wizard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.R
import app.quranhub.databinding.ItemOptionBinding
import java.util.Locale

class OptionsListAdapter : RecyclerView.Adapter<OptionsListAdapter.ViewHolder>, Filterable {

    private var optionsList: MutableList<String>
    private var optionsThumbnailsDrawableIds: IntArray? = null

    /**
     * The indices (into [optionsList]) of the options shown after filtering,
     * so a filtered view position can always be mapped back to its original option.
     */
    private var filteredOriginalIndices: List<Int>
    private var selectedOptionIndex: Int
    private var itemClickListener: ItemClickListener

    constructor(optionsList: MutableList<String>, listener: ItemClickListener) {
        this.optionsList = optionsList
        filteredOriginalIndices = optionsList.indices.toList()
        itemClickListener = listener
        selectedOptionIndex = -1
    }

    constructor(
        optionsList: MutableList<String>, selectedOptionIndex: Int, listener: ItemClickListener
    ) {
        this.optionsList = optionsList
        filteredOriginalIndices = optionsList.indices.toList()
        this.selectedOptionIndex = selectedOptionIndex
        itemClickListener = listener
    }

    constructor(
        optionsList: List<String>,
        optionsThumbnailsDrawableIds: IntArray?,
        selectedOptionIndex: Int,
        listener: ItemClickListener
    ) {
        this.optionsList = optionsList.toMutableList()
        filteredOriginalIndices = optionsList.indices.toList()
        this.optionsThumbnailsDrawableIds = optionsThumbnailsDrawableIds
        this.selectedOptionIndex = selectedOptionIndex
        itemClickListener = listener
    }

    fun getOptionsList(): List<String> {
        return optionsList
    }

    fun setOptionsList(optionsList: MutableList<String>) {
        this.optionsList = optionsList
        filteredOriginalIndices = optionsList.indices.toList()
        notifyDataSetChanged()
    }

    fun setOptions(
        optionsList: MutableList<String>, optionsThumbnailsDrawableIds: IntArray?
    ) {
        this.optionsList = optionsList
        filteredOriginalIndices = optionsList.indices.toList()
        this.optionsThumbnailsDrawableIds = optionsThumbnailsDrawableIds
        notifyDataSetChanged()
    }

    fun getSelectedOptionIndex(): Int {
        return selectedOptionIndex
    }

    fun setSelectedOptionIndex(selectedOptionIndex: Int) {
        this.selectedOptionIndex = selectedOptionIndex
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_option, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val originalIndex = filteredOriginalIndices[position]
        if (optionsThumbnailsDrawableIds != null) {
            holder.binding.ivOptionThumbnail.visibility = View.VISIBLE
            holder.binding.ivOptionThumbnail.setImageResource(
                optionsThumbnailsDrawableIds!![originalIndex]
            )
        } else {
            holder.binding.ivOptionThumbnail.visibility = View.GONE
        }
        holder.binding.tvOptionName.text = optionsList[originalIndex]
        if (originalIndex == selectedOptionIndex) {
            holder.binding.ivCheckBox.visibility = View.VISIBLE
        } else {
            holder.binding.ivCheckBox.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return filteredOriginalIndices.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val query = constraint.toString().lowercase(Locale.getDefault())
                val matchedIndices = if (query.isEmpty()) {
                    optionsList.indices.toList()
                } else {
                    optionsList.withIndex()
                        .filter { (_, option) ->
                            option.lowercase(Locale.getDefault()).contains(query)
                        }
                        .map { it.index }
                }
                val results = FilterResults()
                results.values = matchedIndices
                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                filteredOriginalIndices = results.values as List<Int>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var binding: ItemOptionBinding

        init {
            binding = ItemOptionBinding.bind(itemView)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val originalIndex = filteredOriginalIndices[adapterPosition]
            setSelectedOptionIndex(originalIndex)
            itemClickListener.onItemClick(originalIndex)
        }
    }

    /**
     * Used in handling items clicks
     */
    interface ItemClickListener {
        fun onItemClick(clickedItemIndex: Int)
    }

    companion object {
        private val TAG = OptionsListAdapter::class.java.simpleName
    }
}