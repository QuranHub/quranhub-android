package app.quranhub.ui.mushaf.dialogs;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import app.quranhub.R;
import app.quranhub.data.Constants;
import app.quranhub.data.local.entity.Note;
import app.quranhub.databinding.DialogAddNoteBinding;
import app.quranhub.util.DialogUtils;
import app.quranhub.util.RecorderMediaHelper;

public class AddNoteDialog extends DialogFragment implements RecorderMediaHelper.MediaPlayerCallback {

    private DialogAddNoteBinding binding;

    private boolean isRecord = false, isPlaying = false, isRecorderAttatched = false, userIsSeeking = false, firstPlay = true;
    private int userSelectedPosition;
    private Dialog dialog;
    private AddNoteListener listener;
    private String[] permissions;
    private String outputRecorderPath;
    private int ayaId;
    private MediaRecorder audioRecorder;
    private RecorderMediaHelper recorderMediaHelper;
    private File outputFile;
    private Note note;
    private boolean isEditable = false;

    public static AddNoteDialog getInstance(int ayaId) {
        Bundle bundle = new Bundle();
        bundle.putInt("aya_id", ayaId);
        AddNoteDialog dialog = new AddNoteDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    public static AddNoteDialog getInstance(Note selectedAyaNote) {
        Bundle bundle = new Bundle();
        bundle.putInt("aya_id", selectedAyaNote.getAyaId());
        bundle.putParcelable("selected_aya", selectedAyaNote);
        AddNoteDialog dialog = new AddNoteDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (AddNoteListener) getParentFragment();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogAddNoteBinding.inflate(getLayoutInflater());
        initializeDialog();
        getArgs();
        listenToSeekbarChanges();
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        DialogUtils.adjustDialogSize(this, DialogUtils.DIALOG_STD_WIDTH_SCREEN_RATIO_PORTRAIT, 0.8f
                , DialogUtils.DIALOG_STD_WIDTH_SCREEN_RATIO_LANDSCAPE, DialogUtils.DIALOG_STD_HEIGHT_SCREEN_RATIO_LANDSCAPE);
    }

    public void initializeDialog() {
        dialog = new Dialog(requireActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(binding.getRoot());
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(false);
        permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        attachListeners();
    }

    private void getArgs() {
        if (getArguments() != null) {
            ayaId = getArguments().getInt("aya_id");
            note = getArguments().getParcelable("selected_aya");
            createRecordingFile();
            if (note != null) {
                isEditable = true;
                setEditView();
                binding.saveBtn.setText(getString(R.string.save));
            }
        }
    }

    private void setEditView() {
        binding.tvTitle.setText(getString(R.string.edit_note));
        ((RadioButton) binding.noteTypeGroup.getChildAt(note.getNoteType())).setChecked(true);
        if (note.getNoteText() != null) {
            binding.addNoteEt.setText(note.getNoteText());
        }
        if (!note.getNoteRecorderPath().isEmpty()) {
            setAudioViewsVisible();
            isRecorderAttatched = true;
            initSoundMedia();
        }

    }

    private void setAudioViewsVisible() {
        binding.voiceTimerTv.setVisibility(View.VISIBLE);
        binding.recordGroup.setVisibility(View.VISIBLE);
        binding.addRecorderIv.setVisibility(View.INVISIBLE);
        binding.voiceStatusTv.setText(getString(R.string.voice_listen));
    }

    private void createRecordingFile() {

        File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC), Constants.Directory.NOTE_VOICE_RECORDER);

        if (!file.exists()) {
            file.mkdir();
        }
        outputRecorderPath = file.getPath() + File.separator + ayaId + ".3gp";
        outputFile = new File(outputRecorderPath);
    }

    private void attachListeners() {
        binding.saveBtn.setOnClickListener(v -> onAddNote());
        binding.cancelBtn.setOnClickListener(v -> onCancel());
        binding.addRecorderIv.setOnClickListener(v -> onClickRecord());
        binding.playIv.setOnClickListener(v -> onPlayRecorder());
        binding.removeRecordIv.setOnClickListener(v -> onRemoveRecord());
    }

    private void onAddNote() {
        if (TextUtils.isEmpty(binding.addNoteEt.getText()) && !isRecorderAttatched && !isRecord) {
            Toast.makeText(getActivity(), getString(R.string.note_empty), Toast.LENGTH_LONG).show();
        } else {
            String path = "";
            if (isRecorderAttatched || isRecord) {
                path = outputRecorderPath;
            } else {
                deleteRecorderFile();
            }
            int selectedType = binding.noteTypeGroup.indexOfChild(
                    binding.getRoot().findViewById(binding.noteTypeGroup.getCheckedRadioButtonId())
            );
            listener.onAddNote(new Note(ayaId, selectedType, binding.addNoteEt.getText().toString(), path), isEditable);
            dismiss();
        }
    }

