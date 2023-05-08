package app.quranhub.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

import app.quranhub.data.Constants;

public final class QuranAudioFileUtils {

    private QuranAudioFileUtils() { /* Prevent instantiation */ }

    /**
     * Generates & returns the Quran audio file name for the given args.
     *
     * @param sura Sura number (one-based index).
     * @param aya  Aya number in sura (one-based index).
     * @return Audio file name constructed for sura & aya, or {@code null} if the provided args
     * is incorrect.
     */
    @Nullable
    public static String getFileName(int sura, int aya) {
        /*
        Aya audio file name should be on the format: XXXYYY.mp3
        Where XXX is a 3 digit sura number (one-based) & YYY is a 3 digit aya number (one-based).
        */

        StringBuilder suraSb = new StringBuilder(Integer.toString(sura));
        StringBuilder ayaSb = new StringBuilder(Integer.toString(aya));

        if (suraSb.length() > 3 || ayaSb.length() > 3)
            return null; // maximum is 3 digit number; incorrect arg

        while (suraSb.length() < 3) suraSb.insert(0, 0);
        while (ayaSb.length() < 3) ayaSb.insert(0, 0);

        return suraSb.toString() + ayaSb.toString() + ".mp3";
    }

    /**
     * Generates & returns the relative path of the directory for the Quran audio files for the
     * given {@code recitationId} & {@code sheikhId}.
     *
     * @param recitationId Recitation ID as in {@link Constants.Recitation}.
     * @param sheikhId     Reciter sheikh ID.
     * @return Directory relative path as a String, or {@code null} if the given
     * {@code recitationId} or {@code sheikhId} args is incorrect
     */
    @Nullable
    public static String getLocalRelativeDirPath(int recitationId, String sheikhId) {

        if (sheikhId == null || sheikhId.length() == 0) return null;

        String recitationDirPath = getLocalRelativeDirPath(recitationId);

        if (recitationDirPath != null) {
            return recitationDirPath + sheikhId + File.separator;
        } else {
            return null;
        }
    }

    /**
     * Generates & returns the relative path of the directory for the Quran audio files for the
     * given {@code recitationId}.
     *
     * @param recitationId Recitation ID as in {@link Constants.Recitation}.
     * @return Directory relative path as a String, or {@code null} if the given
     * {@code recitationId} or {@code sheikhId} args is incorrect
     */
    @Nullable
    public static String getLocalRelativeDirPath(int recitationId) {
        String recitationKey;
        if (recitationId == Constants.Recitation.HAFS_ID)
            recitationKey = Constants.Recitation.HAFS_KEY;
        else if (recitationId == Constants.Recitation.WARSH_ID)
            recitationKey = Constants.Recitation.WARSH_KEY;
        else return null;

        return File.separator + Constants.Directory.QURAN_AUDIO + File.separator
                + recitationKey + File.separator;
    }

    /**
     * Generates & returns the, absolute, path of the directory for the Quran audio files for the
     * given {@code recitationId} & {@code sheikhId}.
     *
     * @param context      A valid Context.
     * @param recitationId Recitation ID as in {@link Constants.Recitation}.
     * @param sheikhId     Reciter sheikh ID.
     * @return Directory, absolute, path as a String, or {@code null} if the given
     * {@code recitationId} or {@code sheikhId} args is incorrect.
     */
    @Nullable
    public static String getLocalDirPath(@NonNull Context context, int recitationId, String sheikhId) {
        String relativeDirPath = getLocalRelativeDirPath(recitationId, sheikhId);

        if (relativeDirPath != null) {
            return context.getExternalFilesDir(null).getPath() + relativeDirPath;
        } else {
            return null;
        }
    }

    /**
     * Generates & returns the, absolute, path of the directory for the Quran audio files for the
     * given {@code recitationId}.
     *
     * @param context      A valid Context.
     * @param recitationId Recitation ID as in {@link Constants.Recitation}.
     * @return Directory, absolute, path as a String, or {@code null} if the given
     * {@code recitationId} or {@code sheikhId} args is incorrect.
     */
    @Nullable
    public static String getLocalDirPath(@NonNull Context context, int recitationId) {
        String relativeDirPath = getLocalRelativeDirPath(recitationId);

        if (relativeDirPath != null) {
            return context.getExternalFilesDir(null).getPath() + relativeDirPath;
        } else {
            return null;
        }
    }
}
