import com.example.mymindv2.models.audios.TranscriptionResult

import com.example.mymindv2.services.audios.AudioRemoteService
import com.example.mymindv2.services.audios.AudioService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import retrofit2.Response
import java.io.File

@ExperimentalCoroutinesApi
class AudioRemoteServiceTest {
/*
    private lateinit var service: AudioService
    private lateinit var audioRemoteService: AudioRemoteService

    @Before
    fun setup() {
        service = mock()
        audioRemoteService = AudioRemoteService("token123", service)
    }

    @Test
    fun `uploadAudio returns task_id on success`() = runTest {
        val file = File("dummy.m4a")
        val expected = "abc123"
        val dummyResponse = UploadResponse(task_id = expected)

        whenever(service.uploadAudioFile(any(), any()))
            .thenReturn(Response.success(dummyResponse))

        val result = audioRemoteService.uploadAudio(file)
        assertEquals(expected, result)
    }

    @Test
    fun `deleteSingleTranscription returns true`() = runTest {
        whenever(service.deleteTranscription(any(), any()))
            .thenReturn(Response.success(null))
        val result = audioRemoteService.deleteSingleTranscription("123")
        assertTrue(result)
    }

    @Test
    fun `getTranscriptionById returns result`() = runTest {
        val expected = TranscriptionResult("neutral", "positivo", "Buen trabajo")
        whenever(service.getTranscriptionById(any(), any()))
            .thenReturn(Response.success(expected.let { mapOf("result" to it) }))

        // Como getTranscriptionById espera el campo `.result`, debes ajustar mock si estás usando wrapper
        // Aquí puedes adaptarlo según el tipo de respuesta real
    }

 */
}
