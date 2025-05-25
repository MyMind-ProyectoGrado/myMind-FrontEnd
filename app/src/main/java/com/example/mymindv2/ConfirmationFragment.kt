package com.example.mymindv2

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.example.mymindv2.databinding.FragmentConfirmationBinding
import com.example.mymindv2.services.audios.AudioRemoteService
import com.example.mymindv2.services.users.UserPreferences
import kotlinx.coroutines.launch
import java.io.File
import android.media.MediaPlayer

class ConfirmationFragment : Fragment() {

    private var _binding: FragmentConfirmationBinding? = null
    private val binding get() = _binding!!

    private var mediaPlayer: MediaPlayer? = null
    private var audioPath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioPath = arguments?.getString("audio_path")

        if (audioPath.isNullOrBlank()) {
            showErrorDialog("No se encontró la grabación para enviar.")
            return
        }

        binding.continueButton.setOnClickListener {
            sendAudioToBackend(audioPath!!)
        }

        binding.cancelButton.setOnClickListener {
            showCancelDialog()
        }

        binding.playAudioButton.setOnClickListener {
            playAudio(audioPath!!)
        }

    }

    private fun sendAudioToBackend(audioPath: String) {
        val file = File(audioPath)
        if (!file.exists()) {
            showErrorDialog("Archivo de audio no encontrado.")
            return
        }

        val token = UserPreferences(requireContext()).getAuthToken()
        if (token.isNullOrEmpty()) {
            showErrorDialog("Token de autenticación no disponible.")
            return
        }

        lifecycleScope.launch {
            try {
                val audioService = AudioRemoteService(token)
                val taskId = audioService.uploadAudio(file)
                file.delete()

                val bundle = Bundle().apply {
                    putString("task_id", taskId)
                }

                parentFragmentManager.commit {
                    replace(R.id.fragment_container, LoadingFragment().apply { arguments = bundle })
                    addToBackStack(null)
                }

            } catch (e: Exception) {
                Log.e("ConfirmationFragment", "Error al enviar el audio", e)
                showErrorDialog("Error al enviar el audio: ${e.localizedMessage}")
            }
        }

    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    fun showExitConfirmation(onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Salir sin guardar?")
            .setMessage("Si sales ahora, perderás tu grabación. ¿Quieres continuar?")
            .setPositiveButton("Sí, salir") { _, _ -> onConfirm() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showCancelDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmación")
            .setMessage("¿Seguro que quieres cancelar? La grabación se perderá y esta acción no se puede deshacer.")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                parentFragmentManager.commit {
                    replace(R.id.fragment_container, RecordingFragment())
                    addToBackStack(null)
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
    private fun playAudio(path: String) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
            try {
                mediaPlayer?.setDataSource(path)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                binding.playAudioButton.text = "Pausar"

                mediaPlayer?.setOnCompletionListener {
                    binding.playAudioButton.text = "Escuchar nuevamente"
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
            } catch (e: Exception) {
                showErrorDialog("No se pudo reproducir el audio.")
            }
        } else if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            binding.playAudioButton.text = "Reanudar"
        } else {
            mediaPlayer?.start()
            binding.playAudioButton.text = "Pausar"

            mediaPlayer?.setOnCompletionListener {
                binding.playAudioButton.text = "Escuchar nuevamente"
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
        _binding = null

    }
}
