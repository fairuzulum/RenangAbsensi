// ui/home/MainActivity.kt
package com.coachbro.absenrenang.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.coachbro.absenrenang.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // Deklarasikan variabel binding. 'lateinit' berarti kita akan menginisialisasinya nanti.
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Inisialisasi binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Set content view menggunakan root dari binding
        setContentView(binding.root)

        // Di tahap selanjutnya, kita akan menambahkan fungsi klik pada tombol-tombol ini.
        // Contoh: binding.btnAbsensi.setOnClickListener { ... }
    }
}