package app.quranhub.ui.mushaf.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.R;
import app.quranhub.databinding.ItemOptionBinding;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {

    private List<String> optionsList;
    private List<String> filteredOptionsList;

    private String selectedOption;
    @NonNull
    private OptionClickListener itemClickListener;
    private int requestCode;

    public FilterAdapter(@NonNull List<String> optionsList, String selectedOption
            , @NonNull OptionClickListener listener, int requestCode) {
        this.optionsList = optionsList;
        this.filteredOptionsList = optionsList;
        this.selectedOption = selectedOption;
        this.itemClickListener = listener;
        this.requestCode = requestCode;
    }

    @NonNull
    @Override
    public FilterAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_option, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterAdapter.ViewHolder holder, int position) {
        String option = filteredOptionsList.get(position);
        holder.binding.tvOptionName.setText(option);
        if (option.equals(selectedOption)) {
            holder.binding.ivCheckBox.setVisibility(View.VISIBLE);
        } else {
            holder.binding.ivCheckBox.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return filteredOptionsList.size();
    }

    public void filter(String inputQuery) {
        if (inputQuery.isEmpty()) {
            filteredOptionsList = optionsList;
        } else {
            List<String> filteredList = new ArrayList<>();
            for (String row : optionsList) {
                if (row.toLowerCase().contains(inputQuery.toLowerCase())) {
                    filteredList.add(row);
                }
            }
            filteredOptionsList = filteredList;
        }
        notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        ItemOptionBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemOptionBinding.bind(itemView);
            attachListeners();
        }

        private void attachListeners() {
            binding.tvOptionName.setOnClickListener(v -> onClickSura());
        }

        private void onClickSura() {
            selectedOption = binding.tvOptionName.getText().toString();
            notifyDataSetChanged();
            getSelectedOptionIndex(binding.tvOptionName.getText().toString());
        }

    }

    // get selected sura index in original list (not filtered list)
    private void getSelectedOptionIndex(String option) {
        int selectedIndex = 0;
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).equals(option)) {
                selectedIndex = i;
                break;
            }
        }
        itemClickListener.onOptionClick(option, selectedIndex);
    }

    public interface OptionClickListener {
        void onOptionClick(String optionName, int optionIndex);
    }


}
