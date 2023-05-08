package app.quranhub.ui.first_wizard

import android.util.Pair
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
    private var filteredOptionsList: List<String>
    private var optionsThumbnailsDrawableIds: IntArray? = null
    private var filteredOptionsThumbnailsDrawableIds: IntArray? = null
    private var selectedOptionIndex: Int
    private var itemClickListener: ItemClickListener

    constructor(optionsList: MutableList<String>, listener: ItemClickListener) {
        this.optionsList = optionsList
        filteredOptionsList = optionsList
        itemClickListener = listener
        selectedOptionIndex = -1
    }

    constructor(
        optionsList: MutableList<String>, selectedOptionIndex: Int, listener: ItemClickListener
    ) {
        this.optionsList = optionsList
        filteredOptionsList = optionsList
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
        filteredOptionsList = optionsList
        this.optionsThumbnailsDrawableIds = optionsThumbnailsDrawableIds
        filteredOptionsThumbnailsDrawableIds = optionsThumbnailsDrawableIds
        this.selectedOptionIndex = selectedOptionIndex
        itemClickListener = listener
    }

    fun getOptionsList(): List<String> {
        return optionsList
    }

    fun setOptionsList(optionsList: MutableList<String>) {
        this.optionsList = optionsList
        filteredOptionsList = optionsList
        notifyDataSetChanged()
    }

    fun setOptions(
        optionsList: MutableList<String>, optionsThumbnailsDrawableIds: IntArray?
    ) {
        this.optionsList = optionsList
        filteredOptionsList = optionsList
        this.optionsThumbnailsDrawableIds = optionsThumbnailsDrawableIds
        filteredOptionsThumbnailsDrawableIds = optionsThumbnailsDrawableIds
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
        if (filteredOptionsThumbnailsDrawableIds != null) {
            val drawableResId = filteredOptionsThumbnailsDrawableIds!![position]
            holder.binding.ivOptionThumbnail.visibility = View.VISIBLE
            holder.binding.ivOptionThumbnail.setImageResource(drawableResId)
        } else {
            holder.binding.ivOptionThumbnail.visibility = View.GONE
        }
        val option = filteredOptionsList[position]
        holder.binding.tvOptionName.text = option
        if (position == selectedOptionIndex) {
            holder.binding.ivCheckBox.visibility = View.VISIBLE
        } else {
            holder.binding.ivCheckBox.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return filteredOptionsList.size
    }

    override fun getFilter(): Filter {
        // TODO refactor & enhance Filter
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                var filterOptionsResult: MutableList<String> = ArrayList()
                var filterThumbnailsResult: MutableList<Int?>? = null
                if (constraint.length == 0) {
                    filterOptionsResult = optionsList
                    if (optionsThumbnailsDrawableIds != null) {
                        filterThumbnailsResult = ArrayList()
                        for (d in optionsThumbnailsDrawableIds!!) {
                            filterThumbnailsResult.add(d)
                        }
                    }
                } else {
                    if (optionsThumbnailsDrawableIds != null) {
                        filterThumbnailsResult = ArrayList()
                    }
                    for (i in optionsList.indices) {
                        val opt = optionsList[i]
                        if (opt.lowercase(Locale.getDefault()).contains(
                                constraint.toString().lowercase(
                                    Locale.getDefault()
                                )
                            )
                        ) {
                            filterOptionsResult.add(opt)
                            filterThumbnailsResult?.add(optionsThumbnailsDrawableIds!![i])
                        }
                    }
                }
                val results = FilterResults()
                results.values =
                    Pair<List<String>, List<Int?>?>(filterOptionsResult, filterThumbnailsResult)
                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                val filterResult = results.values as Pair<List<String>, List<Int>?>
                filteredOptionsList = filterResult.first
                if (filterResult.second != null) {
                    filteredOptionsThumbnailsDrawableIds = IntArray(filterResult.second!!.size)
                    for (i in filterResult.second!!.indices) {
                        filteredOptionsThumbnailsDrawableIds!![i] = filterResult.second!![i]
                    }
                } else {
                    filteredOptionsThumbnailsDrawableIds = null
                }
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
            setSelectedOptionIndex(adapterPosition)
            itemClickListener.onItemClick(selectedOptionIndex)
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