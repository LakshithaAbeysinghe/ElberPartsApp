package com.elber.parts.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.elber.parts.R
import com.elber.parts.data.repo.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tilEmail = view.findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = view.findViewById<TextInputLayout>(R.id.tilPassword)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        val tvForgot = view.findViewById<TextView>(R.id.tvForgot)
        val tvSignUp = view.findViewById<TextView>(R.id.tvSignUp)

        val repo = UserRepository.from(requireContext())

        btnLogin.setOnClickListener {
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val pass = etPassword.text?.toString().orEmpty()

            var ok = true
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Enter a valid email"
                ok = false
            } else tilEmail.error = null

            if (pass.length < 6) {
                tilPassword.error = "At least 6 characters"
                ok = false
            } else tilPassword.error = null

            if (!ok) return@setOnClickListener

            btnLogin.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val success = repo.login(email, pass)
                    if (success) {
                        findNavController().navigate(R.id.action_login_to_homeFragment)
                    } else {
                        tilPassword.error = "Invalid email or password"
                    }
                } finally {
                    btnLogin.isEnabled = true
                }
            }
        }

        tvForgot.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgotPassword)
        }

        tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signupFragment)
        }
    }
}
