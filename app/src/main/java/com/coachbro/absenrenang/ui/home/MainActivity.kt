package com.coachbro.absenrenang.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.coachbro.absenrenang.R
import com.coachbro.absenrenang.data.model.MenuPasswords
import com.coachbro.absenrenang.data.model.MenuSetting
import com.coachbro.absenrenang.databinding.ActivityMainBinding
import com.coachbro.absenrenang.databinding.DialogPinEntryBinding
import com.coachbro.absenrenang.ui.attendance.AttendanceActivity
import com.coachbro.absenrenang.ui.auth.LoginActivity
import com.coachbro.absenrenang.ui.financial.FinancialReportActivity
import com.coachbro.absenrenang.ui.payment.PaymentActivity
import com.coachbro.absenrenang.ui.register.RegisterActivity
import com.coachbro.absenrenang.ui.studentlist.StudentListActivity
import com.coachbro.absenrenang.viewmodel.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

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
            if (passwords != null) {
                Log.d("PinDebug", "Pengaturan PIN berhasil dimuat: $passwords")
            } else {
                Log.e("PinDebug", "Pengaturan PIN gagal dimuat atau tidak ada (null).")
            }
        }

        mainViewModel.errorMessage.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClickListeners() {
        // ... (ClickListener untuk menu-menu lain biarkan seperti semula)
        binding.cardRegister.setOnClickListener {
            handleMenuClick(
                setting = menuPasswords?.register,
                intent = Intent(this, RegisterActivity::class.java),
                menuName = "Register"
            )
        }

        binding.cardListSiswa.setOnClickListener {
            handleMenuClick(
                setting = menuPasswords?.listSiswa,
                intent = Intent(this, StudentListActivity::class.java),
                menuName = "List Siswa"
            )
        }

        binding.cardPembayaran.setOnClickListener {
            handleMenuClick(
                setting = menuPasswords?.pembayaran,
                intent = Intent(this, PaymentActivity::class.java),
                menuName = "Pembayaran"
            )
        }

        binding.cardAbsensi.setOnClickListener {
            handleMenuClick(
                setting = menuPasswords?.absensi,
                intent = Intent(this, AttendanceActivity::class.java),
                menuName = "Absensi"
            )
        }

        binding.cardFinancial.setOnClickListener {
            handleMenuClick(
                setting = menuPasswords?.keuangan,
                intent = Intent(this, FinancialReportActivity::class.java),
                menuName = "Keuangan"
            )
        }

        // ===============================================================
        // TAMBAHKAN CLICK LISTENER UNTUK IKON LOGOUT DI SINI
        // ===============================================================
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
        // ===============================================================
    }

    // ... (fungsi handleMenuClick dan showPinDialog biarkan seperti semula)
    private fun handleMenuClick(setting: MenuSetting?, intent: Intent, menuName: String) {
        Log.d("PinDebug", "Menu '$menuName' diklik. Setting: isEnabled=${setting?.isEnabled}, pin=${setting?.pin}")

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

    // ===============================================================
    // FUNGSI BARU UNTUK MENAMPILKAN DIALOG KONFIRMASI LOGOUT
    // ===============================================================
    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari akun Anda?")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Ya, Keluar") { _, _ ->
                performLogout()
            }
            .show()
    }

    private fun performLogout() {
        // Logout dari Firebase Auth
        FirebaseAuth.getInstance().signOut()

        // Hapus email yang tersimpan di SharedPreferences
        val sharedPreferences = getSharedPreferences("SwimTrackPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("USER_EMAIL").apply()

        // Pindah ke LoginActivity dan bersihkan semua activity sebelumnya
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    // ===============================================================
}