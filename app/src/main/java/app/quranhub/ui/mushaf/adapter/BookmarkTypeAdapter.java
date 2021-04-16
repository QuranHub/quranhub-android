package app.quranhub.ui.mushaf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.quranhub.R;
import app.quranhub.ui.mushaf.data.entity.BookmarkType;
import app.quranhub.ui.mushaf.listener.ItemSelectionListener;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BookmarkTypeAdapter extends RecyclerView.Adapter<BookmarkTypeAdapter.ViewHolder> {

    private List<BookmarkType> bookmarkTypes;
    private Context context;
    private ItemSelectionListener<Integer> listener;
    private int selectedType;
    private int[] bookmarkColors;

    public BookmarkTypeAdapter(List<BookmarkType> bookmarkTypes, Context context, ItemSelectionListener<Integer> listener) {
        this.bookmarkTypes = bookmarkTypes;
        this.context = context;
        this.listener = listener;
        selectedType = 0;
        bookmarkColors = context.getResources().getIntArray(R.array.bookmark_colors);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bookmark_type_item, parent, false);
        return new BookmarkTypeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        BookmarkType type = bookmarkTypes.get(position);

        if (position == 0) {
            holder.typeIv.setColorFilter(null);
            holder.typeIv.setImageResource(R.drawable.fav_gold_sidemenu_ic);
            holder.typeTv.setText(context.getString(R.string.fasil_favorite));
            holder.seperator.setVisibility(View.VISIBLE);

        } else if (position == 1) {
            holder.typeIv.setColorFilter(null);
            holder.typeIv.setImageResource(R.drawable.bookmark_gold);
            holder.typeTv.setText(context.getString(R.string.fasil_read));
        } else if (position == 2) {
            holder.typeIv.setColorFilter(null);
            holder.typeIv.setImageResource(R.drawable.bookmark_green);
            holder.typeTv.setText(context.getString(R.string.fasil_note));
        } else if (position == 3) {
            holder.typeIv.setColorFilter(null);
            holder.typeIv.setImageResource(R.drawable.bookmark_red);
            holder.typeTv.setText(context.getString(R.string.fasil_memorize));
        } else {  // CUSTOM BOOKMARK
            holder.typeIv.setColorFilter(bookmarkColors[type.getColorIndex()]);
            holder.typeTv.setText(type.getBookmarkTypeName());
        }

        if (position > 0) {
            holder.seperator.setVisibility(View.INVISIBLE);
        }

        if (position == selectedType) {
            holder.checkIv.setVisibility(View.VISIBLE);
        } else {
            holder.checkIv.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return bookmarkTypes.size();
    }

    public void hideCheck() {
        selectedType = -1;
        notifyDataSetChanged();
    }

    public void setTypeCheck(int selectedFilter) {
        selectedType = selectedFilter - 1;
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.type_iv)
        ImageView typeIv;
        @BindView(R.id.type_tv)
        TextView typeTv;
        @BindView(R.id.check_iv)
        ImageView checkIv;
        @BindView(R.id.seperator1)
        View seperator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.item_container)
        public void onClickItem() {
            selectedType = getAdapterPosition();
            notifyDataSetChanged();
            listener.onSelectItem(selectedType + 1);
        }
    }

}
