package app.quranhub.ui.mushaf.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.R
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.databinding.ItemTranslationBinding
import app.quranhub.ui.mushaf.model.DisplayableTranslation
import app.quranhub.util.NetworkUtil
import java.util.Locale

class TranslationsAdapter(
    translations: MutableList<DisplayableTranslation>?,
    selectedBookId: String?,
    listener: ItemClickListener
) : RecyclerView.Adapter<TranslationsAdapter.ViewHolder>(), Filterable {

    private var originalTranslations: MutableList<DisplayableTranslation>?
    private var filteredTranslations: List<DisplayableTranslation>?

    private var searchText = ""
    private val selectedBookId: String?
    private val listener: ItemClickListener

    init {
        sortTranslationList(translations)
        originalTranslations = translations
        filteredTranslations = translations
        this.selectedBookId = selectedBookId
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_translation, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val t = filteredTranslations!![position]
        holder.binding.tvBookName.text = t.name
        holder.binding.tvAuthorName.text = t.author
        if (t.downloadStatus == NetworkUtil.STATUS_DOWNLOADED && t.id == selectedBookId) {
            holder.binding.ivSelected.visibility = View.VISIBLE
        } else {
            holder.binding.ivSelected.visibility = View.INVISIBLE
        }
        if (t.downloadStatus == NetworkUtil.STATUS_DOWNLOADED) {
            holder.binding.btnAction.visibility = View.INVISIBLE
            holder.binding.progressDownload.visibility = View.INVISIBLE
            holder.binding.progressDownloadLevel.visibility = View.INVISIBLE
        } else if (t.downloadStatus == NetworkUtil.STATUS_DOWNLOADING) {
            holder.binding.btnAction.visibility = View.VISIBLE
            holder.binding.btnAction.setImageResource(R.drawable.ic_close)
            holder.binding.progressDownload.visibility = View.VISIBLE
            holder.binding.progressDownloadLevel.visibility = View.VISIBLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.binding.progressDownloadLevel.setProgress(t.downloadLevelPercentage, true)
            } else {
                holder.binding.progressDownloadLevel.progress = t.downloadLevelPercentage
            }
        } else {
            // not downloaded
            holder.binding.btnAction.visibility = View.VISIBLE
            holder.binding.btnAction.setImageResource(R.drawable.ic_download)
            holder.binding.progressDownload.visibility = View.INVISIBLE
            holder.binding.progressDownloadLevel.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return if (filteredTranslations != null) filteredTranslations!!.size else 0
    }

    fun setTranslations(translations: MutableList<DisplayableTranslation>?) {
        sortTranslationList(translations)
        originalTranslations = translations
        filter.filter(searchText)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                var resultTranslations: MutableList<DisplayableTranslation>? = ArrayList()
                if (constraint.isEmpty()) {
                    resultTranslations = originalTranslations
                } else {
                    for (t in originalTranslations!!) {
                        if (t.name.lowercase(Locale.getDefault()).contains(
                                constraint.toString().lowercase(
                                    Locale.getDefault()
                                )
                            )
                            || t.author.lowercase(Locale.getDefault()).contains(
                                constraint.toString().lowercase(
                                    Locale.getDefault()
                                )
                            )
                        ) {
                            resultTranslations!!.add(t)
                        }
                    }
                }
                val results = FilterResults()
                results.values = resultTranslations
                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                searchText = constraint.toString()
                filteredTranslations = results.values as? List<DisplayableTranslation>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var binding: ItemTranslationBinding

        init {
            binding = ItemTranslationBinding.bind(itemView)
            attachListeners()
        }

        private fun attachListeners() {
            binding.root.setOnClickListener(this)
            binding.btnAction.setOnClickListener { v: View? -> onActionButtonClicked() }
        }

        override fun onClick(v: View) {
            val position = adapterPosition
            val t = filteredTranslations!![position]
            if (t.downloadStatus == NetworkUtil.STATUS_DOWNLOADED) {
                listener.onTranslationClick(t.translationBook, adapterPosition)
            }
        }

        private fun onActionButtonClicked() {
            val position = adapterPosition
            val t = filteredTranslations!![position]
            if (t.downloadStatus == NetworkUtil.STATUS_NOT_DOWNLOADED) {
                listener.onDownloadTranslationClick(t.translationBook, position)
            } else if (t.downloadStatus == NetworkUtil.STATUS_DOWNLOADING) {
                listener.onCancelDownloadTranslationClick(t.translationBook, position)
            }
        }
    }

    private fun sortTranslationList(displayableTranslations: MutableList<DisplayableTranslation>?) {
        displayableTranslations?.sortWith { o1: DisplayableTranslation, o2: DisplayableTranslation ->
            o1.name.compareTo(
                o2.name,
                ignoreCase = true
            )
        }
    }

    /**
     * Used in handling items clicks
     */
    interface ItemClickListener {
        fun onTranslationClick(translationBook: TranslationBook?, clickedItemIndex: Int)
        fun onDownloadTranslationClick(translationBook: TranslationBook?, clickedItemIndex: Int)
        fun onCancelDownloadTranslationClick(
            translationBook: TranslationBook?,
            clickedItemIndex: Int
        )
    }

    companion object {
        private val TAG = TranslationsAdapter::class.java.simpleName
    }
}