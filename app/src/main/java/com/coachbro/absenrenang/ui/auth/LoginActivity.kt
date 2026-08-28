package com.coachbro.absenrenang.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.doAfterTextChanged
import com.coachbro.absenrenang.databinding.ActivityLoginBinding
import com.coachbro.absenrenang.ui.home.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var isLoginLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        setupAnimations()
        setupListeners()
    }

    private fun setupAnimations() {
        // Initial state
        binding.cardLogin.translationY = 100f
        binding.cardLogin.alpha = 0f
        binding.viewTop.translationY = -200f

        // Animate
        binding.viewTop.animate()
            .translationY(0f)
            .setDuration(800)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        binding.cardLogin.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(200)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            if (!isLoginLoading) handleLogin()
        }

        // Real-time validation
        binding.etEmail.doAfterTextChanged {
            binding.tilEmail.error = null
        }
        binding.etPassword.doAfterTextChanged {
            binding.tilPassword.error = null
        }
    }

    override fun onStart() {
        super.onStart()
        binding.etPassword.text = null
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email tidak boleh kosong"
            return
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password tidak boleh kosong"
            return
        }

        setLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    com.coachbro.absenrenang.utils.SessionManager(this).setLoggedIn(true)
                    Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SetPinActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                } else {
                    setLoading(false)
                    Toast.makeText(this, "Login Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        this.isLoginLoading = isLoading
        binding.loadingLayout.root.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.text = if (isLoading) "Mohon tunggu..." else "Login"
        binding.btnLogin.alpha = if (isLoading) 0.8f else 1.0f
        binding.tilEmail.isEnabled = !isLoading
        binding.tilPassword.isEnabled = !isLoading
    }
}