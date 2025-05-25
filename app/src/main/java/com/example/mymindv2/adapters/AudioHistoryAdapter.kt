package com.example.mymindv2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymindv2.databinding.ItemAudioBinding
import com.example.mymindv2.models.visualizations.TranscriptionItem

class AudioHistoryAdapter(
    private var items: List<TranscriptionItem>,
    private val onDelete: (TranscriptionItem) -> Unit,
    private val onViewReport: (TranscriptionItem) -> Unit
) : RecyclerView.Adapter<AudioHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAudioBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TranscriptionItem) {
            val dateOnly = item.transcription_date.split("T").firstOrNull() ?: item.transcription_date

            binding.tvFecha.text = dateOnly
            binding.tvHora.text = item.transcription_time

            binding.btnEliminar.setOnClickListener {
                onDelete(item)
            }

            binding.btnVerReporte.setOnClickListener {
                onViewReport(item)
            }
        }
    }

    fun updateList(newList: List<TranscriptionItem>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
