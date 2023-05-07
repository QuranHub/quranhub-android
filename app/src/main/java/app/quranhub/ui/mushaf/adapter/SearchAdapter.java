package app.quranhub.ui.mushaf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.R;
import app.quranhub.databinding.AyaSearchItemBinding;
import app.quranhub.ui.mushaf.listener.ItemSelectionListener;
import app.quranhub.ui.mushaf.model.SearchModel;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private Context context;
    private ItemSelectionListener<SearchModel> listener;
    private List<SearchModel> searchModels;
    private List<SearchModel> filterSearchModels;

    public SearchAdapter(@NonNull Context context
            , @NonNull ItemSelectionListener listener) {
        this.context = context;
        this.listener = listener;
        searchModels = new ArrayList<>();
        filterSearchModels = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.aya_search_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchModel model = filterSearchModels.get(position);
        String suraName = context.getResources().getStringArray(R.array.sura_name)[model.getSura() - 1];
        holder.binding.tvAyaContent.setText(model.getPure_text());
        holder.binding.tvAyaNum.setText(String.valueOf(model.getSura_aya()));
        holder.binding.tvGuz2Num.setText(String.valueOf(model.getJuz()));
        holder.binding.tvSuraName.setText(suraName);
        holder.binding.tvPageNum.setText(String.valueOf(model.getPage()));
        holder.binding.tvHizbNum.setText(String.valueOf(model.getHezb()));
        holder.binding.tvRub3Num.setText(String.valueOf(model.getQuarter()));
        holder.itemView.setOnClickListener(v -> listener.onSelectItem(model));
    }

    @Override
    public int getItemCount() {
        return filterSearchModels.size();
    }

    public void setSearchModels(List<SearchModel> searchModels) {
        this.searchModels = searchModels;
        this.filterSearchModels = searchModels;
        notifyDataSetChanged();
    }

    public void filter(String inputQuery) {
        if (inputQuery.isEmpty()) {
            filterSearchModels = searchModels;
        } else {
            List<SearchModel> filteredList = new ArrayList<>();
            for (SearchModel row : searchModels) {
                if (row.getPure_text().toLowerCase().contains(inputQuery.toLowerCase())) {
                    filteredList.add(row);
                }
            }
            filterSearchModels = filteredList;
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        AyaSearchItemBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = AyaSearchItemBinding.bind(itemView);
        }
    }
}
