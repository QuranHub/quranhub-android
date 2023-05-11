package app.quranhub.ui.mushaf.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.R
import app.quranhub.databinding.SuraIndexRowBinding
import app.quranhub.ui.mushaf.listener.ItemSelectionListener
import app.quranhub.ui.mushaf.model.SuraIndexModelMapper
import java.util.Locale

class SuraIndexAdapter(
    private val context: Context,
    itemSelectionListener: ItemSelectionListener<Int>
) : RecyclerView.Adapter<SuraIndexAdapter.ViewHolder>() {

    private var suraIndexModelList: List<SuraIndexModelMapper>
    private var suraIndexFilterList: List<SuraIndexModelMapper>

    private val itemSelectionListener: ItemSelectionListener<Int>

    init {
        suraIndexModelList = ArrayList()
        suraIndexFilterList = ArrayList()
        this.itemSelectionListener = itemSelectionListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<SuraIndexRowBinding>(
            LayoutInflater.from(parent.context),
            R.layout.sura_index_row,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position >= suraIndexFilterList.size) return
        val suraIndexModel = suraIndexFilterList[position]
        if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            holder.binding.leftSuraBorder.setImageResource(R.drawable.gold_ornament_surah_ic)
            holder.binding.rightSuraBorder.setImageResource(R.drawable.gold_ornament_surah_mirror_ic)
            holder.binding.suraNameTv.setTypeface(
                ResourcesCompat.getFont(
                    context,
                    R.font.secondary_font
                ), Typeface.BOLD
            )
            //holder.suraName.setTextSize(TypedValue.COMPLEX_UNIT_SP,context.getResources().getDimension(R.dimen.text_size_18));
        }
        holder.binding.suraIndex = suraIndexModel
        holder.itemView.setOnClickListener { v: View? ->
            itemSelectionListener.onSelectItem(
                Integer.valueOf(
                    suraIndexModel.page
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return suraIndexFilterList.size
    }

    fun setSuraIndexModelList(suraIndexModelList: List<SuraIndexModelMapper>) {
        this.suraIndexModelList = suraIndexModelList
        suraIndexFilterList = suraIndexModelList
        notifyDataSetChanged()
    }

    fun filter(inputQuery: String) {
        suraIndexFilterList = if (inputQuery.isEmpty()) {
            suraIndexModelList
        } else {
            val filteredList: MutableList<SuraIndexModelMapper> = ArrayList()
            for (row in suraIndexModelList) {
                if (row.name?.lowercase(Locale.getDefault())
                        ?.contains(inputQuery.lowercase(Locale.getDefault())) == true
                ) {
                    filteredList.add(row)
                }
            }
            filteredList
        }
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: SuraIndexRowBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}