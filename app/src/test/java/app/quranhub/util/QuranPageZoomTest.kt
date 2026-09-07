package app.quranhub.util

import org.junit.Assert.assertEquals
import org.junit.Test

class QuranPageZoomTest {

    @Test
    fun `normalize snaps drifted value below one back to one`() {
        // Simulates float drift: 1.05f - 0.05f == 0.99999994f
        val drifted = 1.05f - 0.05f
        assertEquals(1f, QuranPageZoom.normalize(drifted), 0f)
    }

    @Test
    fun `normalize keeps exact boundary values unchanged`() {
        assertEquals(1f, QuranPageZoom.normalize(1f), 0f)
        assertEquals(1.5f, QuranPageZoom.normalize(1.5f), 0f)
    }

    @Test
    fun `normalize rounds to two decimal places`() {
        assertEquals(1.05f, QuranPageZoom.normalize(1.0499999f), 0f)
        assertEquals(1.1f, QuranPageZoom.normalize(1.1000001f), 0f)
    }

    @Test
    fun `normalize clamps out of range values`() {
        assertEquals(1f, QuranPageZoom.normalize(0.5f), 0f)
        assertEquals(1.5f, QuranPageZoom.normalize(2f), 0f)
    }

    @Test
    fun `zoom in then zoom out returns exactly one`() {
        val zoomedIn = QuranPageZoom.zoomIn(1f)
        assertEquals(1.05f, zoomedIn, 0f)
        assertEquals(1f, QuranPageZoom.zoomOut(zoomedIn), 0f)
    }

    @Test
    fun `zoom out at minimum stays at minimum`() {
        assertEquals(1f, QuranPageZoom.zoomOut(1f), 0f)
    }

    @Test
    fun `zoom in at maximum stays at maximum`() {
        assertEquals(1.5f, QuranPageZoom.zoomIn(1.5f), 0f)
    }

    @Test
    fun `ten zoom in steps reach exactly the maximum`() {
        var scale = 1f
        repeat(10) { scale = QuranPageZoom.zoomIn(scale) }
        assertEquals(1.5f, scale, 0f)
    }

    @Test
    fun `can zoom in below maximum but not at it`() {
        assertEquals(true, QuranPageZoom.canZoomIn(1f))
        assertEquals(true, QuranPageZoom.canZoomIn(1.45f))
        assertEquals(false, QuranPageZoom.canZoomIn(1.5f))
    }

    @Test
    fun `can zoom out above minimum but not at it`() {
        assertEquals(true, QuranPageZoom.canZoomOut(1.5f))
        assertEquals(true, QuranPageZoom.canZoomOut(1.05f))
        assertEquals(false, QuranPageZoom.canZoomOut(1f))
    }
}
