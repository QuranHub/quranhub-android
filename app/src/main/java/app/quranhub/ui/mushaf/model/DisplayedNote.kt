package app.quranhub.ui.mushaf.model;

import android.os.Parcel;
import android.os.Parcelable;

public class DisplayedNote implements Parcelable {

    private int ayaId;
    private int noteType;
    private String noteText;
    private String noteRecorderPath;

    private int sura;
    private int sura_aya;
    private String pure_text;
    private String text;
    private int page;

    public DisplayedNote(int ayaId, int noteType, String noteText, String noteRecorderPath, int sura, int sura_aya, String pure_text, String text, int page) {
        this.ayaId = ayaId;
        this.noteType = noteType;
        this.noteText = noteText;
        this.noteRecorderPath = noteRecorderPath;
        this.sura = sura;
        this.sura_aya = sura_aya;
        this.pure_text = pure_text;
        this.text = text;
        this.page = page;
    }

    protected DisplayedNote(Parcel in) {
        ayaId = in.readInt();
        noteType = in.readInt();
        noteText = in.readString();
        noteRecorderPath = in.readString();
        sura = in.readInt();
        sura_aya = in.readInt();
        pure_text = in.readString();
        text = in.readString();
        page = in.readInt();
    }

    public static final Creator<DisplayedNote> CREATOR = new Creator<DisplayedNote>() {
        @Override
        public DisplayedNote createFromParcel(Parcel in) {
            return new DisplayedNote(in);
        }

        @Override
        public DisplayedNote[] newArray(int size) {
            return new DisplayedNote[size];
        }
    };

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getAyaId() {
        return ayaId;
    }

    public void setAyaId(int ayaId) {
        this.ayaId = ayaId;
    }

    public int getSura() {
        return sura;
    }

    public void setSura(int sura) {
        this.sura = sura;
    }

    public int getSura_aya() {
        return sura_aya;
    }

    public void setSura_aya(int sura_aya) {
        this.sura_aya = sura_aya;
    }

    public String getPure_text() {
        return pure_text;
    }

    public void setPure_text(String pure_text) {
        this.pure_text = pure_text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getNoteType() {
        return noteType;
    }

    public void setNoteType(int noteType) {
        this.noteType = noteType;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public String getNoteRecorderPath() {
        return noteRecorderPath;
    }

    public void setNoteRecorderPath(String noteRecorderPath) {
        this.noteRecorderPath = noteRecorderPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ayaId);
        dest.writeInt(noteType);
        dest.writeString(noteText);
        dest.writeString(noteRecorderPath);
        dest.writeInt(sura);
        dest.writeInt(sura_aya);
        dest.writeString(pure_text);
        dest.writeString(text);
        dest.writeInt(page);
    }
}
