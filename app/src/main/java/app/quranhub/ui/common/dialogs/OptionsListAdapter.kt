package app.quranhub.ui.common.dialogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.quranhub.R;
import app.quranhub.databinding.ItemOptionBinding;

// TODO remove this & use the one in first_wizard package, refactor if necessary
public class OptionsListAdapter extends RecyclerView.Adapter<OptionsListAdapter.ViewHolder> {

    private static final String TAG = OptionsListAdapter.class.getSimpleName();

    @NonNull
    private List<String> optionsList;
    @Nullable
    private int[] optionsThumbnailsDrawableIds;
    private int selectedOptionIndex;
    @NonNull
    private ItemClickListener itemClickListener;

    public OptionsListAdapter(@NonNull List<String> optionsList, int selectedOptionIndex
            , @NonNull ItemClickListener listener) {
        this.optionsList = optionsList;
        this.selectedOptionIndex = selectedOptionIndex;
        this.itemClickListener = listener;
    }

    public OptionsListAdapter(@NonNull List<String> optionsList, @Nullable int[] optionsThumbnailsDrawableIds
            , int selectedOptionIndex, @NonNull ItemClickListener listener) {
        this.optionsList = optionsList;
        this.optionsThumbnailsDrawableIds = optionsThumbnailsDrawableIds;
        this.selectedOptionIndex = selectedOptionIndex;
        this.itemClickListener = listener;
    }

    @NonNull
    public List<String> getOptionsList() {
        return optionsList;
    }

    public void setOptionsList(@NonNull List<String> optionsList) {
        this.optionsList = optionsList;
        notifyDataSetChanged();
    }

    public void setOptions(@NonNull List<String> optionsList, @Nullable int[] optionsThumbnailsDrawableIds) {
        this.optionsList = optionsList;
        this.optionsThumbnailsDrawableIds = optionsThumbnailsDrawableIds;
        notifyDataSetChanged();
    }

    public int getSelectedOptionIndex() {
        return selectedOptionIndex;
    }

    public void setSelectedOptionIndex(int selectedOptionIndex) {
        this.selectedOptionIndex = selectedOptionIndex;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_option, parent, false);

        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (optionsThumbnailsDrawableIds != null) {
            int drawableResId = optionsThumbnailsDrawableIds[position];
            holder.optionThumbnailImageView.setVisibility(View.VISIBLE);
            holder.optionThumbnailImageView.setImageResource(drawableResId);
        } else {
            holder.optionThumbnailImageView.setVisibility(View.GONE);
        }
        String option = optionsList.get(position);
        holder.optionNameTextView.setText(option);
        if (position == selectedOptionIndex) {
            holder.checkBoxImageView.setVisibility(View.VISIBLE);
        } else {
            holder.checkBoxImageView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if (optionsList == null)
            return 0;
        return optionsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView optionThumbnailImageView;
        TextView optionNameTextView;
        ImageView checkBoxImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ItemOptionBinding binding = ItemOptionBinding.bind(itemView);
            optionThumbnailImageView = binding.ivOptionThumbnail;
            optionNameTextView = binding.tvOptionName;
            checkBoxImageView = binding.ivCheckBox;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            setSelectedOptionIndex(getAdapterPosition());
            itemClickListener.onItemClick(selectedOptionIndex);
        }
    }

    /**
     * Used in handling items clicks
     */
    public interface ItemClickListener {
        void onItemClick(int clickedItemIndex);
    }

}
