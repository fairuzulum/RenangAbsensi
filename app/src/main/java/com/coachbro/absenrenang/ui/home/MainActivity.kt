package com.coachbro.absenrenang.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log // <-- 1. Impor Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.coachbro.absenrenang.data.model.MenuPasswords
import com.coachbro.absenrenang.data.model.MenuSetting
import com.coachbro.absenrenang.databinding.ActivityMainBinding
import com.coachbro.absenrenang.databinding.DialogPinEntryBinding
import com.coachbro.absenrenang.ui.attendance.AttendanceActivity
import com.coachbro.absenrenang.ui.financial.FinancialReportActivity
import com.coachbro.absenrenang.ui.payment.PaymentActivity
import com.coachbro.absenrenang.ui.register.RegisterActivity
import com.coachbro.absenrenang.ui.studentlist.StudentListActivity
import com.coachbro.absenrenang.viewmodel.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    private var menuPasswords: MenuPasswords? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        mainViewModel.menuPasswords.observe(this) { passwords ->
            this.menuPasswords = passwords
            // ===============================================================
            // LOGGING DITAMBAHKAN DI SINI
            // ===============================================================
            if (passwords != null) {
                Log.d("PinDebug", "Pengaturan PIN berhasil dimuat: $passwords")
            } else {
                Log.e("PinDebug", "Pengaturan PIN gagal dimuat atau tidak ada (null).")
            }
            // ===============================================================
        }

        mainViewModel.errorMessage.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClickListeners() {
        // Menu Register Siswa
        binding.cardRegister.setOnClickListener {
            handleMenuClick(
                setting = menuPasswords?.register,
                intent = Intent(this, RegisterActivity::class.java),
                menuName = "Register"
            )
        }

        // Menu Daftar Member FSS
        binding.cardListSiswa.setOnClickListener {
            handleMenuClick(
                setting = menuPasswords?.listSiswa,
                intent = Intent(this, StudentListActivity::class.java),
                menuName = "List Siswa"
            )
        }

        // Menu Pembayaran
        binding.cardPembayaran.setOnClickListener {
            handleMenuClick(
                setting = menuPasswords?.pembayaran,
                intent = Intent(this, PaymentActivity::class.java),
                menuName = "Pembayaran"
            )
        }

        // Menu Absen Kehadiran
        binding.cardAbsensi.setOnClickListener {
            handleMenuClick(
                setting = menuPasswords?.absensi,
                intent = Intent(this, AttendanceActivity::class.java),
                menuName = "Absensi"
            )
        }

        // Menu Keuangan
        binding.cardFinancial.setOnClickListener {
            handleMenuClick(
                setting = menuPasswords?.keuangan,
                intent = Intent(this, FinancialReportActivity::class.java),
                menuName = "Keuangan"
            )
        }
    }

    private fun handleMenuClick(setting: MenuSetting?, intent: Intent, menuName: String) {
        // ===============================================================
        // LOGGING DITAMBAHKAN DI SINI
        // ===============================================================
        Log.d("PinDebug", "Menu '$menuName' diklik. Setting: isEnabled=${setting?.isEnabled}, pin=${setting?.pin}")
        // ===============================================================

        if (setting == null || !setting.isEnabled) {
            startActivity(intent)
            return
        }

        showPinDialog(setting.pin) {
            startActivity(intent)
        }
    }

    private fun showPinDialog(correctPin: String, onSuccess: () -> Unit) {
        val dialogBinding = DialogPinEntryBinding.inflate(LayoutInflater.from(this))
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton("Buka", null)
            .setNegativeButton("Batal") { d, _ -> d.dismiss() }
            .show()

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val enteredPin = dialogBinding.etPin.text.toString()

            if (enteredPin == correctPin) {
                dialog.dismiss()
                onSuccess()
            } else {
                dialogBinding.tilPin.error = "PIN Salah!"
            }
        }
    }
}