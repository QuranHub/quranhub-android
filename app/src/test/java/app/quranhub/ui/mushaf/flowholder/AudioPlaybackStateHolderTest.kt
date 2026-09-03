package app.quranhub.ui.mushaf.flowholder

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AudioPlaybackStateHolderTest {

    @Before
    fun setUp() {
        AudioPlaybackStateHolder.reset()
    }

    @Test
    fun `starts in STOPPED state`() = runBlocking {
        val initial = AudioPlaybackStateHolder.state.first()
        assertEquals(AudioPlaybackState.STOPPED, initial.state)
        assertEquals(0L, initial.seq)
    }

    @Test
    fun `delivers every update even when the same state repeats`() = runBlocking {
        val collected = mutableListOf<AudioPlaybackUpdate>()
        val job = launch {
            AudioPlaybackStateHolder.state.take(4).toList(collected)
        }
        yield() // let the collector subscribe before emitting
        AudioPlaybackStateHolder.update(AudioPlaybackState.NOT_DOWNLOADED)
        yield()
        AudioPlaybackStateHolder.update(AudioPlaybackState.NOT_DOWNLOADED)
        yield()
        AudioPlaybackStateHolder.update(AudioPlaybackState.PLAYING)
        job.join()
        assertEquals(
            listOf(
                AudioPlaybackState.STOPPED, // initial replay on subscription
                AudioPlaybackState.NOT_DOWNLOADED,
                AudioPlaybackState.NOT_DOWNLOADED,
                AudioPlaybackState.PLAYING
            ),
            collected.map { it.state }
        )
        assertEquals(listOf(0L, 1L, 2L, 3L), collected.map { it.seq })
    }

    @Test
    fun `latest update is available to late collectors`() = runBlocking {
        AudioPlaybackStateHolder.update(AudioPlaybackState.PAUSED)
        assertEquals(
            AudioPlaybackState.PAUSED,
            AudioPlaybackStateHolder.state.first().state
        )
    }
}
