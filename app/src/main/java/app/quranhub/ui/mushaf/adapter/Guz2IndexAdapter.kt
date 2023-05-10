package app.quranhub.ui.mushaf.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.R
import app.quranhub.databinding.ItemGuz2IndexBinding
import app.quranhub.ui.mushaf.adapter.Guz2IndexAdapter
import app.quranhub.ui.mushaf.model.HizbQuarterDataModel
import app.quranhub.util.LocaleUtils.formatNumber

class Guz2IndexAdapter : RecyclerView.Adapter<Guz2IndexAdapter.ViewHolder>, Filterable {
    var hizbQuarterDataModels: List<HizbQuarterDataModel>?
        private set
    private var originalHizbQuarterDataModels: MutableList<HizbQuarterDataModel>?
    private var clickCallback: IndexItemClickListener?
    private var context: Context? = null
    private var filterGuz2 = FILTER_GUZ2_ALL

    constructor(
        hizbQuarterDataModels: MutableList<HizbQuarterDataModel>?,
        clickListener: IndexItemClickListener?
    ) {
        this.hizbQuarterDataModels = hizbQuarterDataModels
        originalHizbQuarterDataModels = hizbQuarterDataModels
        clickCallback = clickListener
    }

    constructor(
        hizbQuarterDataModels: MutableList<HizbQuarterDataModel>?,
        filterGuz2: Int,
        clickListener: IndexItemClickListener?
    ) {
        this.hizbQuarterDataModels = hizbQuarterDataModels
        originalHizbQuarterDataModels = hizbQuarterDataModels
        this.filterGuz2 = filterGuz2
        clickCallback = clickListener
        if (hizbQuarterDataModels != null && filterGuz2 != FILTER_GUZ2_ALL) {
            filter.filter(Integer.toString(filterGuz2))
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.item_guz2_index, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = hizbQuarterDataModels!![position]
        if (model.quarter == 1) {
            holder.binding.header.visibility = View.VISIBLE
            holder.binding.tvHeaderGuz2.text =
                context!!.resources.getStringArray(R.array.agza2_name)[model.juz - 1]
            holder.binding.tvHeaderHizb.text =
                context!!.resources.getStringArray(R.array.hezb_name)[model.hizb - 1]
        } else {
            holder.binding.header.visibility = View.GONE
        }
        when (model.quarter) {
            1 -> holder.binding.ivQuarterIndicator.setImageResource(R.drawable.juz2_0)
            2 -> holder.binding.ivQuarterIndicator.setImageResource(R.drawable.juz2_1_4)
            3 -> holder.binding.ivQuarterIndicator.setImageResource(R.drawable.juz2_1_2)
            4 -> holder.binding.ivQuarterIndicator.setImageResource(R.drawable.juz2_3_4)
        }
        holder.binding.tvAyaContent.text = model.ayaText
        holder.binding.tvRub3Num.text = formatNumber(model.quarter)
        holder.binding.tvSuraName.text =
            context!!.resources.getStringArray(R.array.sura_name)[model.suraNumber - 1]
        holder.binding.tvAyaNum.text = formatNumber(model.ayaNumber)
        holder.binding.tvPageNumStart.text = formatNumber(model.startPage)
        holder.binding.tvPageNumEnd.text = formatNumber(model.endPage)
    }

    override fun getItemCount(): Int {
        return if (hizbQuarterDataModels == null) {
            0
        } else hizbQuarterDataModels!!.size
    }

    fun setHizbQuarterDataModels(hizbQuarterDataModels: MutableList<HizbQuarterDataModel>?) {
        originalHizbQuarterDataModels = hizbQuarterDataModels
        filter.filter(filterGuz2.toString())
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults? {
                if (originalHizbQuarterDataModels == null) return null

                var resultHizbQuarterDataModels = mutableListOf<HizbQuarterDataModel>()
                val guz2 = constraint.toString().toInt()
                if (guz2 == FILTER_GUZ2_ALL) {
                    resultHizbQuarterDataModels = originalHizbQuarterDataModels!!
                } else {
                    for (quarterDataModel in originalHizbQuarterDataModels!!) {
                        if (quarterDataModel.juz == guz2) {
                            resultHizbQuarterDataModels.add(quarterDataModel)
                        }
                    }
                }
                val results = FilterResults()
                results.values = resultHizbQuarterDataModels
                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                filterGuz2 = constraint.toString().toInt()
                Log.d(TAG, "publishResults: filterGuz2 = $filterGuz2")
                hizbQuarterDataModels = results.values as? MutableList<HizbQuarterDataModel>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var binding: ItemGuz2IndexBinding

        init {
            binding = ItemGuz2IndexBinding.bind(itemView)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (clickCallback != null) {
                val clickedItemIndex = adapterPosition
                clickCallback!!.onIndexItemClick(
                    hizbQuarterDataModels!![clickedItemIndex],
                    originalHizbQuarterDataModels!!.indexOf(hizbQuarterDataModels!![clickedItemIndex])
                )
            }
        }
    }

    /**
     * Used in handling items clicks
     */
    interface IndexItemClickListener {
        fun onIndexItemClick(model: HizbQuarterDataModel?, clickedItemIndex: Int)
    }

    companion object {
        private val TAG = Guz2IndexAdapter::class.java.simpleName
        const val FILTER_GUZ2_ALL = 0
    }
}