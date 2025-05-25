package com.example.mymindv2.services

import com.example.mymindv2.models.users.*
import com.example.mymindv2.services.users.UserPreferences
import com.example.mymindv2.services.users.UserRemoteService
import com.example.mymindv2.services.users.UserService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.awaitResponse

@ExperimentalCoroutinesApi
class UserRemoteServiceTest {

    private lateinit var service: UserService
    private lateinit var remoteService: UserRemoteService
    private lateinit var preferences: UserPreferences

    @Before
    fun setup() {
        service = mock()
        preferences = mock()
        remoteService = UserRemoteService("Bearer token123", service)
    }

    @Test
    fun `fetchAndSaveUserProfile guarda datos correctamente en preferencias`() = runTest {
        val mockResponse = FullUserRequest(
            email = "test@example.com",
            name = "Test User",
            profilePic = "https://example.com/image.jpg",
            birthdate = "2000-01-01",
            city = "Bogotá",
            university = "Javeriana",
            degree = "Ingeniería",
            gender = "Femenino",
            personality = "Introvertido"
        )

        val callMock = mock<Call<FullUserRequest>>()
        whenever(callMock.awaitResponse()).thenReturn(Response.success(mockResponse))
        whenever(service.getUserProfile("Bearer token123")).thenReturn(callMock)

        remoteService.fetchAndSaveUserProfile(preferences)

        verify(preferences).saveFullUserData(
            email = mockResponse.email,
            name = mockResponse.name,
            profileImageUri = mockResponse.profilePic,
            city = mockResponse.city,
            university = mockResponse.university,
            career = mockResponse.degree,
            gender = mockResponse.gender,
            personality = mockResponse.personality,
            birthday = mockResponse.birthdate
        )
    }


    @Test
    fun `fetchNotificationSettings devuelve true cuando las notificaciones están activadas`() = runTest {
        val response = NotificationSettingsResponse(notifications = true)

        val callMock = mock<Call<NotificationSettingsResponse>>()
        whenever(callMock.awaitResponse()).thenReturn(Response.success(response))
        whenever(service.getNotificationSettings("Bearer token123")).thenReturn(callMock)

        val (success, state) = remoteService.fetchNotificationSettings()

        assertTrue(success)
        assertTrue(state)
    }

    @Test
    fun `formatDate convierte fechas correctamente`() {
        val result = remoteService.formatDate("5/11/2000")
        assertEquals("2000-11-05", result)
    }

    @Test
    fun `getCurrentFormattedTimestamp retorna un string con formato ISO`() {
        val ts = remoteService.getCurrentFormattedTimestamp()
        assertTrue(ts.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}.*")))
    }

    @Test
    fun `deleteUserFromBackend lanza excepción si falla`() {
        runBlocking {
            val callMock = mock<Call<Void>>()
            whenever(callMock.awaitResponse()).thenReturn(Response.error(500, ResponseBody.create(null, "Error")))
            whenever(service.deleteUserFromBackend("Bearer token123")).thenReturn(callMock)

            assertThrows(Exception::class.java) {
                runBlocking {
                    remoteService.deleteUserFromBackend()
                }
            }
        }
    }

    @Test
    fun `updateUserName hace llamada correcta`() = runTest {
        val callMock = mock<Call<Void>>()
        whenever(callMock.awaitResponse()).thenReturn(Response.success(null))
        whenever(service.updateUserName("Nuevo", "Bearer token123")).thenReturn(callMock)

        remoteService.updateUserName("Nuevo")

        verify(service).updateUserName("Nuevo", "Bearer token123")
    }

    @Test
    fun `updateUserProfilePic hace llamada correcta`() = runTest {
        val uri = "https://cloudinary.com/img.jpg"
        val callMock = mock<Call<Void>>()
        whenever(callMock.awaitResponse()).thenReturn(Response.success(null))
        whenever(service.updateUserProfilePic("Bearer token123", mapOf("profilePic" to uri))).thenReturn(callMock)

        remoteService.updateUserProfilePic(uri)

        verify(service).updateUserProfilePic("Bearer token123", mapOf("profilePic" to uri))
    }

    @Test
    fun `toggleNotificationSettings lanza error si la respuesta falla`() {
        runBlocking {
            val callMock = mock<Call<Void>>()
            whenever(callMock.awaitResponse()).thenReturn(Response.error(403, ResponseBody.create(null, "Prohibido")))
            whenever(service.toggleNotifications("Bearer token123")).thenReturn(callMock)

            assertThrows(Exception::class.java) {
                runBlocking {
                    remoteService.toggleNotificationSettings()
                }
            }
        }
    }
}
