package app.quranhub.ui.mushaf.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.R
import app.quranhub.data.local.entity.Book
import app.quranhub.data.remote.model.BookContent
import app.quranhub.databinding.ItemBookBinding
import app.quranhub.ui.mushaf.listener.ItemSelectionListener
import app.quranhub.util.LocaleUtils.appLanguage
import java.util.Locale

class BookAdapter(private val translationActionsListener: TranslationActionsListener) :
    RecyclerView.Adapter<BookAdapter.ViewHolder>() {

    private var bookList: MutableList<BookContent>
    private var translationFilterList: MutableList<BookContent>

    private var isEditable = false

    init {
        bookList = ArrayList()
        translationFilterList = ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = translationFilterList[position]
        holder.binding.translationTv.text = model.name
        if (isEditable) {
            holder.binding.translationIv.setImageResource(R.drawable.ic_delete)
            changeIconType(false, holder)
        } else if (model.downloadStatus == TRANSLATION_DOWNLOADED) {
            holder.binding.translationIv.setImageResource(R.drawable.check_gold_ic)
            changeIconType(false, holder)
        } else if (model.downloadStatus == TRANSLATION_NOT_DOWNLOADED) {
            holder.binding.translationIv.setImageResource(R.drawable.ic_download)
            changeIconType(false, holder)
        } else if (model.downloadStatus == TRANSLATION_DOWNLOADED_IN_PROGRESS) {
            changeIconType(true, holder)
        }
        if (appLanguage != "ar") {
            holder.binding.translationTv.gravity = Gravity.LEFT
        }
        holder.binding.translationTv.setOnClickListener { v: View? ->
            if (model.downloadStatus == TRANSLATION_NOT_DOWNLOADED) {                             // open file in pdf if it exist in local storage and downloaded before
                translationActionsListener.onDownloadTranslation(model)
            } else if (model.downloadStatus == TRANSLATION_DOWNLOADED) {
                translationActionsListener.onSelectItem(model)
            }
        }
        holder.binding.downloadProgress.setOnClickListener { v: View? ->
            translationActionsListener.onCancelDownload(
                model
            )
        }
        holder.binding.translationIv.setOnClickListener { v: View? ->
            if (isEditable) {
                translationActionsListener.onDeleteTranslation(model)
            } else if (model.downloadStatus == TRANSLATION_NOT_DOWNLOADED) {
                translationActionsListener.onDownloadTranslation(model)
            }
        }
    }

    private fun changeIconType(isDownloadProgress: Boolean, holder: ViewHolder) {
        if (isDownloadProgress) {
            holder.binding.cancelDownload.visibility = View.VISIBLE
            holder.binding.downloadProgress.visibility = View.VISIBLE
            holder.binding.translationIv.visibility = View.INVISIBLE
        } else {
            holder.binding.cancelDownload.visibility = View.INVISIBLE
            holder.binding.downloadProgress.visibility = View.INVISIBLE
            holder.binding.translationIv.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return translationFilterList.size
    }

    fun setBookList(bookList: MutableList<BookContent>) {
        this.bookList = bookList
        translationFilterList = bookList
        notifyDataSetChanged()
    }

    fun filter(inputQuery: String) {
        if (inputQuery.isEmpty()) {
            if (!isEditable) translationFilterList = bookList else setDownloadTranslations()
        } else {
            val filteredList: MutableList<BookContent> = ArrayList()
            for (row in bookList) {
                if (isEditable && row.downloadStatus != TRANSLATION_DOWNLOADED) continue
                if (row.name.lowercase(Locale.getDefault())
                        .contains(inputQuery.lowercase(Locale.getDefault()))
                ) {
                    filteredList.add(row)
                }
            }
            translationFilterList = filteredList
        }
        notifyDataSetChanged()
    }

    fun setDownloadTranslations() {
        isEditable = true
        val filteredList: MutableList<BookContent> = ArrayList()
        for (model in bookList) {
            if (model.downloadStatus == TRANSLATION_DOWNLOADED) filteredList.add(model)
        }
        translationFilterList = filteredList
        notifyDataSetChanged()
    }

    fun setAllTranslation() {
        isEditable = false
        translationFilterList = bookList
        notifyDataSetChanged()
    }

    fun removeDeletedFile(id: Int) {
        for (model in translationFilterList) {
            if (model.id == id) {
                translationFilterList.remove(model)
                break
            }
        }
        for (model in bookList) {
            if (model.id == id) {
                model.downloadStatus = TRANSLATION_NOT_DOWNLOADED
                break
            }
        }
        notifyDataSetChanged()
    }

    fun updateBooksDownloadStatus(models: List<Book>) {
        for (book in models) {
            for (content in translationFilterList) {
                if (book.id == content.id) {
                    content.downloadId = book.downloadId
                    content.downloadStatus = book.downloadStatus
                    break
                }
            }
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemBookBinding

        init {
            binding = ItemBookBinding.bind(itemView)
        }
    }

    interface TranslationActionsListener : ItemSelectionListener<BookContent?> {
        fun onCancelDownload(model: BookContent?)
        fun onDownloadTranslation(model: BookContent?)
        fun onDeleteTranslation(model: BookContent?)
    }

    companion object {
        const val TRANSLATION_NOT_DOWNLOADED = 0
        const val TRANSLATION_DOWNLOADED_IN_PROGRESS = 1
        const val TRANSLATION_DOWNLOADED = 2
    }
}