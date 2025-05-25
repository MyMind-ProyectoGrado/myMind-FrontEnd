package com.example.mymindv2.services

import com.example.mymindv2.models.visualizations.TranscriptionItem
import com.example.mymindv2.services.visualizations.VisualizationPreferences
import com.example.mymindv2.services.visualizations.VisualizationRemoteService
import com.example.mymindv2.services.visualizations.VisualizationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import retrofit2.Response

@ExperimentalCoroutinesApi
class VisualizationRemoteServiceTest {
/*
    private lateinit var service: VisualizationService
    private lateinit var preferences: VisualizationPreferences
    private lateinit var remoteService: VisualizationRemoteService

    @Before
    fun setup() {
        service = mock()
        preferences = mock()
        remoteService = VisualizationRemoteService("token123").also {
            it.service = service
        }
    }

    @Test
    fun `fetchAndSaveUserTranscriptions guarda la lista correctamente`() = runTest {
        val dummyList = listOf(
            TranscriptionItem("1", "2025-05-11", "10:30"),
            TranscriptionItem("2", "2025-05-10", "18:00")
        )
        whenever(service.getUserTranscriptions("Bearer token123")).thenReturn(Response.success(dummyList))

        remoteService.fetchAndSaveUserTranscriptions(preferences)

        verify(preferences).saveTranscriptions(dummyList)
    }*/
}
