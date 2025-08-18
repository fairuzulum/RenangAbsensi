package com.coachbro.absenrenang.ui.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.coachbro.absenrenang.databinding.ActivityLoginBinding
import com.coachbro.absenrenang.ui.home.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        sharedPreferences = getSharedPreferences("SwimTrackPrefs", Context.MODE_PRIVATE)

        // ===============================================================
        // KODE UNTUK LOGIN OTOMATIS TELAH DIHAPUS DARI BAGIAN INI
        // Aplikasi sekarang akan selalu menampilkan halaman login.
        // ===============================================================

        binding.btnLogin.setOnClickListener {
            handleLogin()
        }
    }

    override fun onStart() {
        super.onStart()
        // Cek email setiap kali activity ini terlihat oleh pengguna
        checkIfEmailIsSaved()

        // Selalu pastikan field password kosong saat halaman muncul
        binding.etPassword.text = null
    }

    private fun checkIfEmailIsSaved() {
        val savedEmail = sharedPreferences.getString("USER_EMAIL", null)
        if (savedEmail != null) {
            binding.tvLoginEmail.text = savedEmail
            binding.tvLoginEmail.visibility = View.VISIBLE
            binding.tilEmail.visibility = View.GONE
        } else {
            binding.tvLoginEmail.visibility = View.GONE
            binding.tilEmail.visibility = View.VISIBLE
        }
    }

    private fun handleLogin() {
        val savedEmail = sharedPreferences.getString("USER_EMAIL", null)
        val email = savedEmail ?: binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password tidak boleh kosong.", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                    // Kita tetap menyimpan email untuk kemudahan pengguna
                    sharedPreferences.edit().putString("USER_EMAIL", email).apply()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }
}