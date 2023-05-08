package app.quranhub.ui.downloads_manager.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.R
import app.quranhub.databinding.ItemDownloadBinding
import app.quranhub.ui.downloads_manager.adapters.DownloadsAdapter
import app.quranhub.ui.downloads_manager.model.DisplayableDownload

class DownloadsAdapter : RecyclerView.Adapter<DownloadsAdapter.ViewHolder> {

    private var displayableDownloads: List<DisplayableDownload>
    private var clickListener: ItemClickListener
    private var edit = false

    constructor(
        displayableDownloads: List<DisplayableDownload>,
        clickListener: ItemClickListener
    ) {
        this.displayableDownloads = displayableDownloads
        this.clickListener = clickListener
    }

    constructor(
        displayableDownloads: List<DisplayableDownload>,
        clickListener: ItemClickListener, edit: Boolean
    ) {
        this.displayableDownloads = displayableDownloads
        this.clickListener = clickListener
        this.edit = edit
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_download, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(displayableDownloads[position])
    }

    override fun getItemCount(): Int {
        return displayableDownloads.size
    }

    fun getDisplayableDownloads(): List<DisplayableDownload> {
        return displayableDownloads
    }

    fun setDisplayableDownloads(displayableDownloads: List<DisplayableDownload>) {
        this.displayableDownloads = displayableDownloads
        notifyDataSetChanged()
    }

    fun isEdit(): Boolean {
        return edit
    }

    fun setEdit(edit: Boolean) {
        this.edit = edit
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(
        itemView!!
    ) {
        var binding: ItemDownloadBinding

        init {
            binding = ItemDownloadBinding.bind(itemView!!)
            attachClickListeners()
        }

        fun bind(displayableDownload: DisplayableDownload) {
            binding.tvName.text = displayableDownload.name
            if (displayableDownload.downloadedAmount != null) {
                binding.tvDownloadedAmount.text = displayableDownload.downloadedAmount
            }
            if (edit) {  // edit mode
                if (displayableDownload.isDeletable) {
                    binding.ibAction.setImageResource(R.drawable.ic_delete)
                    binding.ibAction.visibility = View.VISIBLE
                } else {
                    binding.ibAction.visibility = View.INVISIBLE
                }
            } else {  // download mode
                if (displayableDownload.isDownloadable) {
                    binding.ibAction.setImageResource(R.drawable.ic_download)
                    binding.ibAction.visibility = View.VISIBLE
                } else {
                    binding.ibAction.visibility = View.INVISIBLE
                }
            }
        }

        private fun attachClickListeners() {
            binding.llContent.setOnClickListener { v: View? -> onContentClick() }
            binding.ibAction.setOnClickListener { v: View? -> onActionButtonClick() }
        }

        private fun onContentClick() {
            val clickedDisplayableDownload = displayableDownloads[adapterPosition]
            clickListener.onClickItem(clickedDisplayableDownload, adapterPosition)
        }

        private fun onActionButtonClick() {
            val clickedDisplayableDownload = displayableDownloads[adapterPosition]
            if (edit) {
                clickListener.onDeleteItem(clickedDisplayableDownload, adapterPosition)
            } else {
                clickListener.onDownloadItem(clickedDisplayableDownload, adapterPosition)
            }
        }
    }

    interface ItemClickListener {
        fun onClickItem(displayableDownload: DisplayableDownload?, position: Int)
        fun onDeleteItem(displayableDownload: DisplayableDownload?, position: Int)
        fun onDownloadItem(displayableDownload: DisplayableDownload?, position: Int)
    }

    companion object {
        private val TAG = DownloadsAdapter::class.java.simpleName
    }
}