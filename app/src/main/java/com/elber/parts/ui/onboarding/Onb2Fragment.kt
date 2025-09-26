package com.elber.parts.ui.onboarding

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.elber.parts.R

class Onb2Fragment : Fragment(R.layout.fragment_onb2) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<ImageButton>(R.id.btnNext).setOnClickListener {
            findNavController().navigate(R.id.action_onb2_to_onb3)
        }
    }
}
