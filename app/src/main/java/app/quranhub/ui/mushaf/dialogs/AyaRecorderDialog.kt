package app.quranhub.ui.mushaf.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Objects;

import app.quranhub.databinding.DialogAyaRecorderBinding;
import app.quranhub.ui.mushaf.viewmodel.VoiceRecorderViewModel;

public class AyaRecorderDialog extends DialogFragment {

    private Dialog dialog;
    private StopRecordingListener listener;
    private int ayaId;
    private VoiceRecorderViewModel voiceRecorderViewModel;
    private static final String ARG_AYA_ID = "ARG_AYA_ID";

    private DialogAyaRecorderBinding binding;

    public static AyaRecorderDialog getInstance(int ayaId) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_AYA_ID, ayaId);
        AyaRecorderDialog recorderDialog = new AyaRecorderDialog();
        recorderDialog.setArguments(bundle);
        return recorderDialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (StopRecordingListener) getParentFragment();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogAyaRecorderBinding.inflate(getLayoutInflater());
        getArgs();
        initializeDialog();
        initReorder(savedInstanceState == null);
        getPrevState(savedInstanceState);
        return dialog;
    }

    private void getPrevState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            startTimer(SystemClock.elapsedRealtime());
        } else {
            startTimer(SystemClock.elapsedRealtime() + savedInstanceState.getLong("chronometer_time"));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("chronometer_time", binding.recorderChronometer.getBase() - SystemClock.elapsedRealtime());
    }

    private void initReorder(boolean startRecord) {
        voiceRecorderViewModel = new ViewModelProvider(this).get(VoiceRecorderViewModel.class);
        if (startRecord) {
            voiceRecorderViewModel.setAyaRecorderPath(ayaId, getActivity());
            voiceRecorderViewModel.startRecord();
        }
    }

    private void getArgs() {
        if (getArguments() != null) {
            ayaId = getArguments().getInt(ARG_AYA_ID);
        }
    }

    public void initializeDialog() {
        dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        dialog.setContentView(binding.getRoot());
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        attachListeners();
    }


    private void startTimer(long base) {
        binding.recorderChronometer.setBase(base);
        binding.recorderChronometer.start();
    }

    private void attachListeners() {
        binding.stopRecordingView.setOnClickListener(v -> onStopRecording());
    }

    private void onStopRecording() {
        voiceRecorderViewModel.releaseRecorder();
        binding.recorderChronometer.stop();
        listener.onStopRecording(voiceRecorderViewModel.getOutputRecorderPath());
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.recorderChronometer.stop();
        if (!getActivity().isChangingConfigurations()) {
            voiceRecorderViewModel.releaseRecorder();
        }
        binding = null;
    }

    public interface StopRecordingListener {
        void onStopRecording(String filePath);
    }
}
