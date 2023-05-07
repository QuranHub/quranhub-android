package app.quranhub.ui.mushaf.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.R;
import app.quranhub.data.local.entity.Book;
import app.quranhub.data.remote.model.BookContent;
import app.quranhub.databinding.ItemBookBinding;
import app.quranhub.ui.mushaf.listener.ItemSelectionListener;
import app.quranhub.util.LocaleUtils;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    private List<BookContent> bookList;
    private List<BookContent> translationFiliterList;
    public static final int TRANSLATION_NOT_DOWNLOADED = 0;
    public static final int TRANSLATION_DOWNLOADED_IN_PROGRESS = 1;
    public static final int TRANSLATION_DOWNLOADED = 2;
    private boolean isEditable;
    private TranslationActionsListener translationActionsListener;

    public BookAdapter(TranslationActionsListener translationActionsListener) {
        this.translationActionsListener = translationActionsListener;
        isEditable = false;
        bookList = new ArrayList<>();
        translationFiliterList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookContent model = translationFiliterList.get(position);
        holder.binding.translationTv.setText(model.getName());
        if (isEditable) {
            holder.binding.translationIv.setImageResource(R.drawable.ic_delete);
            changeIconType(false, holder);
        } else if (model.getDownloadStatus() == TRANSLATION_DOWNLOADED) {
            holder.binding.translationIv.setImageResource(R.drawable.check_gold_ic);
            changeIconType(false, holder);
        } else if (model.getDownloadStatus() == TRANSLATION_NOT_DOWNLOADED) {
            holder.binding.translationIv.setImageResource(R.drawable.ic_download);
            changeIconType(false, holder);
        } else if (model.getDownloadStatus() == TRANSLATION_DOWNLOADED_IN_PROGRESS) {
            changeIconType(true, holder);
        }

        if (!LocaleUtils.getAppLanguage().equals("ar")) {
            holder.binding.translationTv.setGravity(Gravity.LEFT);
        }

        holder.binding.translationTv.setOnClickListener(v -> {
            if (model.getDownloadStatus() == TRANSLATION_NOT_DOWNLOADED) {                             // open file in pdf if it exist in local storage and downloaded before
                translationActionsListener.onDownloadTranslation(model);
            } else if (model.getDownloadStatus() == TRANSLATION_DOWNLOADED) {
                translationActionsListener.onSelectItem(model);
            }
        });

        holder.binding.downloadProgress.setOnClickListener(v -> {
            translationActionsListener.onCancelDownload(model);
        });

        holder.binding.translationIv.setOnClickListener(v -> {
            if (isEditable) {
                translationActionsListener.onDeleteTranslation(model);
            } else if (model.getDownloadStatus() == TRANSLATION_NOT_DOWNLOADED) {
                translationActionsListener.onDownloadTranslation(model);
            }
        });
    }

    private void changeIconType(boolean isDownloadProgress, ViewHolder holder) {
        if (isDownloadProgress) {
            holder.binding.cancelDownload.setVisibility(View.VISIBLE);
            holder.binding.downloadProgress.setVisibility(View.VISIBLE);
            holder.binding.translationIv.setVisibility(View.INVISIBLE);
        } else {
            holder.binding.cancelDownload.setVisibility(View.INVISIBLE);
            holder.binding.downloadProgress.setVisibility(View.INVISIBLE);
            holder.binding.translationIv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return translationFiliterList.size();
    }

    public void setBookList(List<BookContent> bookList) {
        this.bookList = bookList;
        this.translationFiliterList = bookList;
        notifyDataSetChanged();
    }

    public void filter(String inputQuery) {
        if (inputQuery.isEmpty()) {
            if (!isEditable)
                translationFiliterList = bookList;
            else
                setDownloadTranslations();
        } else {
            List<BookContent> filteredList = new ArrayList<>();
            for (BookContent row : bookList) {
                if (isEditable && row.getDownloadStatus() != TRANSLATION_DOWNLOADED)
                    continue;
                if (row.getName().toLowerCase().contains(inputQuery.toLowerCase())) {
                    filteredList.add(row);
                }
            }
            translationFiliterList = filteredList;
        }
        notifyDataSetChanged();
    }


    public void setDownloadTranslations() {
        isEditable = true;
        List<BookContent> filteredList = new ArrayList<>();
        for (BookContent model : bookList) {
            if (model.getDownloadStatus() == TRANSLATION_DOWNLOADED)
                filteredList.add(model);
        }

        this.translationFiliterList = filteredList;
        notifyDataSetChanged();
    }

    public void setAllTranslation() {
        isEditable = false;
        translationFiliterList = bookList;
        notifyDataSetChanged();
    }

    public void removeDeletedFile(int id) {
        for (BookContent model : translationFiliterList) {
            if (model.getId() == id) {
                translationFiliterList.remove(model);
                break;
            }
        }

        for (BookContent model : bookList) {
            if (model.getId() == id) {
                model.setDownloadStatus(TRANSLATION_NOT_DOWNLOADED);
                break;
            }
        }

        notifyDataSetChanged();
    }

    public void updateBooksDownloadStatus(List<Book> models) {
        for (Book book : models) {
            for (BookContent content : translationFiliterList) {
                if (book.getId() == content.getId()) {
                    content.setDownloadId(book.getDownloadId());
                    content.setDownloadStatus(book.getDownloadStatus());
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ItemBookBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemBookBinding.bind(itemView);
        }
    }

    public interface TranslationActionsListener extends ItemSelectionListener<BookContent> {
        void onCancelDownload(BookContent model);

        void onDownloadTranslation(BookContent model);

        void onDeleteTranslation(BookContent model);
    }


}
