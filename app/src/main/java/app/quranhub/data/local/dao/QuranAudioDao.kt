package app.quranhub.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.quranhub.data.local.entity.AyaRecorder
import app.quranhub.data.local.entity.QuranAudio
import io.reactivex.Single

@Dao
interface QuranAudioDao {
    /* Just a test */ //TODO remove this
    @Query("SELECT * FROM QURANAUDIO;")
    fun getAll(): List<QuranAudio?>?

    @Query("SELECT file_path FROM QURANAUDIO where aya_id=:id")
    fun getAllAyaAudioPathTest(id: Int): Single<String?>?

    @Query("SELECT * FROM QuranAudio where id IN (:quranAudioIds) ORDER BY aya_id")
    fun getAllByIds(quranAudioIds: IntArray?): List<QuranAudio?>?

    @Query("SELECT * FROM QuranAudio Where id=:quranAudioId")
    fun getById(quranAudioId: Int): QuranAudio?

    @Query(
        "SELECT * FROM QuranAudio as q JOIN ReciterRecitation as sr " +
                "ON q.sheikh_recitation_id=sr.id WHERE recitation_id=:recitationId " +
                "AND reciter_id=:reciterId AND sura=:suraId"
    )
    fun getForSura(recitationId: Int, reciterId: String?, suraId: Int): List<QuranAudio?>

    @Insert
    fun insert(quranAudio: QuranAudio?)

    @Insert
    fun insertAll(quranAudios: Array<QuranAudio?>?)

    @Delete
    fun delete(quranAudio: QuranAudio?)

    @Delete
    fun deleteAll(quranAudios: Array<QuranAudio?>?)

    @Query(
        "DELETE FROM QuranAudio WHERE (sheikh_recitation_id = " +
                "(SELECT sheikh_recitation_id FROM QuranAudio as q JOIN ReciterRecitation as sr " +
                "ON q.sheikh_recitation_id=sr.id WHERE recitation_id=:recitationId AND " +
                "reciter_id=:reciterId) AND sura=:suraId)"
    )
    fun deleteForSura(recitationId: Int, reciterId: String?, suraId: Int)

    @Query(
        "SELECT QuranAudio.file_path from QuranAudio join ReciterRecitation " +
                "on QuranAudio.sheikh_recitation_id=ReciterRecitation.id " +
                "and QuranAudio.aya_id=:ayaId " +
                "and ReciterRecitation.recitation_id=:recitation " +
                "and ReciterRecitation.reciter_id=:sheikh"
    )
    fun getAyaAudioPath(ayaId: Int, recitation: Int, sheikh: String?): Single<String>

    @Query("select recorderPath from ayarecorder where ayaId=:ayaId and recitation=:recitation")
    fun getAyaRecorderPath(ayaId: Int, recitation: Int): Single<String?>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAyaRecorder(recorder: AyaRecorder?)

    @Query("delete from AyaRecorder where ayaId=:ayaId and recitation=:recitation")
    fun deleteAyaVoiceRecorder(ayaId: Int, recitation: Int)
}