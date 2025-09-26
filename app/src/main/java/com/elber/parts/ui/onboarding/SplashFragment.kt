package com.elber.parts.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.elber.parts.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment(R.layout.fragment_splash) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.alpha = 0f
        view.animate().alpha(1f).setDuration(200).start()

        viewLifecycleOwner.lifecycleScope.launch {
            delay(1500)
            findNavController().navigate(R.id.action_splash_to_onb1)
        }
    }
}
