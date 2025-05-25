package com.example.mymindv2.services.visualizations

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.mymindv2.services.users.UserPreferences
import com.example.mymindv2.services.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReportFetchService : Service() {

    private val TAG = "ReportFetchService"
    private lateinit var userPreferences: UserPreferences
    private lateinit var visualizationPreferences: VisualizationPreferences
    private lateinit var visualizationRemoteService: VisualizationRemoteService

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        userPreferences = UserPreferences(this)
        val token = userPreferences.getAuthToken() ?: run {
            stopSelf()
            return START_NOT_STICKY
        }
        Log.d(TAG, "Token ${token}")
        visualizationPreferences = VisualizationPreferences(this)
        visualizationRemoteService = VisualizationRemoteService(token)

        Log.d(TAG, "🕐 Iniciando temporizador de 80 segundos...")

        object : CountDownTimer(80000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(TAG, "⏳ Faltan ${millisUntilFinished / 1000} segundos...")
            }

            override fun onFinish() {
                Log.d(TAG, "🚀 Ejecutando fetch de visualizaciones...")

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        visualizationRemoteService.fetchAndSaveUserTranscriptions(visualizationPreferences)
                        visualizationRemoteService.fetchAndSaveLast7DaysAggregated(visualizationPreferences)


                        Log.d(TAG, "✅ Datos guardados correctamente")

                        // Notificar a la UI si está activa
                        val uiIntent = Intent("com.example.mymindv2.REPORT_DATA_READY")
                        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(uiIntent)

                        // Mostrar notificación si la app está cerrada
                        NotificationService.showAnalysisReadyNotification(applicationContext)
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error al obtener visualizaciones: ${e.message}")
                    } finally {
                        stopSelf()
                    }
                }
            }
        }.start()

        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
