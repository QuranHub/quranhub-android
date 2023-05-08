package app.quranhub.ui.common.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.databinding.DialogOptionsListBinding;
import app.quranhub.util.DialogUtils;

/**
 * Display options as a list (single selection)
 */
public class OptionsListDialogFragment extends DialogFragment implements OptionsListAdapter.ItemClickListener {

    private static final String TAG = OptionsListDialogFragment.class.getSimpleName();

    private static final String ARG_DIALOG_TITLE = "ARG_DIALOG_TITLE";
    private static final String ARG_DIALOG_OPTIONS = "ARG_DIALOG_OPTIONS";
    private static final String ARG_DIALOG_OPTIONS_THUMBNAILS = "ARG_DIALOG_OPTIONS_THUMBNAILS";
    private static final String ARG_SELECTED_OPTION_INDEX = "ARG_SELECTED_OPTION_INDEX";

    private String dialogTitle;
    private List<String> options;
    private int[] optionsThumbnailsDrawableIds;
    private int selectedOptionIndex;

    private DialogOptionsListBinding binding;

    private ItemSelectionListener itemSelectionListener;

    public OptionsListDialogFragment() {
        // default constructor
    }

    public static OptionsListDialogFragment getInstance(@NonNull String dialogTitle
            , @NonNull List<String> options, @NonNull Fragment targetFragment, int requestCode) {
        return getInstance(dialogTitle, options, -1, targetFragment, requestCode);
    }

    public static OptionsListDialogFragment getInstance(@NonNull String dialogTitle
            , int[] optionsResIds, @NonNull Fragment targetFragment, int requestCode) {
        return getInstance(dialogTitle, optionsResIds, -1, targetFragment, requestCode);
    }

    public static OptionsListDialogFragment getInstance(@NonNull String dialogTitle
            , int[] optionsResIds, int selectedOptionIndex, @NonNull Fragment targetFragment
            , int requestCode) {
        List<String> options = new ArrayList<>();
        for (int stringResId : optionsResIds) {
            options.add(targetFragment.getString(stringResId));
        }

        return getInstance(dialogTitle, options, selectedOptionIndex, targetFragment, requestCode);
    }

    public static OptionsListDialogFragment getInstance(@NonNull String dialogTitle
            , @NonNull List<String> options, int selectedOptionIndex, @NonNull Fragment targetFragment
            , int requestCode) {
        OptionsListDialogFragment fragment = new OptionsListDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DIALOG_TITLE, dialogTitle);
        args.putStringArrayList(ARG_DIALOG_OPTIONS, new ArrayList<>(options));
        args.putInt(ARG_SELECTED_OPTION_INDEX, selectedOptionIndex);
        fragment.setArguments(args);
        fragment.setTargetFragment(targetFragment, requestCode);
        return fragment;
    }

    public static OptionsListDialogFragment getInstance(@NonNull String dialogTitle
            , int[] optionsResIds, int[] optionsThumbnailsDrawableIds, int selectedOptionIndex
            , @NonNull Fragment targetFragment, int requestCode) {
        List<String> options = new ArrayList<>();
        for (int stringResId : optionsResIds) {
            options.add(targetFragment.getString(stringResId));
        }

        OptionsListDialogFragment fragment = new OptionsListDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DIALOG_TITLE, dialogTitle);
        args.putStringArrayList(ARG_DIALOG_OPTIONS, new ArrayList<>(options));
        args.putIntArray(ARG_DIALOG_OPTIONS_THUMBNAILS, optionsThumbnailsDrawableIds);
        args.putInt(ARG_SELECTED_OPTION_INDEX, selectedOptionIndex);
        fragment.setArguments(args);
        fragment.setTargetFragment(targetFragment, requestCode);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            itemSelectionListener = (ItemSelectionListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getTargetFragment().getClass().getSimpleName()
                    + " must implement OptionsListDialogFragment#ItemSelectionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            dialogTitle = getArguments().getString(ARG_DIALOG_TITLE);
            options = getArguments().getStringArrayList(ARG_DIALOG_OPTIONS);
            optionsThumbnailsDrawableIds = getArguments().getIntArray(ARG_DIALOG_OPTIONS_THUMBNAILS);
            selectedOptionIndex = getArguments().getInt(ARG_SELECTED_OPTION_INDEX);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {
        binding = DialogOptionsListBinding.inflate(inflater, container, false);

        initDialogView();

        return binding.getRoot();
    }

    private void initDialogView() {
        binding.tvTitle.setText(dialogTitle);

        binding.rvOptions.setHasFixedSize(true);
        binding.rvOptions.setLayoutManager(new LinearLayoutManager(
                getContext(), RecyclerView.VERTICAL, false));
        binding.rvOptions.addItemDecoration(new DividerItemDecoration(
                getContext(), DividerItemDecoration.VERTICAL));
        OptionsListAdapter adapter = new OptionsListAdapter(options, optionsThumbnailsDrawableIds
                , selectedOptionIndex, this);
        binding.rvOptions.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        DialogUtils.adjustDialogSize(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        itemSelectionListener = null;
    }

    @Override
    public void onItemClick(int clickedItemIndex) {
        itemSelectionListener.onItemSelected(getTargetRequestCode(), clickedItemIndex);
        dismiss();
    }


    public interface ItemSelectionListener {
        void onItemSelected(int requestCode, int itemIndex);
    }
}
