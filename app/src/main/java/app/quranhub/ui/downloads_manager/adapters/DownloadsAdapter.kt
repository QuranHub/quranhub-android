package app.quranhub.ui.downloads_manager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.quranhub.R;
import app.quranhub.databinding.ItemDownloadBinding;
import app.quranhub.ui.downloads_manager.model.DisplayableDownload;

public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.ViewHolder> {

    private static final String TAG = DownloadsAdapter.class.getSimpleName();

    @NonNull
    private List<DisplayableDownload> displayableDownloads;
    @NonNull
    private ItemClickListener clickListener;
    private boolean edit = false;

    public DownloadsAdapter(@NonNull List<DisplayableDownload> displayableDownloads,
                            @NonNull ItemClickListener clickListener) {
        this.displayableDownloads = displayableDownloads;
        this.clickListener = clickListener;
    }

    public DownloadsAdapter(@NonNull List<DisplayableDownload> displayableDownloads,
                            @NonNull ItemClickListener clickListener, boolean edit) {
        this.displayableDownloads = displayableDownloads;
        this.clickListener = clickListener;
        this.edit = edit;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_download, parent, false);

        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(displayableDownloads.get(position));
    }

    @Override
    public int getItemCount() {
        return displayableDownloads.size();
    }

    @NonNull
    public List<DisplayableDownload> getDisplayableDownloads() {
        return displayableDownloads;
    }

    public void setDisplayableDownloads(@NonNull List<DisplayableDownload> displayableDownloads) {
        this.displayableDownloads = displayableDownloads;
        notifyDataSetChanged();
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ItemDownloadBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);
            binding = ItemDownloadBinding.bind(itemView);

            attachClickListeners();
        }

        public void bind(@NonNull DisplayableDownload displayableDownload) {
            binding.tvName.setText(displayableDownload.getName());
            if (displayableDownload.getDownloadedAmount() != null) {
                binding.tvDownloadedAmount.setText(displayableDownload.getDownloadedAmount());
            }
            if (edit) {  // edit mode
                if (displayableDownload.isDeletable()) {
                    binding.ibAction.setImageResource(R.drawable.ic_delete);
                    binding.ibAction.setVisibility(View.VISIBLE);
                } else {
                    binding.ibAction.setVisibility(View.INVISIBLE);
                }
            } else {  // download mode
                if (displayableDownload.isDownloadable()) {
                    binding.ibAction.setImageResource(R.drawable.ic_download);
                    binding.ibAction.setVisibility(View.VISIBLE);
                } else {
                    binding.ibAction.setVisibility(View.INVISIBLE);
                }
            }
        }

        private void attachClickListeners() {

            binding.llContent.setOnClickListener(v -> {
                onContentClick();
            });

            binding.ibAction.setOnClickListener(v -> {
                onActionButtonClick();
            });

        }

        private void onContentClick() {
            DisplayableDownload clickedDisplayableDownload = displayableDownloads.get(getAdapterPosition());
            clickListener.onClickItem(clickedDisplayableDownload, getAdapterPosition());
        }

        private void onActionButtonClick() {
            DisplayableDownload clickedDisplayableDownload = displayableDownloads.get(getAdapterPosition());
            if (edit) {
                clickListener.onDeleteItem(clickedDisplayableDownload, getAdapterPosition());
            } else {
                clickListener.onDownloadItem(clickedDisplayableDownload, getAdapterPosition());
            }
        }
    }

    public interface ItemClickListener {

        void onClickItem(DisplayableDownload displayableDownload, int position);

        void onDeleteItem(DisplayableDownload displayableDownload, int position);

        void onDownloadItem(DisplayableDownload displayableDownload, int position);

    }
}
