package com.coachbro.absenrenang.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.inputmethod.InputMethodManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.coachbro.absenrenang.R
import com.coachbro.absenrenang.databinding.ActivityPinEntryBinding
import com.coachbro.absenrenang.ui.home.MainActivity
import com.coachbro.absenrenang.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth

class PinEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinEntryBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        
        setupPinEntry()
        setupLogout()
        
        // Show keyboard automatically
        binding.etHiddenPin.postDelayed({
            showKeyboard()
        }, 300)
    }

    private fun setupPinEntry() {
        binding.etHiddenPin.doAfterTextChanged { s ->
            val pin = s.toString()
            updateDots(pin.length)
            
            if (pin.length == 4) {
                validatePin(pin)
            }
        }

        binding.layoutDots.setOnClickListener {
            showKeyboard()
        }
    }

    private fun showKeyboard() {
        binding.etHiddenPin.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etHiddenPin, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun updateDots(length: Int) {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
        dots.forEachIndexed { index, view ->
            if (index < length) {
                view.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.primary_blue))
                view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).start()
            } else {
                view.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.divider))
                view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
            }
        }
    }

    private fun validatePin(enteredPin: String) {
        val savedPin = sessionManager.getPin()
        if (enteredPin == savedPin) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            handleError()
        }
    }

    private fun handleError() {
        binding.etHiddenPin.setText("")
        updateDots(0)
        
        // Shake animation
        val shake = AnimationUtils.loadAnimation(this, R.anim.skeleton_pulse)
        binding.layoutDots.startAnimation(shake)
        
        // Vibrate
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(300)
        }
        
        Toast.makeText(this, "PIN Salah", Toast.LENGTH_SHORT).show()
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            sessionManager.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}