package app.quranhub.data

import app.quranhub.R
import java.io.File

object Constants {
    const val API_BASE_URL = "https://api.quranhub.app"
    const val STATIC_FILES_BASE_URL = "https://www.quranhub.app"

    object Quran {
        const val HAFS_IMG_BASE_URL = "$STATIC_FILES_BASE_URL/quran-pages-images/kfgqpc/hafs-wasat/"
        const val WARSH_IMG_BASE_URL = "$STATIC_FILES_BASE_URL/quran-pages-images/kfgqpc/warsh/"
        const val NUM_OF_PAGES = 604

        // Quran pages sizes in pixels
        const val HAFS_PAGE_IMG_ORIGINAL_WIDTH = 807
        const val HAFS_PAGE_IMG_ORIGINAL_HEIGHT = 1205
        const val HAFS_PAGE_IMG_FIRST_TWO_ORIGINAL_WIDTH = 843
        const val HAFS_PAGE_IMG_FIRST_TWO_ORIGINAL_HEIGHT = 1140
        const val WARSH_PAGE_IMG_ORIGINAL_WIDTH = 1064
        const val WARSH_PAGE_IMG_ORIGINAL_HEIGHT = 1570
        const val WARSH_PAGE_IMG_FIRST_TWO_ORIGINAL_WIDTH = 1211
        const val WARSH_PAGE_IMG_FIRST_TWO_ORIGINAL_HEIGHT = 1640
        const val NUM_OF_VERSES = 6236
    }

    object BookmarkType {
        const val FAVORITE = 1
        const val RECITING = 2
        const val NOTE = 3
        const val MEMORIZE = 4
    }

    object Directory {
        const val ROOT_PUBLIC = "QuranHub"

        @JvmField
        val LIBRARY_PUBLIC = ROOT_PUBLIC + File.separator + "Library"
        const val NOTE_VOICE_RECORDER = "Note_Recorder"
        const val AYA_VOICE_RECORDER = "Aya_Recorder"
        const val QURAN_AUDIO = ".quran_audio"
    }

    object Language {
        const val ENGLISH_CODE = "en"
        const val ARABIC_CODE = "ar"
        const val SPANISH_CODE = "es"
        const val FRENCH_CODE = "fr"
        const val HAUSA_CODE = "ha"
        const val INDONESIAN_CODE = "in"
        const val URDU_CODE = "ur"
        const val DEFAULT_APP_LANGUAGE = ENGLISH_CODE

        /* It's important that the indices of languages is the same in CODES, NAMES_STR_IDS & FLAGS_DRAWABLE_IDS */
        @JvmField
        val CODES = listOf(
            ENGLISH_CODE,
            ARABIC_CODE,
            SPANISH_CODE,
            FRENCH_CODE,
            HAUSA_CODE,
            INDONESIAN_CODE,
            URDU_CODE
        )

        @JvmField
        val NAMES_STR_IDS = intArrayOf(
            R.string.english_language,
            R.string.arabic_language,
            R.string.spanish_language,
            R.string.french_language,
            R.string.hausa_language,
            R.string.indonesian_language,
            R.string.urdu_language
        )

        @JvmField
        val FLAGS_DRAWABLE_IDS = intArrayOf(
            R.drawable.flag_en,
            R.drawable.flag_ar,
            R.drawable.flag_es,
            R.drawable.flag_fr,
            R.drawable.flag_ha,
            R.drawable.flag_in,
            R.drawable.flag_ur
        )
    }

    object Recitation {
        const val HAFS_KEY = "hafs"
        const val WARSH_KEY = "warsh"
        const val HAFS_ID = 0
        const val WARSH_ID = 1

        /* It's important that the index of any recitation name is the same as the ID integer given for it above */
        @JvmField
        val NAMES_STR_IDS = intArrayOf(R.string.hafs_recitation, R.string.warsh_recitation)
    }
}