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
import app.quranhub.data.local.prefs.AppPreferencesManager;
import app.quranhub.databinding.TafseerRowBinding;
import app.quranhub.ui.mushaf.model.TafseerModel;

public class TafseerAdapter extends RecyclerView.Adapter<TafseerAdapter.ViewHolder> {

    private Context context;
    private List<TafseerModel> tafseerModelList;
    private List<TafseerModel> tafseerFilteredModelList;


    public TafseerAdapter(Context context) {
        this.context = context;
        tafseerModelList = new ArrayList<>();
        tafseerFilteredModelList = new ArrayList<>();
    }

    public void setTafseerModelList(List<TafseerModel> tafseerModelList) {
        this.tafseerModelList = tafseerModelList;
        this.tafseerFilteredModelList = tafseerModelList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tafseer_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        TafseerModel model = tafseerFilteredModelList.get(position);
        holder.binding.ayaTv.setText(model.getText());
        holder.binding.tafseerTv.setText(model.getTafseer());
        holder.binding.numlinesTv.setText(model.getTafseer());

        if (model.isExpandable()) {
            holder.binding.tafseerTv.expand();
        } else {
            holder.binding.tafseerTv.collapse();
        }


        holder.binding.numlinesTv.post(() -> {
            final int lineCount = holder.binding.numlinesTv.getLineCount();
            holder.binding.numlinesTv.setVisibility(View.GONE);
            if (lineCount < 5) {
                holder.binding.numlinesTv.setVisibility(View.GONE);
            } else {
                holder.binding.numlinesTv.setVisibility(View.VISIBLE);
            }
        });


        if (!AppPreferencesManager.getAppLangSetting(context).equals("ar") && !AppPreferencesManager.getQuranTranslationLanguage(context).equals("ar")) {             // !LocaleUtils.getTranslationLanguage().equals("ar")
            holder.binding.parentLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }

        holder.binding.moreTv.setOnClickListener(v -> {
            if (holder.binding.tafseerTv.isExpanded()) {
                holder.binding.moreTv.setText(context.getString(R.string.more));
                holder.binding.tafseerTv.collapse();
                tafseerFilteredModelList.get(position).setExpandable(false);
            } else {
                holder.binding.moreTv.setText(context.getString(R.string.collapse));
                holder.binding.tafseerTv.expand();
                tafseerFilteredModelList.get(position).setExpandable(true);
            }
        });

    }

    @Override
    public int getItemCount() {
        return tafseerFilteredModelList.size();
    }

    public void filter(String inputQuery) {
        if (inputQuery.isEmpty()) {
            tafseerFilteredModelList = tafseerModelList;
        } else {
            List<TafseerModel> filteredList = new ArrayList<>();
            for (TafseerModel row : tafseerModelList) {
                if (row.getPure_text().toLowerCase().contains(inputQuery.toLowerCase())) {
                    filteredList.add(row);
                }
            }
            tafseerFilteredModelList = filteredList;
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TafseerRowBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = TafseerRowBinding.bind(itemView);
        }


    }
}
