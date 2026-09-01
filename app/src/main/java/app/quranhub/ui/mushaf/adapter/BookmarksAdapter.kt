package app.quranhub.ui.mushaf.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.databinding.ItemBookmarkedFavoriteAyaBinding
import app.quranhub.ui.mushaf.listener.ItemSelectionListener
import app.quranhub.ui.mushaf.model.DisplayableBookmark
import app.quranhub.util.LocaleUtils.formatNumber
import java.util.Locale

class BookmarksAdapter(
    private val context: Context, private val bookmarkActionListener: BookmarkActionListener
) : RecyclerView.Adapter<BookmarksAdapter.ViewHolder>(), Filterable {

    private var originalBookmarks: MutableList<DisplayableBookmark>? = null
    private var filteredBookmarks: MutableList<DisplayableBookmark>? = null

    private var bookmarkColors: IntArray = context.resources.getIntArray(R.array.bookmark_colors)

    private var searchText = ""
    private var isEditable = false

    fun setBookmarks(bookmarks: MutableList<DisplayableBookmark>) {
        originalBookmarks = bookmarks
        filteredBookmarks = bookmarks
        notifyDataSetChanged()
    }

    fun setEditable(isEditable: Boolean) {
        this.isEditable = isEditable
        notifyDataSetChanged()
    }

    fun isEditable(): Boolean {
        return isEditable
    }

    fun toggleEdit() {
        setEditable(!isEditable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_bookmarked_favorite_aya, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredBookmarks!![position])
    }

    override fun getItemCount(): Int {
        return if (filteredBookmarks == null) 0 else filteredBookmarks!!.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                var resultBookmarks: MutableList<DisplayableBookmark>? = ArrayList()
                if (constraint.isEmpty()) {
                    resultBookmarks = originalBookmarks
                } else {
                    for (bookmark in originalBookmarks!!) {
                        if (bookmark.ayaContent?.lowercase(Locale.getDefault())?.contains(
                                constraint.toString().lowercase(
                                    Locale.getDefault()
                                )
                            ) == true
                        ) {
                            resultBookmarks!!.add(bookmark)
                        }
                    }
                }
                val results = FilterResults()
                results.values = resultBookmarks
                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                searchText = constraint.toString()
                filteredBookmarks = results.values as? MutableList<DisplayableBookmark>
                notifyDataSetChanged()
            }
        }
    }

    fun deleteBookmark(ayaId: Int) {
        for (displayableBookmark in filteredBookmarks!!) {
            if (displayableBookmark.ayaId == ayaId) {
                originalBookmarks!!.remove(displayableBookmark)
                filteredBookmarks!!.remove(displayableBookmark)
                notifyDataSetChanged()
                break
            }
        }
    }

    fun filterBookmarks(bookmarkType: Int) {
        if (bookmarkType == 0) { // show all bookmark
            filteredBookmarks = originalBookmarks
        } else {
            val filteredList: MutableList<DisplayableBookmark> = ArrayList()
            for (bookmark in originalBookmarks!!) {
                if (bookmark.bookmarkType == bookmarkType) {
                    filteredList.add(bookmark)
                }
                filteredBookmarks = filteredList
            }
        }
        notifyDataSetChanged()
    }

    fun editBookmark(ayaId: Int, bookmarkType: Int, colorIndex: Int) {
        for (displayableBookmark in filteredBookmarks!!) {
            if (displayableBookmark.ayaId == ayaId) {
                displayableBookmark.bookmarkType = bookmarkType
                displayableBookmark.colorIndex = colorIndex
                break
            }
        }
        for (displayableBookmark in originalBookmarks!!) {
            if (displayableBookmark.ayaId == ayaId) {
                displayableBookmark.bookmarkType = bookmarkType
                displayableBookmark.colorIndex = colorIndex
                break
            }
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        var binding: ItemBookmarkedFavoriteAyaBinding

        init {
            binding = ItemBookmarkedFavoriteAyaBinding.bind(view!!)
            attachListeners()
        }

        @SuppressLint("SetTextI18n")
        fun bind(displayableBookmark: DisplayableBookmark) {
            if (displayableBookmark.bookmarkType == Constants.BookmarkType.NOTE) {
                binding.ivBookmarkType.colorFilter = null
                binding.ivBookmarkType.setImageResource(R.drawable.bookmark_green)
            } else if (displayableBookmark.bookmarkType == Constants.BookmarkType.MEMORIZE) {
                binding.ivBookmarkType.colorFilter = null
                binding.ivBookmarkType.setImageResource(R.drawable.bookmark_red)
            } else if (displayableBookmark.bookmarkType == Constants.BookmarkType.RECITING) {
                binding.ivBookmarkType.colorFilter = null
                binding.ivBookmarkType.setImageResource(R.drawable.bookmark_gold)
            } else if (displayableBookmark.bookmarkType == Constants.BookmarkType.FAVORITE) {
                binding.ivBookmarkType.colorFilter = null
                binding.ivBookmarkType.setImageResource(R.drawable.fav_added__gold_ic)
            } else {
                binding.ivBookmarkType.setImageResource(R.drawable.bookmark_gold)
                binding.ivBookmarkType.setColorFilter(bookmarkColors[displayableBookmark.colorIndex])
            }
            binding.tvAyaContent.text = displayableBookmark.ayaContent
            binding.tvAyaNum.text =
                formatNumber(displayableBookmark.suraAyaNumber)
            binding.tvGuz2Num.text = formatNumber(displayableBookmark.guz2Number)
            binding.tvHizbNum.text = formatNumber(displayableBookmark.hizbNumber)
            binding.tvRub3Num.text =
                formatNumber(displayableBookmark.rub3Number)
            binding.tvSuraName.text = displayableBookmark.suraName
            binding.tvSuraName.typeface = Typeface.create(
                Typeface.createFromAsset(
                    context.assets, "fonts/diwany_thuluth.ttf"
                ), Typeface.BOLD
            )
            binding.tvPageNum.text = formatNumber(displayableBookmark.pageNumber)
            if (isEditable) {
                binding.ibDeleteBookmark.visibility = View.VISIBLE
            } else {
                binding.ibDeleteBookmark.visibility = View.GONE
            }
        }

        private fun attachListeners() {
            binding.itemView.setOnClickListener { v: View? -> gotoBookmarkAya() }
            binding.ibDeleteBookmark.setOnClickListener { v: View? -> deleteBookmark() }
            binding.ivBookmarkType.setOnClickListener { v: View? -> displayBookmarkTypeDialog() }
        }

        private fun gotoBookmarkAya() {
            if (!isEditable) {
                bookmarkActionListener.onSelectItem(filteredBookmarks!![adapterPosition])
            }
        }

        private fun deleteBookmark() {
            if (isEditable) {
                Log.d(TAG, "delete bookmark: $adapterPosition")
                bookmarkActionListener.deleteBookmark(filteredBookmarks!![adapterPosition])
            }
        }

        private fun displayBookmarkTypeDialog() {
            if (isEditable) {
                bookmarkActionListener.updateBookmarkType(filteredBookmarks!![adapterPosition].bookmarkId)

                /*BookmarkTypesPopup bookmarkTypesPopup = new BookmarkTypesPopup(context, bookmarkTypeId -> {
                    bookmarkActionListener.updateBookmarkType(filteredBookmarks.get(getAdapterPosition())
                            , bookmarkTypeId);
                });
                bookmarkTypesPopup.showPopup(view);*/
            }
        }
    }

    interface BookmarkActionListener : ItemSelectionListener<DisplayableBookmark?> {
        fun deleteBookmark(displayableBookmark: DisplayableBookmark)
        fun updateBookmarkType(bookmarkId: Int)
    }

    companion object {
        private val TAG = BookmarksAdapter::class.java.simpleName
    }
}