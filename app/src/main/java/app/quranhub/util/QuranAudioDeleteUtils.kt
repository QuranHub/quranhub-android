package app.quranhub.util

import android.content.Context
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.prefs.AppPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object QuranAudioDeleteUtils {

    suspend fun deleteRecitationAudio(context: Context, recitationId: Int) {
        withContext(Dispatchers.IO) {

            // 1. delete from file system recitation folder with all of its contents
            val recitationDirPath = QuranAudioFileUtils.getLocalDirPath(context, recitationId)
            if (recitationDirPath != null) {
                val dir = File(recitationDirPath)
                if (dir.exists()) {
                    deleteRecursive(dir)
                }
            }

            // 2. delete from DB
            val userDatabase = UserDatabase.getInstance(context)
            val reciters = userDatabase.reciterRecitationDao
                .getRecitersForRecitation(recitationId)
            reciters?.filterNotNull()?.let {
                userDatabase.reciterDao.deleteAll(it.toTypedArray())
            }

            // 3. delete reciter preference if same recitation
            val recitationIdPreference = AppPreferencesManager.getRecitationSetting(context)
            if (recitationIdPreference == recitationId) {
                AppPreferencesManager.resetReciterSheikhSetting(context)
            }
        }
    }

    suspend fun deleteReciterAudio(
        context: Context,
        recitationId: Int,
        reciterId: String
    ) {
        withContext(Dispatchers.IO) {

            // 1. delete from file system the reciter folder for this recitation with all of its contents
            val reciterDirPath = QuranAudioFileUtils.getLocalDirPath(
                context, recitationId, reciterId
            )
            if (reciterDirPath != null) {
                val dir = File(reciterDirPath)
                if (dir.exists()) {
                    deleteRecursive(dir)
                }
            }

            // 2. delete from DB
            val userDatabase = UserDatabase.getInstance(context)
            userDatabase.reciterRecitationDao.delete(recitationId, reciterId)
            // TODO delete also the reciter if he has no suras in any recitation
        }
    }

    suspend fun deleteSuraAudio(
        context: Context,
        recitationId: Int,
        reciterId: String,
        suraId: Int
    ) {
        withContext(Dispatchers.IO) {
            val userDatabase = UserDatabase.getInstance(context)

            // 1. delete from file system the reciter folder for this recitation with all of its contents
            val quranAudios = userDatabase.quranAudioDao
                .getForSura(recitationId, reciterId, suraId)
            for (q in quranAudios) {
                q?.let {
                    val audioFilePath = context.getExternalFilesDir(null)!!.path + it.filePath
                    val audioFile = File(audioFilePath)
                    if (audioFile.exists()) {
                        audioFile.delete()
                    }
                }
            }

            // 2. delete from DB
            userDatabase.quranAudioDao.deleteForSura(
                recitationId, reciterId, suraId
            )
        }
    }

    private fun deleteRecursive(dir: File) {
        if (dir.isDirectory) for (child in dir.listFiles()) deleteRecursive(child)
        dir.delete()
    }
}
