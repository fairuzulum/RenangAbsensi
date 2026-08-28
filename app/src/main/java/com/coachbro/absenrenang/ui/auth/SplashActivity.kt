package com.coachbro.absenrenang.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.appcompat.app.AppCompatActivity
import com.coachbro.absenrenang.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startAnimations()

        Handler(Looper.getMainLooper()).postDelayed({
            checkNavigation()
        }, 3500)
    }

    private fun checkNavigation() {
        val sessionManager = com.coachbro.absenrenang.utils.SessionManager(this)
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        
        val intent = when {
            auth.currentUser == null -> {
                Intent(this, LoginActivity::class.java)
            }
            !sessionManager.hasPin() -> {
                Intent(this, SetPinActivity::class.java)
            }
            else -> {
                Intent(this, PinEntryActivity::class.java)
            }
        }
        
        startActivity(intent)
        finish()
    }

    private fun startAnimations() {
        // Phase 1: Logo fade in
        binding.ivLogo.alpha = 0f
        binding.ivLogo.animate()
            .alpha(1f)
            .setDuration(800)
            .withEndAction {
                // Wait a bit, then Phase 2
                Handler(Looper.getMainLooper()).postDelayed({
                    // Fade out logo and fade in dots
                    binding.ivLogo.animate().alpha(0f).setDuration(500).start()
                    binding.layoutDots.animate().alpha(1f).setDuration(500).withStartAction {
                        // Start dots jumping animation
                        animateDot(binding.dot1, 0)
                        animateDot(binding.dot2, 200)
                        animateDot(binding.dot3, 400)
                    }.start()
                }, 1000)
            }
            .start()
    }

    private fun animateDot(dot: View, delay: Long) {
        val animation = TranslateAnimation(0f, 0f, 0f, -15f)
        animation.duration = 400
        animation.repeatMode = Animation.REVERSE
        animation.repeatCount = Animation.INFINITE
        animation.startOffset = delay
        dot.startAnimation(animation)
    }
}