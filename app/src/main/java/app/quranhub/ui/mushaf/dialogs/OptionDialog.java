package app.quranhub.ui.mushaf.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.quranhub.databinding.DialogSuraListBinding;
import app.quranhub.ui.mushaf.adapter.FilterAdapter;
import app.quranhub.util.DialogUtils;

public class OptionDialog extends DialogFragment implements FilterAdapter.OptionClickListener {

    public static final String SURA_NAME_ARGS = "SURA_NAME_ARGS";
    public static final String ALL_ITEMS_ARGS = "ALL_ITEMS_ARGS";
    public static final String CODE_ARGS = "CODE_ARGS";
    public static final String HEADER_ARGS = "HEADER_ARGS";

    private View dialogView;
    private Dialog dialog;
    private ItemClickListener listener;
    private String suraName;
    private FilterAdapter adapter;
    private ArrayList<String> options;
    private int requestCode;

    private DialogSuraListBinding binding;

    public static DialogFragment getInstance(List<String> options, String suraName, int requestCode, String headerText) {
        DialogFragment fragment = new OptionDialog();
        Bundle bundle = new Bundle();
        bundle.putString(SURA_NAME_ARGS, suraName);
        bundle.putStringArrayList(ALL_ITEMS_ARGS, (ArrayList<String>) options);
        bundle.putInt(CODE_ARGS, requestCode);
        bundle.putString(HEADER_ARGS, headerText);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static DialogFragment getInstance(List<String> options, String suraName, String headerText) {
        DialogFragment fragment = new OptionDialog();
        Bundle bundle = new Bundle();
        bundle.putString(SURA_NAME_ARGS, suraName);
        bundle.putStringArrayList(ALL_ITEMS_ARGS, (ArrayList<String>) options);
        bundle.putString(HEADER_ARGS, headerText);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (ItemClickListener) getParentFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogSuraListBinding.inflate(getLayoutInflater());
        initializeDialog();
        setRecyclerList();
        observeOnInputSearch();
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        //DialogUtils.adjustDialogSize(this);
        DialogUtils.adjustDialogSize(this, 0.8f, 0.7f
                , 0.5f, 0.9f);
    }

    private void observeOnInputSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setRecyclerList() {
        adapter = new FilterAdapter(options, suraName, this, requestCode);
        binding.suraRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.suraRv.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.suraRv.setAdapter(adapter);
    }

    private void initializeDialog() {
        dialog = new Dialog(requireActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        if (getArguments() != null) {
            suraName = getArguments().getString(SURA_NAME_ARGS);
            options = getArguments().getStringArrayList(ALL_ITEMS_ARGS);
            requestCode = getArguments().getInt(CODE_ARGS, 1);
            binding.tvTitle.setText(getArguments().getString(HEADER_ARGS));
        }
    }

    @Override
    public void onOptionClick(String suraName, int suraIndex) {
        listener.onItemClick(suraName, suraIndex, requestCode);
        dialog.dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface ItemClickListener {
        void onItemClick(String optionName, int optionIndex, int requestCode);
    }
}
