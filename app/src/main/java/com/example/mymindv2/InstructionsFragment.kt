package com.example.mymindv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mymindv2.databinding.FragmentInstructionsBinding
import com.example.mymindv2.services.users.UserPreferences

class InstructionsFragment : Fragment() {

    private var _binding: FragmentInstructionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstructionsBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())

        binding.btnContinue.setOnClickListener {
            parentFragmentManager.setFragmentResult("instructions_completed", Bundle())
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RecordingFragment()) // Cambia si vas a otro fragmento
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
