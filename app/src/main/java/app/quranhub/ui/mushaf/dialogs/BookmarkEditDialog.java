package app.quranhub.ui.mushaf.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.quranhub.R;
import app.quranhub.data.local.entity.BookmarkType;
import app.quranhub.databinding.DialogBookmarkFilterBinding;
import app.quranhub.ui.mushaf.adapter.BookmarkTypeAdapter;
import app.quranhub.ui.mushaf.listener.ItemSelectionListener;
import app.quranhub.util.DialogUtils;

public class BookmarkEditDialog extends DialogFragment implements ItemSelectionListener<Integer> {

    private final static String BOOKMARK_TYPES_ARGS = "BOOKMARK_TYPES_ARGS";
    private final static String FILTER_TYPE = "FILTER_TYPE";
    private final static String DIALOG_TYPE = "DIALOG_TYPE";

    private Dialog dialog;
    private BookmarkFilterListener listener;
    public static final int ALL_BOOKMARK_FILTER = 0;
    private int selectedFilter, bookmarkColorIndex;
    private BookmarkTypeAdapter adapter;
    private List<BookmarkType> bookmarkTypes;
    private boolean editDialog;

    private DialogBookmarkFilterBinding binding;

    public static BookmarkEditDialog getInstance(List<BookmarkType> types, int type, boolean editDialog) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(BOOKMARK_TYPES_ARGS, (ArrayList<? extends Parcelable>) types);
        bundle.putBoolean(DIALOG_TYPE, editDialog);
        bundle.putInt(FILTER_TYPE, type);
        BookmarkEditDialog dialog = new BookmarkEditDialog();
        dialog.setArguments(bundle);
        return dialog;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (BookmarkFilterListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("The parent fragment of BookmarkEditDialog (" +
                    getParentFragment().getClass().getSimpleName() + ") must implement the BookmarkFilterListener interface");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogBookmarkFilterBinding.inflate(getLayoutInflater());
        getArgs();
        initializeDialog();
        setDialogTypeViews();
        return dialog;
    }

    private void setDialogTypeViews() {
        if (editDialog) {
            binding.btnShow.setText(getString(R.string.edit));
            binding.allBookmarkCheckbox.setVisibility(View.GONE);
            binding.allBookmark.setVisibility(View.GONE);
        }
    }


    private void getArgs() {
        if (getArguments() != null) {
            selectedFilter = getArguments().getInt(FILTER_TYPE, 0);
            bookmarkTypes = getArguments().getParcelableArrayList(BOOKMARK_TYPES_ARGS);
            editDialog = getArguments().getBoolean(DIALOG_TYPE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        DialogUtils.adjustDialogSize(this);
    }

    public void initializeDialog() {
        dialog = new Dialog(requireActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(binding.getRoot());
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        adapter = new BookmarkTypeAdapter(bookmarkTypes, getActivity(), this);
        binding.bookmarkTypesRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.bookmarkTypesRv.setAdapter(adapter);
        if (selectedFilter == 0) {
            adapter.hideCheck();
        } else {
            binding.allBookmarkCheckbox.setVisibility(View.GONE);
            adapter.setTypeCheck(selectedFilter);
        }
        attachListeners();
    }

    private void attachListeners() {
        binding.allBookmark.setOnClickListener(v -> onSelectAllBookmark());
        binding.btnShow.setOnClickListener(v -> onShowFilterList());
        binding.btnBack.setOnClickListener(v -> onBackDialog());
    }

    private void onSelectAllBookmark() {
        binding.allBookmarkCheckbox.setVisibility(View.VISIBLE);
        adapter.hideCheck();
        selectedFilter = ALL_BOOKMARK_FILTER;
    }

    private void onShowFilterList() {
        listener.onBookmarkFilter(selectedFilter, bookmarkColorIndex);
        dialog.dismiss();
    }

    private void onBackDialog() {
        dialog.dismiss();
    }

    @Override
    public void onSelectItem(Integer bookmarkType) {
        binding.allBookmarkCheckbox.setVisibility(View.GONE);
        selectedFilter = bookmarkType;
        bookmarkColorIndex = bookmarkTypes.get(selectedFilter - 1).getColorIndex();
    }


    public interface BookmarkFilterListener {
        void onBookmarkFilter(int filter, int colorIndex);
    }

}
