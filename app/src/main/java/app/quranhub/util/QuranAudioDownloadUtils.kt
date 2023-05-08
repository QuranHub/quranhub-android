package app.quranhub.util

import app.quranhub.data.Constants

object QuranAudioDownloadUtils {

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
        } else if (sheikhId == "yassin_aljazaery" && recitationId == Constants.Recitation.WARSH_ID) {
            sb.append("quran-audio-warsh-aljazaery")
        }
        sb.append("/verses/")

        // file_name part
        val fileName = QuranAudioFileUtils.getFileName(sura, aya)
        if (fileName != null) sb.append(fileName) else return null
        return sb.toString()
    }
}