package com.example.mymindv2

import android.content.*
import android.os.*
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.mymindv2.databinding.FragmentLoadingBinding
import com.example.mymindv2.models.audios.TranscriptionResult
import com.example.mymindv2.services.LoadingStatusManager
import com.example.mymindv2.services.NotificationService
import com.example.mymindv2.services.RetrofitClient
import com.example.mymindv2.services.audios.AudioService
import com.example.mymindv2.services.visualizations.VisualizationPreferences
import com.example.mymindv2.services.users.UserPreferences
import com.example.mymindv2.services.visualizations.VisualizationRemoteService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.awaitResponse


class LoadingFragment : Fragment() {

    private var _binding: FragmentLoadingBinding? = null
    private val binding get() = _binding!!
    private val handler = Handler(Looper.getMainLooper())

    private val TIMEOUT_MS = 120000L
    private var hasTimedOut = false
    private var timeoutRunnable: Runnable? = null

    private val loadingMessages = listOf(
        "Estamos procesando tu audio, esto podría tardar unos minutos.",
        "Gracias por tu paciencia, esto se tardará unos minutos.",
        "Analizando la información…",
        "Estamos trabajando en ello, por favor espera.",
        "¡Casi listo! Solo un poco más…"
    )
    private var messageIndex = 0

    private val reportDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.mymindv2.REPORT_DATA_READY" && !hasTimedOut) {
                Log.d("LoadingFragment", "📊 Datos de reporte recibidos. Navegando...")
                handler.removeCallbacks(timeoutRunnable!!)
                requireActivity().runOnUiThread {
                    LoadingStatusManager.setLoading(requireContext(), false)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SpecificReportFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userPrefs = UserPreferences(requireContext())
        val token = userPrefs.getAuthToken()
        val taskId = arguments?.getString("task_id")

        if (token.isNullOrBlank() || taskId.isNullOrBlank()) {
            showErrorAndGoBack("Error de conexión, verifica tu conexión a internet.")
            return
        }

        LoadingStatusManager.setLoading(requireContext(), true)

        val pulseAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse)
        binding.appLogo.startAnimation(pulseAnimation)

        startTextRotation()
        startTimeoutFallback()
        startPollingForResult(taskId, token)
    }

    private fun startPollingForResult(taskId: String, token: String) {
        lifecycleScope.launch {
            try {
                val result = pollForTranscription(taskId, token)
                Log.d("LoadingFragment", "✅ Transcripción recibida: ${result.text}")

                // Guardar en SharedPreferences
                val prefs = VisualizationPreferences(requireContext())
                prefs.saveLatestTranscriptionResult(result)

                // Actualizar visualizaciones y transcripciones completas
                val visualizationRemoteService = VisualizationRemoteService(token)
                visualizationRemoteService.fetchAndSaveUserTranscriptions(prefs)
                visualizationRemoteService.fetchAndSaveLast7DaysAggregated(prefs)

                // Emitir broadcast para navegación
                val intent = Intent("com.example.mymindv2.REPORT_DATA_READY")
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)

                // Mostrar notificación
                NotificationService.showAnalysisReadyNotification(requireContext())


            } catch (e: Exception) {
                Log.e("LoadingFragment", "❌ Error al obtener transcripción", e)
                showErrorAndGoBack("Error al procesar el audio. Intenta nuevamente.")
            }
        }
    }

    private suspend fun pollForTranscription(taskId: String, token: String): TranscriptionResult {
        val service = RetrofitClient.getInstance().create(AudioService::class.java)

        repeat(30) { attempt ->
            try {
                val response = service.getTranscriptionResult("Bearer $token", taskId).awaitResponse()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.result != null) {
                        return body.result
                    }
                } else {
                    Log.w("LoadingFragment", "⚠️ Respuesta sin éxito: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("LoadingFragment", "❌ Error durante polling: ${e.message}")
            }

            Log.d("LoadingFragment", "⏳ Intento ${attempt + 1}: sin resultado aún")
            delay(4000)
        }

        throw Exception("Timeout esperando transcripción.")
    }


    private fun startTextRotation() {
        handler.post(object : Runnable {
            override fun run() {
                binding.loadingMessage.text = loadingMessages[messageIndex]
                messageIndex = (messageIndex + 1) % loadingMessages.size
                handler.postDelayed(this, 20000)
            }
        })
    }

    private fun startTimeoutFallback() {
        timeoutRunnable = Runnable {
            hasTimedOut = true
            Log.e("LoadingFragment", "⏰ Timeout alcanzado")
            LoadingStatusManager.setLoading(requireContext(), false)
            Toast.makeText(requireContext(), "Demoró demasiado. Intenta nuevamente.", Toast.LENGTH_LONG).show()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RecordingFragment())
                .commit()
        }
        handler.postDelayed(timeoutRunnable!!, TIMEOUT_MS)
    }

    private fun showErrorAndGoBack(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, RecordingFragment())
            .commit()
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("com.example.mymindv2.REPORT_DATA_READY")
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(reportDataReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(reportDataReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacksAndMessages(null)
    }
}
