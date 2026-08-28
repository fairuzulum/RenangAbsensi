package com.coachbro.absenrenang.utils

import android.view.View
import android.view.animation.AnimationUtils
import com.coachbro.absenrenang.R

fun View.startPulse() {
    val pulse = AnimationUtils.loadAnimation(context, R.anim.skeleton_pulse)
    this.startAnimation(pulse)
}

fun View.stopPulse() {
    this.clearAnimation()
}