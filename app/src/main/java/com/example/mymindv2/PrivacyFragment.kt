package com.example.mymindv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mymindv2.databinding.FragmentPrivacyBinding

class PrivacyFragment : Fragment() {

    private var _binding: FragmentPrivacyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadTermsAndConditions()

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    private fun loadTermsAndConditions() {
        val inputStream = resources.openRawResource(R.raw.terms)
        val termsText = inputStream.bufferedReader().use { it.readText() }
        binding.tvTerms.text = termsText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
