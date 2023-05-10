package app.quranhub.ui.mushaf.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
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
import app.quranhub.data.Constants;
import app.quranhub.databinding.ItemBookmarkedFavoriteAyaBinding;
import app.quranhub.ui.mushaf.listener.ItemSelectionListener;
import app.quranhub.ui.mushaf.model.DisplayableBookmark;
import app.quranhub.util.LocaleUtils;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.ViewHolder>
        implements Filterable {

    private static final String TAG = BookmarksAdapter.class.getSimpleName();

    @Nullable
    private List<DisplayableBookmark> originalBookmarks;
    @Nullable
    private List<DisplayableBookmark> filteredBookmarks;
    @NonNull
    private Context context;
    @NonNull
    private BookmarkActionListener bookmarkActionListener;
    protected int[] bookmarkColors;
    private String searchText = "";

    private boolean isEditable = false;

    public BookmarksAdapter(@NonNull Context context
            , @NonNull BookmarkActionListener bookmarkActionListener) {
        this.context = context;
        this.bookmarkActionListener = bookmarkActionListener;
        bookmarkColors = context.getResources().getIntArray(R.array.bookmark_colors);
    }

    public void setBookmarks(@NonNull List<DisplayableBookmark> bookmarks) {
        this.originalBookmarks = bookmarks;
        getFilter().filter(searchText);
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
        notifyDataSetChanged();
    }

    public boolean isEditable() {
        return this.isEditable;
    }

    public void toggleEdit() {
        setEditable(!isEditable);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_bookmarked_favorite_aya, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.bind(filteredBookmarks.get(position));
    }

    @Override
    public int getItemCount() {
        if (filteredBookmarks == null)
            return 0;
        return filteredBookmarks.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<DisplayableBookmark> resultBookmarks = new ArrayList<>();
                if (constraint.length() == 0) {
                    resultBookmarks = originalBookmarks;
                } else {
                    for (DisplayableBookmark bookmark : originalBookmarks) {
                        if (bookmark.getAyaContent().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            resultBookmarks.add(bookmark);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = resultBookmarks;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                searchText = constraint.toString();
                filteredBookmarks = (List<DisplayableBookmark>) results.values;
                notifyDataSetChanged();
            }
        };
    }


    public void deleteBookmark(int ayaId) {
        for (DisplayableBookmark displayableBookmark : filteredBookmarks) {
            if (displayableBookmark.getAyaId() == ayaId) {
                originalBookmarks.remove(displayableBookmark);
                filteredBookmarks.remove(displayableBookmark);
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void filterBookmarks(int bookmarkType) {
        if (bookmarkType == 0) { // show all bookmark
            filteredBookmarks = originalBookmarks;
        } else {
            List<DisplayableBookmark> filteredList = new ArrayList<>();
            for (DisplayableBookmark bookmark : originalBookmarks) {
                if (bookmark.getBookmarkType() == bookmarkType) {
                    filteredList.add(bookmark);
                }
                filteredBookmarks = filteredList;
            }
        }
        notifyDataSetChanged();
    }

    public void editBookmark(int ayaId, int bookmarkType, int colorIndex) {
        for (DisplayableBookmark displayableBookmark : filteredBookmarks) {
            if (displayableBookmark.getAyaId() == ayaId) {
                displayableBookmark.setBookmarkType(bookmarkType);
                displayableBookmark.setColorIndex(colorIndex);
                break;
            }
        }
        for (DisplayableBookmark displayableBookmark : originalBookmarks) {
            if (displayableBookmark.getAyaId() == ayaId) {
                displayableBookmark.setBookmarkType(bookmarkType);
                displayableBookmark.setColorIndex(colorIndex);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ItemBookmarkedFavoriteAyaBinding binding;

        public ViewHolder(View view) {
            super(view);
            binding = ItemBookmarkedFavoriteAyaBinding.bind(view);
            attachListeners();
        }

        @SuppressLint("SetTextI18n")
        public void bind(DisplayableBookmark displayableBookmark) {
            if (displayableBookmark.getBookmarkType() == Constants.BookmarkType.NOTE) {
                binding.ivBookmarkType.setColorFilter(null);
                binding.ivBookmarkType.setImageResource(R.drawable.bookmark_green);
            } else if (displayableBookmark.getBookmarkType() == Constants.BookmarkType.MEMORIZE) {
                binding.ivBookmarkType.setColorFilter(null);
                binding.ivBookmarkType.setImageResource(R.drawable.bookmark_red);
            } else if (displayableBookmark.getBookmarkType() == Constants.BookmarkType.RECITING) {
                binding.ivBookmarkType.setColorFilter(null);
                binding.ivBookmarkType.setImageResource(R.drawable.bookmark_gold);
            } else if (displayableBookmark.getBookmarkType() == Constants.BookmarkType.FAVORITE) {
                binding.ivBookmarkType.setColorFilter(null);
                binding.ivBookmarkType.setImageResource(R.drawable.fav_added__gold_ic);
            } else {
                binding.ivBookmarkType.setImageResource(R.drawable.bookmark_gold);
                binding.ivBookmarkType.setColorFilter(bookmarkColors[displayableBookmark.getColorIndex()]);
            }

            binding.tvAyaContent.setText(displayableBookmark.getAyaContent());
            binding.tvAyaNum.setText(LocaleUtils.formatNumber(displayableBookmark.getSuraAyaNumber()));
            binding.tvGuz2Num.setText(LocaleUtils.formatNumber(displayableBookmark.getGuz2Number()));
            binding.tvHizbNum.setText(LocaleUtils.formatNumber(displayableBookmark.getHizbNumber()));
            binding.tvRub3Num.setText(LocaleUtils.formatNumber(displayableBookmark.getRub3Number()));
            binding.tvSuraName.setText(displayableBookmark.getSuraName());
            binding.tvSuraName.setTypeface(Typeface.create(Typeface.createFromAsset(
                    context.getAssets(), "fonts/diwany_thuluth.ttf"), Typeface.BOLD));
            binding.tvPageNum.setText(LocaleUtils.formatNumber(displayableBookmark.getPageNumber()));

            if (isEditable) {
                binding.ibDeleteBookmark.setVisibility(View.VISIBLE);
            } else {
                binding.ibDeleteBookmark.setVisibility(View.GONE);
            }

        }

        private void attachListeners() {
            binding.itemView.setOnClickListener(v -> gotoBookmarkAya());
            binding.ibDeleteBookmark.setOnClickListener(v -> deleteBookmark());
            binding.ivBookmarkType.setOnClickListener(v -> displayBookmarkTypeDialog());
        }

        private void gotoBookmarkAya() {
            if (!isEditable) {
                bookmarkActionListener.onSelectItem(filteredBookmarks.get(getAdapterPosition()));
            }
        }

        private void deleteBookmark() {
            if (isEditable) {
                Log.d(TAG, "delete bookmark: " + getAdapterPosition());
                bookmarkActionListener.deleteBookmark(filteredBookmarks.get(getAdapterPosition()));
            }
        }

        private void displayBookmarkTypeDialog() {
            if (isEditable) {
                bookmarkActionListener.updateBookmarkType(filteredBookmarks.get(getAdapterPosition()).getBookmarkId());

                /*BookmarkTypesPopup bookmarkTypesPopup = new BookmarkTypesPopup(context, bookmarkTypeId -> {
                    bookmarkActionListener.updateBookmarkType(filteredBookmarks.get(getAdapterPosition())
                            , bookmarkTypeId);
                });
                bookmarkTypesPopup.showPopup(view);*/
            }
        }

    }

    public interface BookmarkActionListener extends ItemSelectionListener<DisplayableBookmark> {

        void deleteBookmark(@NonNull DisplayableBookmark displayableBookmark);

        void updateBookmarkType(@NonNull int bookmarkId);

    }
}
