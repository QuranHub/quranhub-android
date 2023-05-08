package app.quranhub.util

import android.content.Context
import app.quranhub.data.Constants
import java.io.File

object QuranAudioFileUtils {

    /**
     * Generates & returns the Quran audio file name for the given args.
     *
     * @param sura Sura number (one-based index).
     * @param aya  Aya number in sura (one-based index).
     * @return Audio file name constructed for sura & aya, or `null` if the provided args
     * is incorrect.
     */
    fun getFileName(sura: Int, aya: Int): String? {
        /*
        Aya audio file name should be on the format: XXXYYY.mp3
        Where XXX is a 3 digit sura number (one-based) & YYY is a 3 digit aya number (one-based).
        */
        val suraSb = StringBuilder(Integer.toString(sura))
        val ayaSb = StringBuilder(Integer.toString(aya))
        if (suraSb.length > 3 || ayaSb.length > 3) return null // maximum is 3 digit number; incorrect arg
        while (suraSb.length < 3) suraSb.insert(0, 0)
        while (ayaSb.length < 3) ayaSb.insert(0, 0)
        return "$suraSb$ayaSb.mp3"
    }

    /**
     * Generates & returns the relative path of the directory for the Quran audio files for the
     * given `recitationId` & `sheikhId`.
     *
     * @param recitationId Recitation ID as in [Constants.Recitation].
     * @param sheikhId     Reciter sheikh ID.
     * @return Directory relative path as a String, or `null` if the given
     * `recitationId` or `sheikhId` args is incorrect
     */
    fun getLocalRelativeDirPath(recitationId: Int, sheikhId: String?): String? {
        if (sheikhId.isNullOrEmpty()) return null
        val recitationDirPath = getLocalRelativeDirPath(recitationId)
        return if (recitationDirPath != null) {
            recitationDirPath + sheikhId + File.separator
        } else {
            null
        }
    }

    /**
     * Generates & returns the relative path of the directory for the Quran audio files for the
     * given `recitationId`.
     *
     * @param recitationId Recitation ID as in [Constants.Recitation].
     * @return Directory relative path as a String, or `null` if the given
     * `recitationId` or `sheikhId` args is incorrect
     */
    fun getLocalRelativeDirPath(recitationId: Int): String? {
        val recitationKey: String =
            if (recitationId == Constants.Recitation.HAFS_ID) Constants.Recitation.HAFS_KEY else if (recitationId == Constants.Recitation.WARSH_ID) Constants.Recitation.WARSH_KEY else return null
        return (File.separator + Constants.Directory.QURAN_AUDIO + File.separator
                + recitationKey + File.separator)
    }

    /**
     * Generates & returns the, absolute, path of the directory for the Quran audio files for the
     * given `recitationId` & `sheikhId`.
     *
     * @param context      A valid Context.
     * @param recitationId Recitation ID as in [Constants.Recitation].
     * @param sheikhId     Reciter sheikh ID.
     * @return Directory, absolute, path as a String, or `null` if the given
     * `recitationId` or `sheikhId` args is incorrect.
     */
    fun getLocalDirPath(context: Context, recitationId: Int, sheikhId: String?): String? {
        val relativeDirPath = getLocalRelativeDirPath(recitationId, sheikhId)
        return if (relativeDirPath != null) {
            context.getExternalFilesDir(null)!!.path + relativeDirPath
        } else {
            null
        }
    }

    /**
     * Generates & returns the, absolute, path of the directory for the Quran audio files for the
     * given `recitationId`.
     *
     * @param context      A valid Context.
     * @param recitationId Recitation ID as in [Constants.Recitation].
     * @return Directory, absolute, path as a String, or `null` if the given
     * `recitationId` or `sheikhId` args is incorrect.
     */
    fun getLocalDirPath(context: Context, recitationId: Int): String? {
        val relativeDirPath = getLocalRelativeDirPath(recitationId)
        return if (relativeDirPath != null) {
            context.getExternalFilesDir(null)!!.path + relativeDirPath
        } else {
            null
        }
    }
}