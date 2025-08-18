// ui/home/MainActivity.kt
package com.coachbro.absenrenang.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
// import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen <-- Hapus impor ini jika ada
import com.coachbro.absenrenang.databinding.ActivityMainBinding
import com.coachbro.absenrenang.ui.attendance.AttendanceActivity
import com.coachbro.absenrenang.ui.financial.FinancialReportActivity
import com.coachbro.absenrenang.ui.payment.PaymentActivity
import com.coachbro.absenrenang.ui.register.RegisterActivity
import com.coachbro.absenrenang.ui.studentlist.StudentListActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen() <-- HAPUS BARIS INI
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.cardRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.cardListSiswa.setOnClickListener {
            startActivity(Intent(this, StudentListActivity::class.java))
        }

        binding.cardPembayaran.setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }

        binding.cardAbsensi.setOnClickListener {
            startActivity(Intent(this, AttendanceActivity::class.java))
        }

        binding.cardFinancial.setOnClickListener {
            startActivity(Intent(this, FinancialReportActivity::class.java))
        }
    }
}