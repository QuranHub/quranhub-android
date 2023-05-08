package app.quranhub.ui.mushaf.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

import app.quranhub.databinding.DialogOpenFileBinding;

public class OpenFileDialog extends DialogFragment {

    private DialogOpenFileBinding binding;

    private Dialog dialog;
    private OpenFileListener listener;
    public static final int IN_APP = 1;
    public static final int OUT_APP = 2;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (OpenFileListener) getParentFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogOpenFileBinding.inflate(getLayoutInflater());
        initializeDialog();
        return dialog;
    }

    private void openPdfInApp() {
        dialog.cancel();
        listener.onOpefFile(IN_APP);
    }

    private void openPdfOutApp() {
        dialog.cancel();
        listener.onOpefFile(OUT_APP);
    }

    private void initializeDialog() {
        dialog = new Dialog(requireActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(binding.getRoot());
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        attachListeners();
    }

    private void attachListeners() {
        binding.inApp.setOnClickListener(v -> openPdfInApp());
        binding.outApp.setOnClickListener(v -> openPdfOutApp());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface OpenFileListener {
        void onOpefFile(int openType);
    }
}
