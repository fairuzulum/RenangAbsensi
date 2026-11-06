// ui/attendance/AttendanceDetailActivity.kt
package com.coachbro.absenrenang.ui.attendance

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.coachbro.absenrenang.R
import com.coachbro.absenrenang.databinding.ActivityAttendanceDetailBinding
import com.coachbro.absenrenang.viewmodel.AttendanceDetailViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AttendanceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceDetailBinding
    private val viewModel: AttendanceDetailViewModel by viewModels()
    private lateinit var historyAdapter: AttendanceHistoryAdapter

    private var studentId: String? = null
    private var studentName: String? = null
    private var hasAttendedToday = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        studentId = intent.getStringExtra(EXTRA_STUDENT_ID)
        studentName = intent.getStringExtra(EXTRA_STUDENT_NAME)

        if (studentId == null) {
            Toast.makeText(this, "ID Siswa tidak valid!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.fetchStudentDetails(studentId!!)
    }

    private fun setupToolbar() {
        binding.toolbar.title = "Detail Absensi" // Judul dibuat generik
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = AttendanceHistoryAdapter()
        binding.rvAttendanceHistory.apply {
            layoutManager = LinearLayoutManager(this@AttendanceDetailActivity)
            adapter = historyAdapter
        }
    }

    private fun setupListeners() {
        binding.btnMarkPresent.setOnClickListener {
            if (hasAttendedToday) {
                Toast.makeText(this, "Siswa ini sudah absen hari ini.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showConfirmAttendanceDialog()
        }
    }

    private fun showConfirmAttendanceDialog() {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_attendance, null)
        dialog.setContentView(dialogView)

        val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<View>(R.id.btnConfirm)

        tvMessage.text = "Yakin ingin mencatat kehadiran untuk ${studentName}? Sisa pertemuan akan berkurang 1."

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            studentId?.let { viewModel.markAsPresent(it) }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingLayout.root.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.student.observe(this) { student ->
            student?.let {
                binding.tvStudentName.text = it.name
                binding.tvCurrentSessions.text = it.remainingSessions.toString()

                // Warna teks sesi
                val color = if (it.remainingSessions <= 0) {
                    ContextCompat.getColor(this, R.color.error)
                } else {
                    ContextCompat.getColor(this, R.color.primary_blue)
                }
                binding.tvCurrentSessions.setTextColor(color)

                updateButtonState(hasAttendedToday)
            }
        }

        viewModel.hasAttendedToday.observe(this) { hasAttended ->
            this.hasAttendedToday = hasAttended
            binding.tvTodayStatus.text = if (hasAttended) "Sudah Absen" else "Belum Absen"
            binding.tvTodayStatus.setTextColor(
                if (hasAttended) ContextCompat.getColor(this, R.color.success)
                else ContextCompat.getColor(this, R.color.gray_700)
            )
            updateButtonState(hasAttended)
        }

        viewModel.attendanceHistory.observe(this) { history ->
            binding.tvEmptyHistory.visibility = if (history.isNullOrEmpty()) View.VISIBLE else View.GONE
            historyAdapter.submitList(history)
        }

        viewModel.attendanceStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Kehadiran berhasil dicatat!", Toast.LENGTH_SHORT).show()
            }.onFailure { exception ->
                Toast.makeText(this, "Gagal: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ===============================================================
    // FUNGSI INI SEKARANG LEBIH SEDERHANA
    // ===============================================================
    private fun updateButtonState(hasAttended: Boolean) {
        if (hasAttended) {
            binding.btnMarkPresent.isEnabled = false
            binding.btnMarkPresent.text = "Siswa Sudah Hadir"
            binding.btnMarkPresent.alpha = 0.5f
        } else {
            binding.btnMarkPresent.isEnabled = true
            binding.btnMarkPresent.text = "Konfirmasi Kehadiran"
            binding.btnMarkPresent.alpha = 1.0f
        }
    }

    companion object {
        const val EXTRA_STUDENT_ID = "extra_student_id"
        const val EXTRA_STUDENT_NAME = "extra_student_name"
    }
}