    private void onCancel() {
        listener.onDismissDialog();
        dismiss();
    }

    private void onClickRecord() {
        if (isRecord) {
            setAudioViewsVisible();
            stopTimer();
            stopRecorderMedia();
            initSoundMedia();
            isRecord = false;
            isRecorderAttatched = true;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(getActivity(), permissions[0]) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(getActivity(), permissions[1]) == PackageManager.PERMISSION_GRANTED) {
                    initRecording();
                } else {
                    requestPermissions(permissions, 1);
                }
            } else {
                initRecording();
            }
        }

    }

    private void initSoundMedia() {
        recorderMediaHelper = new RecorderMediaHelper();
        recorderMediaHelper.setMediaPlayerCallback(this);
        recorderMediaHelper.setAudioPath(outputRecorderPath);
    }

    private void stopRecorderMedia() {
        if (audioRecorder != null) {
            if (isRecord) {
                audioRecorder.stop();
            }
            audioRecorder.release();
        }
        if (recorderMediaHelper != null) {
            recorderMediaHelper.release();
        }
    }

    private void onPlayRecorder() {

        if (isPlaying) {
            binding.playIv.setImageResource(R.drawable.player_play_white_ic);
            recorderMediaHelper.pause();
        } else {
            binding.playIv.setImageResource(R.drawable.ic_pause);
            recorderMediaHelper.play();
            recorderMediaHelper.startUpdatingAudioTime();
            if (firstPlay) {
                firstPlay = false;
                binding.voiceTimerTv.setText("0:00");
            }
        }

        isPlaying = !isPlaying;
    }

    private void onRemoveRecord() {
        binding.recordGroup.setVisibility(View.GONE);
        binding.voiceTimerTv.setVisibility(View.GONE);
        binding.addRecorderIv.setVisibility(View.VISIBLE);
        binding.addRecorderIv.setBackgroundResource(R.drawable.corner_primary_dialog);
        binding.voiceStatusTv.setText(getString(R.string.add_voice));
        recorderMediaHelper.release();
        isRecorderAttatched = false;
    }

    public void deleteRecorderFile() {
        if (outputFile.exists()) {
            outputFile.delete();
        }
    }

    private void listenToSeekbarChanges() {
        binding.recorderProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userIsSeeking = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    userSelectedPosition = progress;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userIsSeeking = false;
                recorderMediaHelper.seekTo(userSelectedPosition);
            }
        });
    }


    private void initRecording() {
        isRecord = true;
        binding.addRecorderIv.setBackgroundResource(R.drawable.red_corner);
        binding.voiceStatusTv.setText(getString(R.string.voice_recorded));
        startTimer();
        startRecord();
    }

    private void startRecord() {
        audioRecorder = new MediaRecorder();
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        audioRecorder.setOutputFile(outputRecorderPath);
        try {
            audioRecorder.prepare();
            audioRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopTimer() {
        binding.recorderChronometer.setVisibility(View.GONE);
        binding.recorderChronometer.stop();
    }

    private void startTimer() {
        binding.recorderChronometer.setVisibility(View.VISIBLE);
        binding.recorderChronometer.setBase(SystemClock.elapsedRealtime());
        binding.recorderChronometer.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGranted = true;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
                break;
            }
        }
        if (isGranted) {
            initRecording();
        } else {
            Toast.makeText(getActivity(), getString(R.string.accept_perm), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        listener.onDismissDialog();
        stopRecorderMedia();
        binding = null;
    }

    @Override
    public void onGetMaxDuration(int duration) {
        binding.recorderProgress.setMax(duration);
    }

    @Override
    public void onPositionChanged(int position) {
        if (!userIsSeeking) {
            if (Build.VERSION.SDK_INT >= 24) {
                binding.recorderProgress.setProgress(position, true);
            } else {
                binding.recorderProgress.setProgress(position);
            }
        }
    }

    @Override
    public void onStateChanged(int state) {
        if (state == State.COMPLETED) {
            binding.recorderProgress.setProgress(0);
            isPlaying = false;
            firstPlay = true;
            binding.playIv.setImageResource(R.drawable.player_play_white_ic);
        }
    }

    @Override
    public void onUpdatedTime(String time) {
        binding.voiceTimerTv.setText(time);
    }

    public interface AddNoteListener {
        void onAddNote(Note note, boolean isEditable);

        void onDismissDialog();
    }
}
