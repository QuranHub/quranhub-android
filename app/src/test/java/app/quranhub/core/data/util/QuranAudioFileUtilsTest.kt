package app.quranhub.core.data.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class QuranAudioFileUtilsTest {

    @Test
    fun resolve_returnsNull_whenExternalStorageUnavailable() {
        assertNull(QuranAudioFileUtils.resolve(null, "/quran_audio/hafs/x/001001.mp3"))
    }

    @Test
    fun resolve_returnsNull_whenRelativePathNull() {
        assertNull(QuranAudioFileUtils.resolve(File("/base"), null))
    }

    @Test
    fun resolve_joinsBaseAndRelativePath() {
        assertEquals(
            "/base/quran_audio/hafs/",
            QuranAudioFileUtils.resolve(File("/base"), "/quran_audio/hafs/")
        )
    }
}
