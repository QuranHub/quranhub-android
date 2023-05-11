package app.quranhub.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = Recitation.class, parentColumns = "id", childColumns = "recitation_id"
                , onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE),
        @ForeignKey(entity = Reciter.class, parentColumns = "id", childColumns = "reciter_id"
                , onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)})
public class ReciterRecitation {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "recitation_id")
    private int recitationId;

    @NonNull
    @ColumnInfo(name = "reciter_id")
    private String reciterId;

    public ReciterRecitation(int recitationId, @NonNull String reciterId) {
        this.recitationId = recitationId;
        this.reciterId = reciterId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecitationId() {
        return recitationId;
    }

    public void setRecitationId(int recitationId) {
        this.recitationId = recitationId;
    }

    @NonNull
    public String getReciterId() {
        return reciterId;
    }

    public void setReciterId(@NonNull String reciterId) {
        this.reciterId = reciterId;
    }

    @NonNull
    @Override
    public String toString() {
        return "ReciterRecitation{" +
                "id=" + id +
                ", recitationId=" + recitationId +
                ", reciterId='" + reciterId + '\'' +
                '}';
    }
}
