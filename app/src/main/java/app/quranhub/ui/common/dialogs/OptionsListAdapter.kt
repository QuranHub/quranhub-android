package app.quranhub.ui.common.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.R
import app.quranhub.databinding.ItemOptionBinding

// TODO remove this & use the one in first_wizard package, refactor if necessary
class OptionsListAdapter : RecyclerView.Adapter<OptionsListAdapter.ViewHolder> {

    private var optionsList: List<String>
    private var optionsThumbnailsDrawableIds: IntArray? = null
    private var selectedOptionIndex: Int
    private var itemClickListener: ItemClickListener

    constructor(
        optionsList: List<String>, selectedOptionIndex: Int,
        listener: ItemClickListener
    ) {
        this.optionsList = optionsList
        this.selectedOptionIndex = selectedOptionIndex
        itemClickListener = listener
    }

    constructor(
        optionsList: List<String>,
        optionsThumbnailsDrawableIds: IntArray?,
        selectedOptionIndex: Int,
        listener: ItemClickListener
    ) {
        this.optionsList = optionsList
        this.optionsThumbnailsDrawableIds = optionsThumbnailsDrawableIds
        this.selectedOptionIndex = selectedOptionIndex
        itemClickListener = listener
    }

    fun getOptionsList(): List<String> {
        return optionsList
    }

    fun setOptionsList(optionsList: List<String>) {
        this.optionsList = optionsList
        notifyDataSetChanged()
    }

    fun setOptions(optionsList: List<String>, optionsThumbnailsDrawableIds: IntArray?) {
        this.optionsList = optionsList
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
        if (optionsThumbnailsDrawableIds != null) {
            val drawableResId = optionsThumbnailsDrawableIds!![position]
            holder.optionThumbnailImageView.visibility = View.VISIBLE
            holder.optionThumbnailImageView.setImageResource(drawableResId)
        } else {
            holder.optionThumbnailImageView.visibility = View.GONE
        }
        val option = optionsList[position]
        holder.optionNameTextView.text = option
        if (position == selectedOptionIndex) {
            holder.checkBoxImageView.visibility = View.VISIBLE
        } else {
            holder.checkBoxImageView.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return optionsList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var optionThumbnailImageView: ImageView
        var optionNameTextView: TextView
        var checkBoxImageView: ImageView

        init {
            val binding = ItemOptionBinding.bind(itemView)
            optionThumbnailImageView = binding.ivOptionThumbnail
            optionNameTextView = binding.tvOptionName
            checkBoxImageView = binding.ivCheckBox
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