package com.coachbro.absenrenang.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.coachbro.absenrenang.R
import com.coachbro.absenrenang.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupListeners() {
        binding.btnNotificationSettings.setOnClickListener {
            startActivity(Intent(this, NotificationManagementActivity::class.java))
        }
        binding.btnClassificationSettings.setOnClickListener {
            startActivity(Intent(this, ClassificationManagementActivity::class.java))
        }
        binding.btnChangePin.setOnClickListener {
            startActivity(Intent(this, com.coachbro.absenrenang.ui.auth.SetPinActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val view = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_logout_bottom_sheet, null)
        dialog.setContentView(view)

        view.findViewById<android.view.View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        view.findViewById<android.view.View>(R.id.btnLogoutConfirm).setOnClickListener {
            dialog.dismiss()
            performLogout()
        }

        dialog.show()
    }

    private fun performLogout() {
        val sessionManager = com.coachbro.absenrenang.utils.SessionManager(this)
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        sessionManager.clearSession()
        
        val intent = Intent(this, com.coachbro.absenrenang.ui.auth.LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}