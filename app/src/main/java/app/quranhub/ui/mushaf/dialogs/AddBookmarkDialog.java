package app.quranhub.ui.mushaf.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.quranhub.R;
import app.quranhub.data.local.entity.BookmarkType;
import app.quranhub.databinding.DialogAddBookmarkBinding;
import app.quranhub.ui.mushaf.adapter.BookmarkTypeAdapter;
import app.quranhub.ui.mushaf.listener.ItemSelectionListener;
import app.quranhub.util.DialogUtils;

public class AddBookmarkDialog extends DialogFragment implements ItemSelectionListener<Integer> {

    private final static String BOOKMARK_TYPES_ARGS = "BOOKMARK_TYPES_ARGS";
    private final static String IS_EDITABLE = "IS_EDITABLE";

    private DialogAddBookmarkBinding binding;

    private Dialog dialog;
    private AddBookmarkListener listener;
    private int selectedType;
    private List<BookmarkType> bookmarkTypes;
    private boolean isAddCustom = false;
    private BookmarkTypeAdapter adapter;
    private int colorIndex = 0;

    public static AddBookmarkDialog getInstance(List<BookmarkType> types, boolean isEditable) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(BOOKMARK_TYPES_ARGS, (ArrayList<? extends Parcelable>) types);
        bundle.putBoolean(IS_EDITABLE, isEditable);
        AddBookmarkDialog dialog = new AddBookmarkDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddBookmarkListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("The parent fragment of BookmarkEditDialog (" +
                    getParentFragment().getClass().getSimpleName() + ") must implement the BookmarkFilterListener interface");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DialogUtils.wrapDialogHeight(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogAddBookmarkBinding.inflate(getLayoutInflater());
        getArgs();
        initializeDialog();
        observeOnSelectedColor();
        return dialog;
    }

    private void observeOnSelectedColor() {
        int[] colors = getActivity().getResources().getIntArray(R.array.bookmark_colors);
        binding.palette.setSelectedColor(colors[0]);
        binding.palette.setOnColorSelectedListener(color -> {
            for (int i = 0; i < colors.length; i++) {
                if (color == colors[i]) {
                    colorIndex = i;
                    break;
                }
            }
            Log.d("yy8", "observeOnSelectedColor: " + color);
        });
    }

    private void getArgs() {
        if (getArguments() != null) {
            bookmarkTypes = getArguments().getParcelableArrayList(BOOKMARK_TYPES_ARGS);
            if (!getArguments().getBoolean(IS_EDITABLE)) {
                binding.addCustomGroup.setVisibility(View.GONE);
                binding.customBookmarkGroup.setVisibility(View.GONE);
                binding.btnShow.setText(getString(R.string.show));
            }
        }
    }

    public void initializeDialog() {
        dialog = new Dialog(requireActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(binding.getRoot());
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        adapter = new BookmarkTypeAdapter(bookmarkTypes, requireActivity(), this);
        binding.bookmarkTypesRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.bookmarkTypesRv.setAdapter(adapter);
        selectedType = 1;
        attachListeners();
    }

    private void attachListeners() {

        binding.addCustomGroup.setOnClickListener(v -> onAddCustomBookmark());

        binding.btnShow.setOnClickListener(v -> onShowFilterList());

        binding.btnBack.setOnClickListener(v -> onBackDialog());

    }

    private void onAddCustomBookmark() {
        binding.addCustomCheckIv.setVisibility(View.VISIBLE);
        binding.customBookmarkGroup.setVisibility(View.VISIBLE);
        adapter.hideCheck();
        isAddCustom = true;
    }

    private void onShowFilterList() {
        if (isAddCustom) {
            if (binding.bookmarkTitleEt.getText().toString().isEmpty()) {
                Toast.makeText(getActivity(), getString(R.string.enter_bookmark_title), Toast.LENGTH_LONG).show();
            } else {
                BookmarkType type = new BookmarkType(bookmarkTypes.size() + 1, binding.bookmarkTitleEt.getText().toString(), colorIndex);
                listener.addCustomBookmark(type);
                dismiss();
            }
        } else {
            listener.addNormalBookmark(selectedType);
            dismiss();
        }
    }

    private void onBackDialog() {
        dialog.dismiss();
    }

    @Override
    public void onSelectItem(Integer type) {
        selectedType = type;
        isAddCustom = false;
        binding.addCustomCheckIv.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface AddBookmarkListener {
        void addNormalBookmark(int bookmarkType);

        void addCustomBookmark(BookmarkType type);
    }
}
