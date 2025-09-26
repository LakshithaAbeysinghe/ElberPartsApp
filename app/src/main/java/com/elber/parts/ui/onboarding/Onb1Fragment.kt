package com.elber.parts.ui.onboarding

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.elber.parts.R
import androidx.core.content.ContextCompat

class Onb1Fragment : Fragment(R.layout.fragment_onb1) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make status bar white for consistency with onboarding background
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.white)

        // Navigate to Onboarding 2
        view.findViewById<ImageButton>(R.id.btnNext).setOnClickListener {
            findNavController().navigate(R.id.action_onb1_to_onb2)
        }
    }
}
