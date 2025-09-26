package com.elber.parts.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.TextView
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

class CreatePasswordFragment : Fragment(R.layout.fragment_create_password) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tilNew = view.findViewById<TextInputLayout>(R.id.tilNewPass)
        val tilConfirm = view.findViewById<TextInputLayout>(R.id.tilConfirmPass)
        val etNew = view.findViewById<TextInputEditText>(R.id.etNewPass)
        val etConfirm = view.findViewById<TextInputEditText>(R.id.etConfirmPass)
        val btn = view.findViewById<MaterialButton>(R.id.btnReset)

        // email passed from ForgotPasswordFragment (may be null if not provided)
        val email = arguments?.getString("email")
        view.findViewById<TextView?>(R.id.tvSubtitle)?.text = email?.let { "Reset password for $it" }

        val repo = UserRepository.from(requireContext())

        btn.setOnClickListener {
            val pass = etNew.text?.toString().orEmpty()
            val confirm = etConfirm.text?.toString().orEmpty()

            var ok = true
            if (pass.length < 6) {
                tilNew.error = "At least 6 characters"
                ok = false
            } else tilNew.error = null

            if (confirm != pass) {
                tilConfirm.error = "Passwords do not match"
                ok = false
            } else tilConfirm.error = null

            if (!ok) return@setOnClickListener

            if (email.isNullOrBlank()) {
                Snackbar.make(view, "Missing email", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btn.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val updated = repo.resetPassword(email, pass)
                    if (updated) {
                        Snackbar.make(
                            view,
                            getString(R.string.password_reset_success),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        // Navigate back to Login and clear CreatePassword from back stack
                        findNavController().navigate(R.id.action_createPassword_to_login)
                    } else {
                        tilNew.error = "Could not update password"
                    }
                } finally {
                    btn.isEnabled = true
                }
            }
        }
    }
}
