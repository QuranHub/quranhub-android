package app.quranhub.ui.mushaf.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.R;
import app.quranhub.databinding.ItemGuz2IndexBinding;
import app.quranhub.ui.mushaf.model.HizbQuarterDataModel;
import app.quranhub.util.LocaleUtils;

public class Guz2IndexAdapter extends RecyclerView.Adapter<Guz2IndexAdapter.ViewHolder> implements Filterable {

    private static final String TAG = Guz2IndexAdapter.class.getSimpleName();

    public static final int FILTER_GUZ2_ALL = 0;

    @Nullable
    private List<HizbQuarterDataModel> filteredHizbQuarterDataModels;
    @Nullable
    private List<HizbQuarterDataModel> originalHizbQuarterDataModels;
    @Nullable
    private IndexItemClickListener clickCallback;
    private Context context;
    private int filterGuz2 = FILTER_GUZ2_ALL;


    public Guz2IndexAdapter(@Nullable List<HizbQuarterDataModel> hizbQuarterDataModels
            , @Nullable IndexItemClickListener clickListener) {
        this.filteredHizbQuarterDataModels = hizbQuarterDataModels;
        this.originalHizbQuarterDataModels = hizbQuarterDataModels;
        this.clickCallback = clickListener;
    }

    public Guz2IndexAdapter(@Nullable List<HizbQuarterDataModel> hizbQuarterDataModels
            , int filterGuz2, @Nullable IndexItemClickListener clickListener) {
        this.filteredHizbQuarterDataModels = hizbQuarterDataModels;
        this.originalHizbQuarterDataModels = hizbQuarterDataModels;
        this.filterGuz2 = filterGuz2;
        this.clickCallback = clickListener;

        if (hizbQuarterDataModels != null && filterGuz2 != FILTER_GUZ2_ALL) {
            getFilter().filter(Integer.toString(filterGuz2));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_guz2_index, parent, false);

        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HizbQuarterDataModel model = filteredHizbQuarterDataModels.get(position);

        if (model.getQuarter() == 1) {
            holder.binding.header.setVisibility(View.VISIBLE);
            holder.binding.tvHeaderGuz2.setText(
                    context.getResources().getStringArray(R.array.agza2_name)[model.getJuz() - 1]);
            holder.binding.tvHeaderHizb.setText(
                    context.getResources().getStringArray(R.array.hezb_name)[model.getHizb() - 1]);
        } else {
            holder.binding.header.setVisibility(View.GONE);
        }

        switch (model.getQuarter()) {
            case 1:
                holder.binding.ivQuarterIndicator.setImageResource(R.drawable.juz2_0);
                break;
            case 2:
                holder.binding.ivQuarterIndicator.setImageResource(R.drawable.juz2_1_4);
                break;
            case 3:
                holder.binding.ivQuarterIndicator.setImageResource(R.drawable.juz2_1_2);
                break;
            case 4:
                holder.binding.ivQuarterIndicator.setImageResource(R.drawable.juz2_3_4);
                break;
        }

        holder.binding.tvAyaContent.setText(model.getAyaText());
        holder.binding.tvRub3Num.setText(LocaleUtils.formatNumber(model.getQuarter()));
        holder.binding.tvSuraName.setText(
                context.getResources().getStringArray(R.array.sura_name)[model.getSuraNumber() - 1]);
        holder.binding.tvAyaNum.setText(LocaleUtils.formatNumber(model.getAyaNumber()));
        holder.binding.tvPageNumStart.setText(LocaleUtils.formatNumber(model.getStartPage()));
        holder.binding.tvPageNumEnd.setText(LocaleUtils.formatNumber(model.getEndPage()));
    }

    @Override
    public int getItemCount() {
        if (filteredHizbQuarterDataModels == null) {
            return 0;
        }
        return filteredHizbQuarterDataModels.size();
    }

    public void setHizbQuarterDataModels(@Nullable List<HizbQuarterDataModel> hizbQuarterDataModels) {
        this.originalHizbQuarterDataModels = hizbQuarterDataModels;
        getFilter().filter(Integer.toString(filterGuz2));
    }

    @Nullable
    public List<HizbQuarterDataModel> getHizbQuarterDataModels() {
        return filteredHizbQuarterDataModels;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (originalHizbQuarterDataModels == null) {
                    return null;
                }

                List<HizbQuarterDataModel> resultHizbQuarterDataModels = new ArrayList<>();
                int guz2 = Integer.parseInt(constraint.toString());

                if (guz2 == FILTER_GUZ2_ALL) {
                    resultHizbQuarterDataModels = originalHizbQuarterDataModels;
                } else {
                    for (HizbQuarterDataModel quarterDataModel : originalHizbQuarterDataModels) {
                        if (quarterDataModel.getJuz() == guz2) {
                            resultHizbQuarterDataModels.add(quarterDataModel);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = resultHizbQuarterDataModels;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filterGuz2 = Integer.parseInt(constraint.toString());
                Log.d(TAG, "publishResults: filterGuz2 = " + filterGuz2);
                if (results != null) {
                    filteredHizbQuarterDataModels = (List<HizbQuarterDataModel>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ItemGuz2IndexBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemGuz2IndexBinding.bind(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickCallback != null) {
                int clickedItemIndex = getAdapterPosition();
                clickCallback.onIndexItemClick(filteredHizbQuarterDataModels.get(clickedItemIndex)
                        , originalHizbQuarterDataModels.indexOf(filteredHizbQuarterDataModels.get(clickedItemIndex)));
            }
        }
    }


    /**
     * Used in handling items clicks
     */
    public interface IndexItemClickListener {
        void onIndexItemClick(HizbQuarterDataModel model, int clickedItemIndex);
    }

}
