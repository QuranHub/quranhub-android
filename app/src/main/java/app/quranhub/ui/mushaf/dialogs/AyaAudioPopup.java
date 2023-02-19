package app.quranhub.ui.mushaf.dialogs;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;

import app.quranhub.R;
import app.quranhub.util.LocaleUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AyaAudioPopup {

    private PopupWindow popupWindow;
    private Context context;
    private AyaAudioListener listener;

    @BindView(R.id.play_iv)
    ImageView playIv;
    @BindView(R.id.record_iv)
    ImageView recordIv;
    @BindView(R.id.next_aya_iv)
    ImageView nextAyaIv;
    @BindView(R.id.prev_aya_iv)
    ImageView prevAyaIv;

    public AyaAudioPopup(@NonNull Context context, AyaAudioListener callback) {
        this.context = context;
        this.listener = callback;
        setWindowView();
    }

    private void setWindowView() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.aya_audio_view
                , null, false);
        ButterKnife.bind(this, contentView);
        popupWindow = new PopupWindow(contentView
                , WRAP_CONTENT, WRAP_CONTENT, false);
        popupWindow.setOutsideTouchable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(24f);
        }
        setViewDirections();
    }

    private void setViewDirections() {
        if (LocaleUtils.getAppLanguage().equals("ar")) {
            prevAyaIv.setImageResource(R.drawable.player_fast_forward_white_ic);
            nextAyaIv.setImageResource(R.drawable.player_fast_rewind_white_ic);
        }
    }

    public void showPopup(View anchorView) {
        if (!popupWindow.isShowing()) {
            popupWindow.showAtLocation(anchorView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 50);
        }
    }

    @OnClick(R.id.play_iv)
    public void onPlayAudio() {
        listener.checkPlayPauseState();
    }


    @OnClick(R.id.record_iv)
    public void onClickRecord() {
        listener.onPressRecord();
    }

    @OnClick(R.id.next_aya_iv)
    public void playNextAya() {
        listener.onPlayNextAya();
    }

    @OnClick(R.id.prev_aya_iv)
    public void playPrevAya() {
        listener.onPlayPrevAya();
    }

    @OnClick(R.id.repeat_iv)
    public void onClickRepeat() {
        listener.onClickRepeat();
    }

    @OnClick(R.id.reciter_iv)
    public void onClickReciter() {
        listener.onClickReciter();
    }

    @OnClick(R.id.stop_iv)
    public void onClickStop() {
        listener.onClickStop();
    }

    public void dismissPopup() {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public void setRecordState(boolean hasRecorder) {
        if (hasRecorder) {
            recordIv.setImageResource(R.drawable.play_record);
        } else {
            recordIv.setImageResource(R.drawable.player_record_white_ic);
        }
    }

    public void setPlayState() {
        playIv.setImageResource(R.drawable.ic_pause);
    }

    public void setPauseState() {
        playIv.setImageResource(R.drawable.player_play_white_ic);
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
