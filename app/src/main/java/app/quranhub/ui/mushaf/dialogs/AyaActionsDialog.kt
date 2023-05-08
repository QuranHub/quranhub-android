package app.quranhub.ui.mushaf.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

import app.quranhub.R;
import app.quranhub.data.Constants;
import app.quranhub.databinding.DialogAyaPropertiesBinding;
import app.quranhub.ui.mushaf.model.BookmarkModel;

public class AyaActionsDialog extends DialogFragment {

    private static final String TAG = AyaActionsDialog.class.getSimpleName();

    public static final String ARG_Y_LOCATION = "ARG_Y_LOCATION";

    private int yLocation;

    private Dialog dialog;
    private AyaPropertiesListener ayaPropertiesListener;

    private DialogAyaPropertiesBinding binding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof AyaPropertiesListener) {
            ayaPropertiesListener = (AyaPropertiesListener) getParentFragment();
        } else {
            throw new ClassCastException(
                    getParentFragment().getClass().getSimpleName() + " must implement AyaActionsDialog#AyaPropertiesListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            yLocation = getArguments().getInt(ARG_Y_LOCATION);
        } else {
            Log.w(TAG, "AyaActionsDialog : No arguments specified");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogAyaPropertiesBinding.inflate(getLayoutInflater());
        initializeDialog();
        return dialog;
    }

    public void initializeDialog() {
        dialog = new Dialog(getActivity());
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        layoutParams.y = yLocation;
        dialog.setContentView(binding.getRoot());
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        attachListeners();
    }

    public void setBookmarkTypeIcon(BookmarkModel bookmarkModel) {
        // handle if set image after orientation change

        if (bookmarkModel.getBookmarkTypeId() == Constants.BookmarkType.NOTE) {
            binding.bookmarkIv.setImageResource(R.drawable.bookmark_green_selected);
        } else if (bookmarkModel.getBookmarkTypeId() == Constants.BookmarkType.MEMORIZE) {
            binding.bookmarkIv.setImageResource(R.drawable.bookmark_red_selected);
        } else if (bookmarkModel.getBookmarkTypeId() == Constants.BookmarkType.RECITING) {
            binding.bookmarkIv.setImageResource(R.drawable.bookmark_gold_selected);
        } else if (bookmarkModel.getBookmarkTypeId() == Constants.BookmarkType.FAVORITE) {
            binding.bookmarkIv.setImageResource(R.drawable.fav_added__gold_ic);
        } else {    // CUSTOM BOOKMARK
            binding.bookmarkIv.setImageResource(R.drawable.bookmark_green_selected);
            binding.bookmarkIv.setColorFilter(getActivity().getResources().getIntArray(R.array.bookmark_colors)[bookmarkModel.getColorIndex()]);
        }
    }

    private void attachListeners() {
        binding.shareContainer.setOnClickListener(v -> onShareClick());

        binding.faselContainer.setOnClickListener(v -> onFasilClick());

        binding.listenContainer.setOnClickListener(v -> onListenClick());

        binding.tafseerContainer.setOnClickListener(v -> onTafserClick());

        binding.notesContainer.setOnClickListener(v -> onNotesClick());
    }

    private void onShareClick() {
        dialog.dismiss();
        ayaPropertiesListener.onShareClick();
    }

    private void onFasilClick() {
        dialog.dismiss();
        ayaPropertiesListener.onFasilClick();
    }

    private void onListenClick() {
        dialog.dismiss();
        ayaPropertiesListener.onListenClick();
    }

    public void onTafserClick() {
        dialog.dismiss();
        ayaPropertiesListener.onTafserClick();
    }

    private void onNotesClick() {
        dialog.dismiss();
        ayaPropertiesListener.onNoteClick();
    }

    public void setAyaHasNote() {
        if (binding.noteIv == null)
            return;
        binding.noteIv.setImageResource(R.drawable.notes_gold_sidemenu_ic);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface AyaPropertiesListener {

        void onShareClick();

        void onFasilClick();

        void onListenClick();

        void onTafserClick();

        void onNoteClick();
    }

}
