package com.coachbro.absenrenang.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
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
import com.coachbro.absenrenang.ui.report.AttendanceReportActivity
import com.coachbro.absenrenang.ui.settings.SettingsActivity
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

        setupEntranceAnimations()
        observeViewModel()
        setupClickListeners()
        
        com.coachbro.absenrenang.utils.UpdateManager.checkUpdate(this)
    }

    private fun setupEntranceAnimations() {
        val viewsToAnimate = listOf(
            binding.cardImageContainer,
            binding.tvMenuHeader,
            binding.cardAbsensi,
            binding.cardPembayaran,
            binding.cardRegister,
            binding.cardListSiswa,
            binding.cardFinancial,
            binding.cardAttendanceReport,
            binding.cardSettings
        )

        viewsToAnimate.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 50f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(100L * index)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
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
        // UBAH CLICK LISTENER UNTUK LAPORAN KEHADIRAN DI SINI
        // ===============================================================
        binding.cardAttendanceReport.setOnClickListener {
            handleMenuClick(
                setting = menuPasswords?.laporan, // <-- Gunakan field 'laporan'
                intent = Intent(this, AttendanceReportActivity::class.java),
                menuName = "Laporan Kehadiran"
            )
        }

        binding.cardSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

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
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        dialog.setContentView(dialogBinding.root)

        // Add a "Unlock" button to the BottomSheet or use a listener
        // Since the original used PositiveButton, I'll add a button to the layout or just use a simple button here.
        // Actually, let's just add a Button to dialogBinding if it doesn't have one, 
        // OR better, I'll just use the PositiveButton style inside the BottomSheet.
        
        // Let's modify the dialogBinding to include a button for better UX in BottomSheet.
        val btnUnlock = com.google.android.material.button.MaterialButton(this).apply {
            text = "Buka Menu"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (24 * resources.displayMetrics.density).toInt()
            }
        }
        (dialogBinding.root as LinearLayout).addView(btnUnlock)

        btnUnlock.setOnClickListener {
            val enteredPin = dialogBinding.etPin.text.toString()
            if (enteredPin == correctPin) {
                dialog.dismiss()
                onSuccess()
            } else {
                dialogBinding.tilPin.error = "PIN Salah!"
            }
        }

        dialog.show()
    }
}