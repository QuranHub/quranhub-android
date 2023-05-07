package app.quranhub.ui.mushaf.dialogs;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;

import app.quranhub.R;
import app.quranhub.databinding.AyaAudioViewBinding;
import app.quranhub.util.LocaleUtils;

public class AyaAudioPopup {

    private PopupWindow popupWindow;
    private Context context;
    private AyaAudioListener listener;

    private AyaAudioViewBinding binding;

    public AyaAudioPopup(@NonNull Context context, AyaAudioListener callback) {
        this.context = context;
        this.listener = callback;
        setWindowView();
    }

    private void setWindowView() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        binding = AyaAudioViewBinding.inflate(inflater);
        popupWindow = new PopupWindow(binding.getRoot(), WRAP_CONTENT, WRAP_CONTENT, false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setElevation(24f);
        setViewDirections();
        attachListeners();
    }

    private void setViewDirections() {
        if (LocaleUtils.getAppLanguage().equals("ar")) {
            binding.prevAyaIv.setImageResource(R.drawable.player_fast_forward_white_ic);
            binding.nextAyaIv.setImageResource(R.drawable.player_fast_rewind_white_ic);
        }
    }

    public void showPopup(View anchorView) {
        if (!popupWindow.isShowing()) {
            popupWindow.showAtLocation(anchorView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 50);
        }
    }

    private void attachListeners() {

        binding.playIv.setOnClickListener(v -> onPlayAudio());

        binding.recordIv.setOnClickListener(v -> onClickRecord());

        binding.nextAyaIv.setOnClickListener(v -> playNextAya());

        binding.prevAyaIv.setOnClickListener(v -> playPrevAya());

        binding.repeatIv.setOnClickListener(v -> onClickRepeat());

        binding.reciterIv.setOnClickListener(v -> onClickReciter());

        binding.stopIv.setOnClickListener(v -> onClickStop());

    }

    private void onPlayAudio() {
        listener.checkPlayPauseState();
    }


    private void onClickRecord() {
        listener.onPressRecord();
    }

    private void playNextAya() {
        listener.onPlayNextAya();
    }

    private void playPrevAya() {
        listener.onPlayPrevAya();
    }

    private void onClickRepeat() {
        listener.onClickRepeat();
    }

    private void onClickReciter() {
        listener.onClickReciter();
    }

    private void onClickStop() {
        listener.onClickStop();
    }

    public void dismissPopup() {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public void setRecordState(boolean hasRecorder) {
        if (hasRecorder) {
            binding.recordIv.setImageResource(R.drawable.play_record);
        } else {
            binding.recordIv.setImageResource(R.drawable.player_record_white_ic);
        }
    }

    public void setPlayState() {
        binding.playIv.setImageResource(R.drawable.ic_pause);
    }

    public void setPauseState() {
        binding.playIv.setImageResource(R.drawable.player_play_white_ic);
    }

    public interface AyaAudioListener {

        void onPlayNextAya();

        void onPlayPrevAya();

        void onPressRecord();

        void checkPlayPauseState();

        void onClickRepeat();

        void onClickReciter();

        void onClickStop();
    }

}
