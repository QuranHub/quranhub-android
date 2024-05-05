package app.quranhub.util

import android.content.Context
import android.os.AsyncTask
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.prefs.AppPreferencesManager
import java.io.File

object QuranAudioDeleteUtils {

    @JvmStatic
    fun deleteRecitationAudio(
        context: Context, recitationId: Int, deleteFinishListener: DeleteFinishListener
    ) {
        object : AsyncTask<Void?, Void?, Void?>() {

            override fun doInBackground(vararg voids: Void?): Void? {

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
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                deleteFinishListener.onDeleteFinish()
            }
        }.execute()
    }

    @JvmStatic
    fun deleteReciterAudio(
        context: Context,
        recitationId: Int,
        reciterId: String,
        deleteFinishListener: DeleteFinishListener
    ) {
        object : AsyncTask<Void?, Void?, Void?>() {
            override fun doInBackground(vararg voids: Void?): Void? {

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
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                deleteFinishListener.onDeleteFinish()
            }
        }.execute()
    }

    @JvmStatic
    fun deleteSuraAudio(
        context: Context,
        recitationId: Int,
        reciterId: String,
        suraId: Int,
        deleteFinishListener: DeleteFinishListener
    ) {
        object : AsyncTask<Void?, Void?, Void?>() {

            override fun doInBackground(vararg voids: Void?): Void? {
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
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                deleteFinishListener.onDeleteFinish()
            }
        }.execute()
    }

    private fun deleteRecursive(dir: File) {
        if (dir.isDirectory) for (child in dir.listFiles()) deleteRecursive(child)
        dir.delete()
    }

    interface DeleteFinishListener {
        fun onDeleteFinish()
    }
}