package com.elber.parts.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.elber.parts.R
import com.elber.parts.data.repo.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tilEmail = view.findViewById<TextInputLayout>(R.id.tilEmail)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val btn = view.findViewById<MaterialButton>(R.id.btnSubmit)

        val repo = UserRepository.from(requireContext())

        btn.setOnClickListener {
            val email = etEmail.text?.toString()?.trim().orEmpty()
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Enter a valid email"
                return@setOnClickListener
            }
            tilEmail.error = null

            btn.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val exists = repo.emailExists(email)
                    if (!exists) {
                        tilEmail.error = "Email not found"
                        return@launch
                    }

                    // Optional feedback
                    Snackbar.make(
                        view,
                        getString(R.string.reset_link_sent, email),
                        Snackbar.LENGTH_SHORT
                    ).show()

                    // Navigate to Create Password, pass the email
                    val args = bundleOf("email" to email)
                    findNavController().navigate(R.id.action_forgotPassword_to_createPassword, args)
                } finally {
                    btn.isEnabled = true
                }
            }
        }
    }
}
