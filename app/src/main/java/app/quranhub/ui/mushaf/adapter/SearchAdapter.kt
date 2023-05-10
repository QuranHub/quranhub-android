package app.quranhub.ui.mushaf.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.R
import app.quranhub.databinding.AyaSearchItemBinding
import app.quranhub.ui.mushaf.listener.ItemSelectionListener
import app.quranhub.ui.mushaf.model.SearchModel
import java.util.Locale

class SearchAdapter(
    private val context: Context, listener: ItemSelectionListener<SearchModel>
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private val listener: ItemSelectionListener<SearchModel>
    private var searchModels: List<SearchModel>
    private var filterSearchModels: List<SearchModel>

    init {
        this.listener = listener
        searchModels = ArrayList()
        filterSearchModels = ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.aya_search_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = filterSearchModels[position]
        val suraName = context.resources.getStringArray(R.array.sura_name)[model.sura - 1]
        holder.binding.tvAyaContent.text = model.pure_text
        holder.binding.tvAyaNum.text = model.sura_aya.toString()
        holder.binding.tvGuz2Num.text = model.juz.toString()
        holder.binding.tvSuraName.text = suraName
        holder.binding.tvPageNum.text = model.page.toString()
        holder.binding.tvHizbNum.text = model.hezb.toString()
        holder.binding.tvRub3Num.text = model.quarter.toString()
        holder.itemView.setOnClickListener { v: View? -> listener.onSelectItem(model) }
    }

    override fun getItemCount(): Int {
        return filterSearchModels.size
    }

    fun setSearchModels(searchModels: List<SearchModel>) {
        this.searchModels = searchModels
        filterSearchModels = searchModels
        notifyDataSetChanged()
    }

    fun filter(inputQuery: String) {
        filterSearchModels = if (inputQuery.isEmpty()) {
            searchModels
        } else {
            val filteredList: MutableList<SearchModel> = ArrayList()
            for (row in searchModels) {
                if (row.pure_text.lowercase(Locale.getDefault())
                        .contains(inputQuery.lowercase(Locale.getDefault()))
                ) {
                    filteredList.add(row)
                }
            }
            filteredList
        }
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: AyaSearchItemBinding

        init {
            binding = AyaSearchItemBinding.bind(itemView)
        }
    }
}