package com.elber.parts.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.elber.parts.R
import com.elber.parts.data.repo.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.util.Patterns
import kotlinx.coroutines.launch

class SignupFragment : Fragment(R.layout.fragment_signup) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tilEmail = view.findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = view.findViewById<TextInputLayout>(R.id.tilPassword)
        val tilConfirm = view.findViewById<TextInputLayout>(R.id.tilConfirm)

        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirm = view.findViewById<TextInputEditText>(R.id.etConfirm)

        val btnCreate = view.findViewById<MaterialButton>(R.id.btnCreate)
        val tvTos = view.findViewById<TextView>(R.id.tvTos)
        val tvLogin = view.findViewById<TextView>(R.id.tvLogin)

        // Render bold word(s) in the TOS line safely for all API levels
        tvTos.text = HtmlCompat.fromHtml(getString(R.string.tos_prefix), HtmlCompat.FROM_HTML_MODE_LEGACY)

        val repo = UserRepository.from(requireContext())

        btnCreate.setOnClickListener {
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val pass = etPassword.text?.toString().orEmpty()
            val conf = etConfirm.text?.toString().orEmpty()

            var ok = true
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Enter a valid email"
                ok = false
            } else tilEmail.error = null

            if (pass.length < 6) {
                tilPassword.error = "At least 6 characters"
                ok = false
            } else tilPassword.error = null

            if (conf != pass) {
                tilConfirm.error = "Passwords do not match"
                ok = false
            } else tilConfirm.error = null

            if (!ok) return@setOnClickListener

            // Create account in Room
            btnCreate.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val exists = repo.emailExists(email)
                    if (exists) {
                        tilEmail.error = "Email already exists"
                        return@launch
                    }
                    val result = repo.signUp(email, pass)
                    if (result.isSuccess) {
                        // Go back to Login
                        findNavController().navigate(R.id.action_signup_to_login)
                    } else {
                        tilEmail.error = result.exceptionOrNull()?.message ?: "Sign up failed"
                    }
                } finally {
                    btnCreate.isEnabled = true
                }
            }
        }

        tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_signup_to_login)
        }
    }
}
