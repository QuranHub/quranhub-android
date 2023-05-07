package app.quranhub.ui.mushaf.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Arrays;
import java.util.Objects;

import app.quranhub.R;
import app.quranhub.databinding.DialogNoteFilterBinding;
import app.quranhub.ui.mushaf.adapter.FilterAdapter;
import app.quranhub.ui.mushaf.listener.ItemSelectionListener;

public class NotesFilterDialog extends DialogFragment implements FilterAdapter.OptionClickListener {

    private static final String NOTE_TYPE_ARGS = "NOTE_TYPE_ARGS";
    private Dialog dialog;
    private ItemSelectionListener<Integer> listener;
    private int selectedOption;
    private FilterAdapter adapter;
    private String[] options;

    private DialogNoteFilterBinding binding;

    public static NotesFilterDialog getInstance(int type) {
        Bundle bundle = new Bundle();
        bundle.putInt(NOTE_TYPE_ARGS, type);
        NotesFilterDialog dialog = new NotesFilterDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (ItemSelectionListener) getParentFragment();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogNoteFilterBinding.inflate(getLayoutInflater());
        initializeDialog();
        setFilterOptions();
        initViews();
        return dialog;
    }

    private void setFilterOptions() {
        options = new String[]{
                getString(R.string.all_types),
                getString(R.string.general_comment),
                getString(R.string.momerize_mistake),
                getString(R.string.tajweed_mistake)
        };
    }

    private void initViews() {
        binding.noteFilterRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new FilterAdapter(Arrays.asList(options), options[selectedOption], this, 0);
        binding.noteFilterRv.setAdapter(adapter);
    }


    public void initializeDialog() {
        dialog = new Dialog(requireActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(binding.getRoot());
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        if (getArguments() != null) {
            selectedOption = getArguments().getInt(NOTE_TYPE_ARGS);
        }
    }

    /*@OnClick(R.id.btn_back)
    public void onClickBack() {
        dismiss();
    }

    @OnClick(R.id.btn_show)
    public void onShowFilter() {
        listener.onSelectItem(selectedOption);
        dismiss();
    }*/

    @Override
    public void onOptionClick(String optionName, int optionIndex) {
        //selectedOption = optionIndex;
        listener.onSelectItem(optionIndex);
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
