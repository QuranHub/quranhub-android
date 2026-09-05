package app.quranhub.util

import android.content.Context
import app.quranhub.data.Constants
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.ReciterRecitation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object QuranAudioDownloadUtils {

    /**
     * Registers the [ReciterRecitation] row for the given recitation & reciter
     * in the local database, if it does not exist yet.
     */
    suspend fun registerReciterRecitation(context: Context, recitationId: Int, reciterId: String) {
        withContext(Dispatchers.IO) {
            val userDatabase = UserDatabase.getInstance(context)
            if (userDatabase.reciterRecitationDao[recitationId, reciterId] == null) {
                userDatabase.reciterRecitationDao.insert(
                    ReciterRecitation(
                        recitationId = recitationId,
                        reciterId = reciterId
                    )
                )
            }
        }
    }

    /**
     * Generates & returns the Quran audio file download URL relative path for the given args.
     *
     * @param recitationId Recitation ID as in [Constants.Recitation].
     * @param sheikhId     Sheikh ID.
     * @param sura         Sura number (one-based index).
     * @param aya          Aya number in sura (one-based index).
     * @return returns the file download path as a String, or `null` if one of the provided args
     * is incorrect.
     */
    fun getDownloadUrlPath(recitationId: Int, sheikhId: String, sura: Int, aya: Int): String? {
        /*
            Aya audio file download path should be on the format:
                /{repo_name}/verses/{file_name}
        */
        val sb = StringBuilder()

        // repo_name part
        sb.append("/")
        if (sheikhId == "husary" && recitationId == Constants.Recitation.HAFS_ID) {
            sb.append("quran-audio-hafs-husary")
        } else if (sheikhId == "sudais" && recitationId == Constants.Recitation.HAFS_ID) {
            sb.append("quran-audio-hafs-sudais")
        } else if (sheikhId == "alafasy" && recitationId == Constants.Recitation.HAFS_ID) {
            sb.append("quran-audio-hafs-alafasy")
        } else if (sheikhId == "almuaiqly" && recitationId == Constants.Recitation.HAFS_ID) {
            sb.append("quran-audio-hafs-almuaiqly")
        } else if (sheikhId == "yassin_aljazaery" && recitationId == Constants.Recitation.WARSH_ID) {
            sb.append("quran-audio-warsh-aljazaery")
        } else if (sheikhId == "ibrahim_aldosary" && recitationId == Constants.Recitation.WARSH_ID) {
            sb.append("quran-audio-warsh-ibrahim-aldosary")
        }
        sb.append("/verses/")

        // file_name part
        val fileName = QuranAudioFileUtils.getFileName(sura, aya)
        if (fileName != null) sb.append(fileName) else return null
        return sb.toString()
    }
}