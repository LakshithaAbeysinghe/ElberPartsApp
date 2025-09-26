package com.elber.parts.ui.onboarding

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.elber.parts.R
import com.elber.parts.util.setSeenOnboard
import kotlinx.coroutines.launch

class Onb3Fragment : Fragment(R.layout.fragment_onb3) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<ImageButton>(R.id.btnNext).setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                setSeenOnboard(requireContext())
                // use the action if you added it; otherwise navigate by destination id
                // findNavController().navigate(R.id.action_onb3_to_login)
                findNavController().navigate(R.id.loginFragment)
            }
        }
    }
}
