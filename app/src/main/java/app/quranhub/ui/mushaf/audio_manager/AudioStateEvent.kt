package app.quranhub.ui.mushaf.audio_manager;

public class AudioStateEvent {
    private int audioState;

    public AudioStateEvent(int audioState) {
        this.audioState = audioState;
    }

    public int getAudioState() {
        return audioState;
    }

    public void setAudioState(int audioState) {
        this.audioState = audioState;
    }

    public interface State {
        int PLAYING = 0;
        int PAUSED = 1;
        int RESUME = 2;
        int STOP = 3;
        int COMPLETED = 4;
        int PLAY_NEXT = 5;
        int PLAY_PREV = 6;
        int NOT_DOWNLOADED = 7;
        int GROUP_REPEAT_COMPLETED = 8;
    }
}
