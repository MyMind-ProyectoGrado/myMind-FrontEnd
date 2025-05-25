package com.example.mymindv2

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.*
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.mymindv2.databinding.FragmentRecordingBinding
import java.io.File
import java.io.IOException

class RecordingFragment : Fragment() {

    private var _binding: FragmentRecordingBinding? = null
    private val binding get() = _binding!!

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var isPaused = false
    private lateinit var outputFile: File

    private val REQUEST_MIC_PERMISSION = 100
    private var countdownTimer: CountDownTimer? = null
    private val maxDurationMillis = 60_000L
    private var millisRemaining = maxDurationMillis

    private var pendingStartRecording = false

    private val waveformHandler = Handler(Looper.getMainLooper())
    private val waveformRunnable = object : Runnable {
        override fun run() {
            if (isRecording && !isPaused) {
                try {
                    val amplitude = mediaRecorder?.maxAmplitude ?: 0
                    binding.voiceVisualizer.addAmplitude(amplitude.toFloat())
                } catch (_: Exception) {}
                waveformHandler.postDelayed(this, 50)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecordingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.subtitle.text = Html.fromHtml(
            "Según <i>The American Heart Association</i>, desahogarte puede mejorar tu salud mental y física.",
            Html.FROM_HTML_MODE_LEGACY
        )
        setupButtons()
    }

    private fun setupButtons() {
        val documentsDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val myMindDir = File(documentsDir, "MyMind")
        if (!myMindDir.exists()) myMindDir.mkdirs()

        outputFile = File(myMindDir, "recorded_audio.mp3")

        binding.btnStart.setOnClickListener {
            if (checkPermissions()) {
                startRecording()
            } else {
                Toast.makeText(requireContext(), "Debes permitir el acceso al micrófono", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnPause.setOnClickListener { pauseRecording() }
        binding.btnResume.setOnClickListener { resumeRecording() }
        binding.btnDelete.setOnClickListener { deleteRecording() }
        binding.btnConfirm.setOnClickListener { confirmRecording() }
        binding.btnInstructions.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, InstructionsFragment())
                .addToBackStack(null)
                .commit()
        }

        updateUI(State.IDLE)
    }

    private fun startRecording() {
        try {
            if (!checkPermissions()) {
                Toast.makeText(requireContext(), "Permiso de micrófono no concedido", Toast.LENGTH_LONG).show()
                return
            }

            Log.d("RecordingFragment", "📁 Ruta de grabación: ${outputFile.absolutePath}")

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            isPaused = false
            binding.voiceVisualizer.clear()
            startCountdown(millisRemaining)
            waveformHandler.post(waveformRunnable)
            updateUI(State.RECORDING)

        } catch (e: Exception) {
            Log.e("RecordingFragment", "Error al iniciar grabación", e)
            Toast.makeText(requireContext(), "Error al iniciar la grabación: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.pause()
            isPaused = true
            countdownTimer?.cancel()
            updateUI(State.PAUSED)
        }
    }

    private fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.resume()
            isPaused = false
            startCountdown(millisRemaining)
            waveformHandler.post(waveformRunnable)
            updateUI(State.RECORDING)
        }
    }

    private fun deleteRecording() {
        stopRecording(restart = false)
        outputFile.delete()
        binding.voiceVisualizer.clear()
        updateUI(State.IDLE)
        Toast.makeText(requireContext(), "Grabación eliminada", Toast.LENGTH_SHORT).show()
    }

    private fun confirmRecording() {
        stopRecording(restart = false)

        val bundle = Bundle().apply {
            putString("audio_path", outputFile.absolutePath)
        }

        val confirmationFragment = ConfirmationFragment()
        confirmationFragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, confirmationFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun stopRecording(restart: Boolean) {
        try {
            countdownTimer?.cancel()
            waveformHandler.removeCallbacks(waveformRunnable)

            mediaRecorder?.apply {
                stop()
                release()
            }

            mediaRecorder = null
            isRecording = false
            isPaused = false
            millisRemaining = maxDurationMillis

            if (restart) updateUI(State.IDLE)

        } catch (e: Exception) {
            Log.e("RecordingFragment", "❌ Error al detener grabación", e)
            Toast.makeText(requireContext(), "Error al detener la grabación", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCountdown(duration: Long) {
        countdownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                millisRemaining = millisUntilFinished
                binding.textCountdown.text = "Tiempo restante: ${millisUntilFinished / 1000} s"
            }

            override fun onFinish() {
                confirmRecording()
            }
        }.start()
    }

    private fun updateUI(state: State) {
        binding.btnStart.visibility = if (state == State.IDLE) View.VISIBLE else View.GONE
        binding.btnPause.visibility = if (state == State.RECORDING) View.VISIBLE else View.GONE
        binding.btnResume.visibility = if (state == State.PAUSED) View.VISIBLE else View.GONE
        binding.btnDelete.visibility = if (state != State.IDLE) View.VISIBLE else View.GONE
        binding.btnConfirm.visibility = if (state != State.IDLE) View.VISIBLE else View.GONE
        binding.textCountdown.visibility = if (state != State.IDLE) View.VISIBLE else View.GONE
        binding.btnInstructions.visibility = if (state == State.IDLE) View.VISIBLE else View.GONE
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        countdownTimer?.cancel()
        waveformHandler.removeCallbacks(waveformRunnable)
        mediaRecorder?.release()
    }



    private enum class State {
        IDLE, RECORDING, PAUSED
    }
}
