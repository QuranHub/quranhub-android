package app.quranhub.data.local.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Note implements Parcelable {

    @PrimaryKey
    int ayaId;
    int noteType;
    String noteText;
    String noteRecorderPath;

    public Note(int ayaId, int noteType, String noteText, String noteRecorderPath) {
        this.ayaId = ayaId;
        this.noteType = noteType;
        this.noteText = noteText;
        this.noteRecorderPath = noteRecorderPath;
    }

    protected Note(Parcel in) {
        ayaId = in.readInt();
        noteType = in.readInt();
        noteText = in.readString();
        noteRecorderPath = in.readString();
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    public int getAyaId() {
        return ayaId;
    }

    public void setAyaId(int ayaId) {
        this.ayaId = ayaId;
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
    }

}
