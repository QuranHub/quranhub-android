package app.quranhub.util;


import androidx.annotation.Nullable;

import app.quranhub.data.Constants;

public final class QuranAudioDownloadUtils {

    private QuranAudioDownloadUtils() { /* Prevent instantiation */ }

    /**
     * Generates & returns the Quran audio file download URL relative path for the given args.
     *
     * @param recitationId Recitation ID as in {@link Constants.Recitation}.
     * @param sheikhId     Sheikh ID.
     * @param sura         Sura number (one-based index).
     * @param aya          Aya number in sura (one-based index).
     * @return returns the file download path as a String, or {@code null} if one of the provided args
     * is incorrect.
     */
    @Nullable
    public static String getDownloadUrlPath(int recitationId, String sheikhId, int sura, int aya) {
        /*
            Aya audio file download path should be on the format:
                /{repo_name}/verses/{file_name}
        */

        StringBuilder sb = new StringBuilder();

        // repo_name part
        sb.append("/");
        if (sheikhId.equals("husary") && recitationId == Constants.Recitation.HAFS_ID) {
            sb.append("quran-audio-hafs-husary");
        } else if (sheikhId.equals("sudais") && recitationId == Constants.Recitation.HAFS_ID) {
            sb.append("quran-audio-hafs-sudais");
        } else if (sheikhId.equals("yassin_aljazaery") && recitationId == Constants.Recitation.WARSH_ID) {
            sb.append("quran-audio-warsh-aljazaery");
        }

        sb.append("/verses/");

        // file_name part
        String fileName = QuranAudioFileUtils.getFileName(sura, aya);
        if (fileName != null) sb.append(fileName);
        else return null;

        return sb.toString();
    }
}
