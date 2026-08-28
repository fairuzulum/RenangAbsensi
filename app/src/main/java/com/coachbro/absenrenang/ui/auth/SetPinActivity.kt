package com.coachbro.absenrenang.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.coachbro.absenrenang.R
import com.coachbro.absenrenang.databinding.ActivityPinEntryBinding
import com.coachbro.absenrenang.ui.home.MainActivity
import com.coachbro.absenrenang.utils.SessionManager

class SetPinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinEntryBinding
    private lateinit var sessionManager: SessionManager
    
    private var isConfirming = false
    private var firstPin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        
        binding.tvPinTitle.text = "Set PIN Baru"
        binding.tvPinSubtitle.text = "Buat 4 digit PIN untuk keamanan aplikasi"
        binding.btnLogout.visibility = View.GONE
        
        setupPinEntry()
        
        binding.etHiddenPin.postDelayed({
            showKeyboard()
        }, 300)
    }

    private fun setupPinEntry() {
        binding.etHiddenPin.doAfterTextChanged { s ->
            val pin = s.toString()
            updateDots(pin.length)
            
            if (pin.length == 4) {
                handlePinEntry(pin)
            }
        }

        binding.layoutDots.setOnClickListener {
            showKeyboard()
        }
    }

    private fun handlePinEntry(enteredPin: String) {
        if (!isConfirming) {
            firstPin = enteredPin
            isConfirming = true
            
            binding.etHiddenPin.setText("")
            binding.tvPinTitle.text = "Konfirmasi PIN"
            binding.tvPinSubtitle.text = "Masukkan ulang PIN yang baru saja dibuat"
            updateDots(0)
        } else {
            if (enteredPin == firstPin) {
                sessionManager.savePin(enteredPin)
                sessionManager.setLoggedIn(true)
                Toast.makeText(this, "PIN Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "PIN tidak cocok, ulangi lagi", Toast.LENGTH_SHORT).show()
                reset()
            }
        }
    }

    private fun showKeyboard() {
        binding.etHiddenPin.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etHiddenPin, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun reset() {
        isConfirming = false
        firstPin = ""
        binding.etHiddenPin.setText("")
        binding.tvPinTitle.text = "Set PIN Baru"
        binding.tvPinSubtitle.text = "Buat 4 digit PIN untuk keamanan aplikasi"
        updateDots(0)
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
}