// ui/home/MainActivity.kt
package com.coachbro.absenrenang.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.coachbro.absenrenang.databinding.ActivityMainBinding
import com.coachbro.absenrenang.ui.register.RegisterActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegisterSiswa.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Listener untuk tombol lain akan kita tambahkan di tahap berikutnya
        // binding.btnAbsensi.setOnClickListener { ... }
        // binding.btnPembayaran.setOnClickListener { ... }
        // binding.btnListSiswa.setOnClickListener { ... }
    }
}