package app.quranhub.ui.mushaf.adapter;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.quranhub.R;
import app.quranhub.data.local.entity.TranslationBook;
import app.quranhub.databinding.ItemTranslationBinding;
import app.quranhub.ui.mushaf.model.DisplayableTranslation;
import app.quranhub.util.NetworkUtil;

public class TranslationsAdapter extends RecyclerView.Adapter<TranslationsAdapter.ViewHolder>
        implements Filterable {

    private static final String TAG = TranslationsAdapter.class.getSimpleName();

    @Nullable
    private List<DisplayableTranslation> originalTranslations;
    @Nullable
    private List<DisplayableTranslation> filteredTranslations;
    private String searchText = "";
    @Nullable
    private String selectedBookId;
    private ItemClickListener listener;


    public TranslationsAdapter(@Nullable List<DisplayableTranslation> translations, @Nullable String selectedBookId
            , ItemClickListener listener) {
        sortTranslationList(translations);
        this.originalTranslations = translations;
        this.filteredTranslations = translations;
        this.selectedBookId = selectedBookId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_translation, parent, false);

        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DisplayableTranslation t = filteredTranslations.get(position);

        holder.binding.tvBookName.setText(t.getName());
        holder.binding.tvAuthorName.setText(t.getAuthor());

        if (t.getDownloadStatus() == NetworkUtil.STATUS_DOWNLOADED && t.getId().equals(selectedBookId)) {
            holder.binding.ivSelected.setVisibility(View.VISIBLE);
        } else {
            holder.binding.ivSelected.setVisibility(View.INVISIBLE);
        }

        if (t.getDownloadStatus() == NetworkUtil.STATUS_DOWNLOADED) {
            holder.binding.btnAction.setVisibility(View.INVISIBLE);
            holder.binding.progressDownload.setVisibility(View.INVISIBLE);
            holder.binding.progressDownloadLevel.setVisibility(View.INVISIBLE);

        } else if (t.getDownloadStatus() == NetworkUtil.STATUS_DOWNLOADING) {
            holder.binding.btnAction.setVisibility(View.VISIBLE);
            holder.binding.btnAction.setImageResource(R.drawable.ic_close);
            holder.binding.progressDownload.setVisibility(View.VISIBLE);
            holder.binding.progressDownloadLevel.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.binding.progressDownloadLevel.setProgress(t.getDownloadLevelPercentage(), true);
            } else {
                holder.binding.progressDownloadLevel.setProgress(t.getDownloadLevelPercentage());
            }
        } else {
            // not downloaded
            holder.binding.btnAction.setVisibility(View.VISIBLE);
            holder.binding.btnAction.setImageResource(R.drawable.ic_download);
            holder.binding.progressDownload.setVisibility(View.INVISIBLE);
            holder.binding.progressDownloadLevel.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return filteredTranslations != null ? filteredTranslations.size() : 0;
    }

    public void setTranslations(@Nullable List<DisplayableTranslation> translations) {
        sortTranslationList(translations);
        this.originalTranslations = translations;
        getFilter().filter(searchText);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<DisplayableTranslation> resultTranslations = new ArrayList<>();
                if (constraint.length() == 0) {
                    resultTranslations = originalTranslations;
                } else {
                    for (DisplayableTranslation t : originalTranslations) {
                        if (t.getName().toLowerCase().contains(constraint.toString().toLowerCase())
                                || t.getAuthor().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            resultTranslations.add(t);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = resultTranslations;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                searchText = constraint.toString();
                filteredTranslations = (List<DisplayableTranslation>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ItemTranslationBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemTranslationBinding.bind(itemView);
            attachListeners();
        }

        private void attachListeners() {
            binding.getRoot().setOnClickListener(this);
            binding.btnAction.setOnClickListener(v -> onActionButtonClicked());
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            DisplayableTranslation t = filteredTranslations.get(position);
            if (t.getDownloadStatus() == NetworkUtil.STATUS_DOWNLOADED) {
                listener.onTranslationClick(t.getTranslationBook(), getAdapterPosition());
            }
        }

        private void onActionButtonClicked() {
            int position = getAdapterPosition();
            DisplayableTranslation t = filteredTranslations.get(position);
            if (t.getDownloadStatus() == NetworkUtil.STATUS_NOT_DOWNLOADED) {
                listener.onDownloadTranslationClick(t.getTranslationBook(), position);
            } else if (t.getDownloadStatus() == NetworkUtil.STATUS_DOWNLOADING) {
                listener.onCancelDownloadTranslationClick(t.getTranslationBook(), position);
            }
        }
    }

    private void sortTranslationList(List<DisplayableTranslation> displayableTranslations) {
        Collections.sort(displayableTranslations, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
    }


    /**
     * Used in handling items clicks
     */
    public interface ItemClickListener {
        void onTranslationClick(TranslationBook translationBook, int clickedItemIndex);

        void onDownloadTranslationClick(TranslationBook translationBook, int clickedItemIndex);

        void onCancelDownloadTranslationClick(TranslationBook translationBook, int clickedItemIndex);
    }

}
