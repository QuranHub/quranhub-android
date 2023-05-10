package app.quranhub.ui.mushaf.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.R
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.databinding.BookmarkTypeItemBinding
import app.quranhub.ui.mushaf.listener.ItemSelectionListener

class BookmarkTypeAdapter(
    private val bookmarkTypes: List<BookmarkType>?,
    private val context: Context,
    private val listener: ItemSelectionListener<Int>
) : RecyclerView.Adapter<BookmarkTypeAdapter.ViewHolder>() {

    private var selectedType = 0

    private val bookmarkColors: IntArray = context.resources.getIntArray(R.array.bookmark_colors)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bookmark_type_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val type = bookmarkTypes!![position]
        when (position) {
            0 -> {
                holder.binding.typeIv.colorFilter = null
                holder.binding.typeIv.setImageResource(R.drawable.fav_gold_sidemenu_ic)
                holder.binding.typeTv.text = context.getString(R.string.fasil_favorite)
                holder.binding.seperator1.visibility = View.VISIBLE
            }

            1 -> {
                holder.binding.typeIv.colorFilter = null
                holder.binding.typeIv.setImageResource(R.drawable.bookmark_gold)
                holder.binding.typeTv.text = context.getString(R.string.fasil_read)
            }

            2 -> {
                holder.binding.typeIv.colorFilter = null
                holder.binding.typeIv.setImageResource(R.drawable.bookmark_green)
                holder.binding.typeTv.text = context.getString(R.string.fasil_note)
            }

            3 -> {
                holder.binding.typeIv.colorFilter = null
                holder.binding.typeIv.setImageResource(R.drawable.bookmark_red)
                holder.binding.typeTv.text = context.getString(R.string.fasil_memorize)
            }

            else -> {  // CUSTOM BOOKMARK
                holder.binding.typeIv.setColorFilter(bookmarkColors[type.colorIndex])
                holder.binding.typeTv.text = type.bookmarkTypeName
            }
        }
        if (position > 0) {
            holder.binding.seperator1.visibility = View.INVISIBLE
        }
        if (position == selectedType) {
            holder.binding.checkIv.visibility = View.VISIBLE
        } else {
            holder.binding.checkIv.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return bookmarkTypes?.size ?: 0
    }

    fun hideCheck() {
        selectedType = -1
        notifyDataSetChanged()
    }

    fun setTypeCheck(selectedFilter: Int) {
        selectedType = selectedFilter - 1
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: BookmarkTypeItemBinding

        init {
            binding = BookmarkTypeItemBinding.bind(itemView)
            attachListeners()
        }

        private fun attachListeners() {
            binding.itemContainer.setOnClickListener { v: View? -> onClickItem() }
        }

        private fun onClickItem() {
            selectedType = adapterPosition
            notifyDataSetChanged()
            listener.onSelectItem(selectedType + 1)
        }
    }
}