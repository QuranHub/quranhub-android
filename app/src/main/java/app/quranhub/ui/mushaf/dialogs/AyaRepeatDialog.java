package app.quranhub.ui.mushaf.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Objects;

import app.quranhub.R;
import app.quranhub.data.local.entity.Aya;
import app.quranhub.databinding.DialogAyaRepeatBinding;
import app.quranhub.ui.mushaf.model.RepeatModel;
import app.quranhub.ui.mushaf.model.SuraVersesNumber;
import app.quranhub.util.DialogUtils;

public class AyaRepeatDialog extends DialogFragment {

    private static final String ARG_SURA_VERSES_NUMBER = "ARG_SURA_VERSES_NUMBER";
    private static final String ARG_SELECTED_SURA = "ARG_SELECTED_SURA";

    private Dialog dialog;

    private AyaRepeatListener listener;

    private ArrayList<SuraVersesNumber> suraVersesNumberArrayList;
    private Aya selectedAya;
    private int lastAyaInPage;
    private int maxFromAyaNumber, maxToAyaNumber;
    private int fromSuraNumber, toSuraNumber;
    private boolean fromUser = false;

    private DialogAyaRepeatBinding binding;

    public static AyaRepeatDialog getInstance(ArrayList<SuraVersesNumber> suraVersesNumberArrayList, Aya selectedAya) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_SELECTED_SURA, selectedAya);
        bundle.putParcelableArrayList(ARG_SURA_VERSES_NUMBER, suraVersesNumberArrayList);
        AyaRepeatDialog dialog = new AyaRepeatDialog();
        dialog.setArguments(bundle);
        return dialog;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (AyaRepeatListener) getParentFragment();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogAyaRepeatBinding.inflate(getLayoutInflater());
        initializeDialog();
        getArgs();
        setFromToViews();
        observeSpinnerSelection();
        observeOnInputEditText();
        return dialog;
    }

    private void observeOnInputEditText() {

        binding.fromEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty())
                    return;
                if (Integer.parseInt(s.toString()) > maxFromAyaNumber) {
                    binding.fromEt.setError(getString(R.string.enter_valid_aya));
                } else {
                    binding.fromEt.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.toEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty())
                    return;
                if (Integer.parseInt(s.toString()) > maxToAyaNumber) {
                    binding.toEt.setError(getString(R.string.enter_valid_aya));
                } else {
                    binding.toEt.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void observeSpinnerSelection() {


        binding.fromAyaSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent != null && parent.getChildAt(0) != null)
                    ((TextView) parent.getChildAt(0)).setTextColor(requireActivity().getResources().getColor(R.color.white_color));
                maxFromAyaNumber = suraVersesNumberArrayList.get(position).getAyas();
                if (fromUser) {
                    binding.fromEt.setText("1");

                    fromSuraNumber = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.toAyaSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent != null && parent.getChildAt(0) != null)
                    ((TextView) parent.getChildAt(0)).setTextColor(requireActivity().getResources().getColor(R.color.white_color));
                maxToAyaNumber = suraVersesNumberArrayList.get(position).getAyas();
                if (fromUser) {
                    binding.toEt.setText(String.valueOf(maxToAyaNumber));
                    toSuraNumber = position;
                } else {
                    fromUser = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }


    private void setFromToViews() {
        String[] surahs = getResources().getStringArray(R.array.sura_name);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, surahs);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.fromAyaSp.setAdapter(dataAdapter);
        binding.toAyaSp.setAdapter(dataAdapter);
        if (selectedAya != null) {
            lastAyaInPage = suraVersesNumberArrayList.get(selectedAya.getSura() - 1).getAyas();
            binding.fromEt.setText(String.valueOf(selectedAya.getSuraAya()));
            binding.toEt.setText(String.valueOf(lastAyaInPage));
            binding.fromAyaSp.setSelection(selectedAya.getSura() - 1);
            binding.toAyaSp.setSelection(selectedAya.getSura() - 1);
            maxFromAyaNumber = suraVersesNumberArrayList.get(selectedAya.getSura() - 1).getAyas();
            maxToAyaNumber = maxFromAyaNumber;
            toSuraNumber = fromSuraNumber = selectedAya.getSura() - 1;
        } else {
            binding.fromEt.setText("1");
            binding.toEt.setText("1");
            binding.fromAyaSp.setSelection(0);
            binding.toAyaSp.setSelection(0);
            maxFromAyaNumber = suraVersesNumberArrayList.get(0).getAyas();
            maxToAyaNumber = maxFromAyaNumber;
            toSuraNumber = fromSuraNumber = 0;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        DialogUtils.adjustDialogSize(this, 0.8f,
                0.8f, 0.7f,
                0.9f);
    }

    public void initializeDialog() {
        dialog = new Dialog(requireActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(binding.getRoot());
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(false);
        attachListeners();
    }

    private void getArgs() {
        if (getArguments() != null) {
            selectedAya = getArguments().getParcelable(ARG_SELECTED_SURA);
            suraVersesNumberArrayList = getArguments().getParcelableArrayList(ARG_SURA_VERSES_NUMBER);
        }
    }

    private void attachListeners() {
        binding.repeatBtn.setOnClickListener(v -> onClickRepeat());
        binding.btnBack.setOnClickListener(v -> onClickBack());
    }

    private void onClickRepeat() {
        if (binding.fromEt.getError() != null || binding.toEt.getError() != null) {
            Toast.makeText(getActivity(), getString(R.string.enter_valid_aya), Toast.LENGTH_LONG).show();
        } else if (binding.fromEt.getText().toString().isEmpty() || binding.toEt.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.enter_repeat_interval), Toast.LENGTH_LONG).show();
        } else if (fromSuraNumber > toSuraNumber) {
            Toast.makeText(getActivity(), getString(R.string.invalid_repeat_interval), Toast.LENGTH_LONG).show();
        } else if (fromSuraNumber == toSuraNumber && Integer.parseInt(binding.fromEt.getText().toString()) > Integer.parseInt(binding.toEt.getText().toString())) {
            Toast.makeText(getActivity(), getString(R.string.enter_valid_aya), Toast.LENGTH_LONG).show();
        } else {
            RepeatModel repeatModel = new RepeatModel();
            repeatModel.setFromSura(fromSuraNumber + 1);
            repeatModel.setFromAyaId(getFromAyaId(Integer.parseInt(binding.fromEt.getText().toString()), fromSuraNumber + 1));
            repeatModel.setFromAya(Integer.parseInt(binding.fromEt.getText().toString()));
            repeatModel.setToSura(toSuraNumber + 1);
            repeatModel.setToAyaId(getToAyaId(Integer.parseInt(binding.toEt.getText().toString()), toSuraNumber + 1));
            repeatModel.setToAya(Integer.parseInt(binding.toEt.getText().toString()));
            repeatModel.setGroupRepeatNum(binding.ayaGroupNumberEt.getText().toString().trim().isEmpty() || Integer.parseInt(binding.ayaGroupNumberEt.getText().toString()) == 0
                    ? 1 : Integer.parseInt(binding.ayaGroupNumberEt.getText().toString().trim()));

            repeatModel.setAyaRepeatNum(binding.ayaGroupNumberEt.getText().toString().trim().isEmpty() || Integer.parseInt(binding.ayaGroupNumberEt.getText().toString()) == 0
                    ? 1 : Integer.parseInt(binding.ayaGroupNumberEt.getText().toString().trim()));

            repeatModel.setDelayTime(binding.delayEt.getText().toString().trim().isEmpty()
                    ? 1 : Integer.parseInt(binding.delayEt.getText().toString().trim()));
            listener.onAyasRepeat(repeatModel);
            dismiss();
        }
    }

    private int getFromAyaId(int fromAya, int fromSura) {
        int fromAyaId = fromAya;
        for (int i = 1; i < fromSura; i++) {
            fromAyaId += suraVersesNumberArrayList.get(i - 1).getAyas();
        }
        return fromAyaId;
    }

    private int getToAyaId(int toAya, int toSura) {
        int toAyaId = toAya;
        for (int i = 1; i < toSura; i++) {
            toAyaId += suraVersesNumberArrayList.get(i - 1).getAyas();
        }
        return toAyaId;
    }

    private void onClickBack() {
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface AyaRepeatListener {
        void onAyasRepeat(RepeatModel repeatModel);
    }
}
