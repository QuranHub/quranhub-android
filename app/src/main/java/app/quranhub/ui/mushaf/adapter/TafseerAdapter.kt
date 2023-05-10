package app.quranhub.ui.mushaf.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.R
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.databinding.TafseerRowBinding
import app.quranhub.ui.mushaf.model.TafseerModel
import java.util.Locale

class TafseerAdapter(private val context: Context) :
    RecyclerView.Adapter<TafseerAdapter.ViewHolder>() {

    private var tafseerModelList: List<TafseerModel>
    private var tafseerFilteredModelList: List<TafseerModel>

    init {
        tafseerModelList = ArrayList()
        tafseerFilteredModelList = ArrayList()
    }

    fun setTafseerModelList(tafseerModelList: List<TafseerModel>) {
        this.tafseerModelList = tafseerModelList
        tafseerFilteredModelList = tafseerModelList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tafseer_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = tafseerFilteredModelList[position]
        holder.binding.ayaTv.text = model.text
        holder.binding.tafseerTv.text = model.tafseer
        holder.binding.numlinesTv.text = model.tafseer
        if (model.isExpandable) {
            holder.binding.tafseerTv.expand()
        } else {
            holder.binding.tafseerTv.collapse()
        }
        holder.binding.numlinesTv.post {
            val lineCount = holder.binding.numlinesTv.lineCount
            holder.binding.numlinesTv.visibility = View.GONE
            if (lineCount < 5) {
                holder.binding.numlinesTv.visibility = View.GONE
            } else {
                holder.binding.numlinesTv.visibility = View.VISIBLE
            }
        }
        if (AppPreferencesManager.getAppLangSetting(context) != "ar" && AppPreferencesManager.getQuranTranslationLanguage(
                context
            ) != "ar"
        ) {             // !LocaleUtils.getTranslationLanguage().equals("ar")
            holder.binding.parentLayout.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        holder.binding.moreTv.setOnClickListener { v: View? ->
            if (holder.binding.tafseerTv.isExpanded) {
                holder.binding.moreTv.text = context.getString(R.string.more)
                holder.binding.tafseerTv.collapse()
                tafseerFilteredModelList[position].isExpandable = false
            } else {
                holder.binding.moreTv.text = context.getString(R.string.collapse)
                holder.binding.tafseerTv.expand()
                tafseerFilteredModelList[position].isExpandable = true
            }
        }
    }

    override fun getItemCount(): Int {
        return tafseerFilteredModelList.size
    }

    fun filter(inputQuery: String) {
        tafseerFilteredModelList = if (inputQuery.isEmpty()) {
            tafseerModelList
        } else {
            val filteredList: MutableList<TafseerModel> = ArrayList()
            for (row in tafseerModelList) {
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
        var binding: TafseerRowBinding

        init {
            binding = TafseerRowBinding.bind(itemView)
        }
    }
}