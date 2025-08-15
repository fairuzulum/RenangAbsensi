// ui/home/MainActivity.kt
package com.coachbro.absenrenang.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.coachbro.absenrenang.databinding.ActivityMainBinding
import com.coachbro.absenrenang.ui.register.RegisterActivity
import com.coachbro.absenrenang.ui.studentlist.StudentListActivity
import com.coachbro.absenrenang.ui.payment.PaymentActivity
import com.coachbro.absenrenang.ui.attendance.AttendanceActivity

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

        binding.btnListSiswa.setOnClickListener {
            val intent = Intent(this, StudentListActivity::class.java)
            startActivity(intent)
        }

        binding.btnPembayaran.setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }

        binding.btnAbsensi.setOnClickListener {
            startActivity(Intent(this, AttendanceActivity::class.java))
        }



        // Listener untuk tombol lain akan kita tambahkan di tahap berikutnya
        // binding.btnAbsensi.setOnClickListener { ... }
        // binding.btnPembayaran.setOnClickListener { ... }
        // binding.btnListSiswa.setOnClickListener { ... }
    }
}