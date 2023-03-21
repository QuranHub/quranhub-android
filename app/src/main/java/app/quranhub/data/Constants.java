package app.quranhub.data;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import app.quranhub.R;

public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    public static final String API_BASE_URL = "https://api.quranhub.app";
    public static final String STATIC_FILES_BASE_URL = "https://www.quranhub.app";

    public static final class Quran {

        private Quran() {
        }

        public static final String HAFS_IMG_BASE_URL =
                STATIC_FILES_BASE_URL + "/quran-pages-images/ayat/hafs/";
        public static final String WARSH_IMG_BASE_URL =
                STATIC_FILES_BASE_URL + "/quran-pages-images/ayat/warsh/";

        public static final int NUM_OF_PAGES = 604;

        // Quran page sizes in pixels
        public static final int HAFS_OTLOOHA_PAGE_ORIGINAL_WIDTH = 622;
        public static final int HAFS_OTLOOHA_PAGE_ORIGINAL_HEIGHT = 917;
        public static final int WARSH_OTLOOHA_PAGE_ORIGINAL_WIDTH = 620;
        public static final int WARSH_OTLOOHA_PAGE_ORIGINAL_HEIGHT = 1005;

        public static final int NUM_OF_VERSES = 6236;
    }

    public static final class BookmarkType {

        private BookmarkType() {
        }

        public static final int FAVORITE = 1;
        public static final int RECITING = 2;
        public static final int NOTE = 3;
        public static final int MEMORIZE = 4;
    }

    public static final class Directory {

        private Directory() {
        }

        public static final String ROOT_PUBLIC = "QuranHub";
        public static final String LIBRARY_PUBLIC = ROOT_PUBLIC + File.separator + "Library";

        public static final String NOTE_VOICE_RECORDER = "Note_Recorder";
        public static final String AYA_VOICE_RECORDER = "Aya_Recorder";
        public static final String QURAN_AUDIO = ".quran_audio";
    }

    public static final class Language {

        private Language() {
        }

        public static final String ENGLISH_CODE = "en";
        public static final String ARABIC_CODE = "ar";
        public static final String SPANISH_CODE = "es";
        public static final String FRENCH_CODE = "fr";
        public static final String HAUSA_CODE = "ha";
        public static final String INDONESIAN_CODE = "in";
        public static final String URDU_CODE = "ur";

        public static final String DEFAULT_APP_LANGUAGE = ENGLISH_CODE;

        /* It's important that the indices of languages is the same in CODES, NAMES_STR_IDS & FLAGS_DRAWABLE_IDS */
        public static final List<String> CODES = Arrays.asList(ENGLISH_CODE, ARABIC_CODE, SPANISH_CODE, FRENCH_CODE
                , HAUSA_CODE, INDONESIAN_CODE, URDU_CODE);
        public static final int[] NAMES_STR_IDS = {R.string.english_language, R.string.arabic_language, R.string.spanish_language
                , R.string.french_language, R.string.hausa_language, R.string.indonesian_language, R.string.urdu_language};
        public static final int[] FLAGS_DRAWABLE_IDS = {R.drawable.flag_en, R.drawable.flag_ar, R.drawable.flag_es
                , R.drawable.flag_fr, R.drawable.flag_ha, R.drawable.flag_in, R.drawable.flag_ur};
    }

    public static final class Recitation {
        public static final String HAFS_KEY = "hafs";
        public static final String WARSH_KEY = "warsh";

        public static final int HAFS_ID = 0;
        public static final int WARSH_ID = 1;

        /* It's important that the index of any recitation name is the same as the ID integer given for it above */
        public static final int[] NAMES_STR_IDS = {R.string.hafs_recitation, R.string.warsh_recitation};
    }

}
