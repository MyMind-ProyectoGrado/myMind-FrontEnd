package com.example.mymindv2

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymindv2.adapters.AudioHistoryAdapter
import com.example.mymindv2.databinding.FragmentAudioHistoryBinding
import com.example.mymindv2.models.visualizations.TranscriptionItem
import com.example.mymindv2.services.audios.AudioRemoteService
import com.example.mymindv2.services.users.UserPreferences
import com.example.mymindv2.services.visualizations.VisualizationPreferences
import com.example.mymindv2.services.visualizations.VisualizationRemoteService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AudioHistoryFragment : Fragment() {

    private var _binding: FragmentAudioHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferences: VisualizationPreferences
    private lateinit var visualizationRemoteService: VisualizationRemoteService
    private lateinit var audioRemoteService: AudioRemoteService
    private lateinit var userPrefs: UserPreferences
    private lateinit var adapter: AudioHistoryAdapter
    private var transcriptions = mutableListOf<TranscriptionItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPrefs = UserPreferences(requireContext())
        preferences = VisualizationPreferences(requireContext())

        val token = userPrefs.getAuthToken() ?: ""
        visualizationRemoteService = VisualizationRemoteService(token)
        audioRemoteService = AudioRemoteService(token)

        loadData()

        binding.btnDeleteAll.setOnClickListener {
            showConfirmationDialogAll()
        }
    }

    private fun loadData() {
        transcriptions = preferences.getTranscriptions()?.toMutableList() ?: mutableListOf()
        transcriptions.sortByDescending {
            "${it.transcription_date} ${it.transcription_time}"
        }

        if (transcriptions.isEmpty()) {
            binding.tvEmptyMessage.visibility = View.VISIBLE
            binding.recyclerViewAudios.visibility = View.GONE
            binding.btnDeleteAll.visibility = View.GONE
        } else {
            binding.tvEmptyMessage.visibility = View.GONE
            binding.recyclerViewAudios.visibility = View.VISIBLE
            binding.btnDeleteAll.visibility = View.VISIBLE
        }

        if (!::adapter.isInitialized) {
            adapter = AudioHistoryAdapter(transcriptions,
                onDelete = { item -> showConfirmationDialog(item) },
                onViewReport = { item -> fetchAndShowReport(item) }
            )

            binding.recyclerViewAudios.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerViewAudios.adapter = adapter
        } else {
            adapter.updateList(transcriptions)
        }
    }

    private fun refreshAllFromBackend() {
        lifecycleScope.launch {
            visualizationRemoteService.fetchAndSaveUserTranscriptions(preferences)
            visualizationRemoteService.fetchAndSaveLast7DaysAggregated(preferences)

            withContext(Dispatchers.Main) {
                if (_binding != null && isAdded) {
                    Log.d("AudioHistoryFragment", "✅ Refrescando UI después del fetch")
                    loadData()
                    parentFragmentManager.setFragmentResult("view_report", Bundle.EMPTY)
                } else {
                    Log.w("AudioHistoryFragment", "⚠️ Fragmento no activo, no se actualiza UI")
                }
            }
        }
    }

    private fun showConfirmationDialog(item: TranscriptionItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Eliminar audio?")
            .setMessage("Esta acción no se puede deshacer")
            .setPositiveButton("Sí") { _, _ -> deleteAudio(item) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showConfirmationDialogAll() {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Eliminar todos los audios?")
            .setMessage("Esta acción no se puede deshacer")
            .setPositiveButton("Sí") { _, _ -> deleteAllAudios() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteAudio(item: TranscriptionItem) {
        lifecycleScope.launch {
            val success = audioRemoteService.deleteSingleTranscription(item.transcription_id)
            if (success) {
                Toast.makeText(requireContext(), "Audio eliminado", Toast.LENGTH_SHORT).show()

                val position = transcriptions.indexOfFirst { it.transcription_id == item.transcription_id }
                if (position != -1) {
                    transcriptions.removeAt(position)
                    adapter.updateList(transcriptions)
                    updateDeleteAllVisibility()

                    parentFragmentManager.setFragmentResult("hide_report", Bundle.EMPTY)

                    launch {
                        delay(3_000)
                        refreshAllFromBackend()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteAllAudios() {
        lifecycleScope.launch {
            val success = audioRemoteService.deleteAllTranscriptions()
            if (success) {
                Toast.makeText(requireContext(), "Audios eliminados", Toast.LENGTH_SHORT).show()

                transcriptions.clear()
                adapter.updateList(transcriptions)
                updateDeleteAllVisibility()
                preferences.clearAll()
                parentFragmentManager.setFragmentResult("hide_report", Bundle.EMPTY)

                launch {
                    delay(3_000)
                    refreshAllFromBackend()
                }
            } else {
                Toast.makeText(requireContext(), "Error al eliminar todos", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateDeleteAllVisibility() {
        binding.btnDeleteAll.visibility = if (transcriptions.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun fetchAndShowReport(item: TranscriptionItem) {
        lifecycleScope.launch {
            try {
                val audioResult = audioRemoteService.getTranscriptionById(item.transcription_id)
                if (audioResult != null) {
                    VisualizationPreferences(requireContext()).saveLatestTranscriptionResult(audioResult)

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SpecificReportFragment())
                        .addToBackStack(null)
                        .commit()
                } else {
                    Toast.makeText(requireContext(), "No se pudo obtener el reporte", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("AudioHistoryFragment", "❌ Error al obtener reporte", e)
                Toast.makeText(requireContext(), "Error al obtener el reporte", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        userPrefs = UserPreferences(requireContext())
        preferences = VisualizationPreferences(requireContext())

        val token = userPrefs.getAuthToken() ?: ""
        visualizationRemoteService = VisualizationRemoteService(token)
        audioRemoteService = AudioRemoteService(token)
        loadData()

        refreshAllFromBackend()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
