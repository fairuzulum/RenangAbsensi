// ui/attendance/AttendanceDetailActivity.kt
package com.coachbro.absenrenang.ui.attendance

import android.graphics.Color
import android.os.Bundle
import android.view.View
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

            MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                .setTitle("Konfirmasi Absensi")
                .setMessage("Yakin ingin mencatat kehadiran untuk ${studentName}? Sisa pertemuan akan berkurang 1.")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Ya, Hadir") { _, _ ->
                    studentId?.let { viewModel.markAsPresent(it) }
                }
                .show()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.student.observe(this) { student ->
            student?.let {
                binding.tvStudentName.text = it.name
                binding.tvCurrentSessions.text = it.remainingSessions.toString()

                // Logika baru untuk mengubah warna sesi jika minus
                if (it.remainingSessions <= 0) {
                    binding.tvCurrentSessions.setBackgroundColor(Color.parseColor("#D32F2F")) // Merah
                } else {
                    binding.tvCurrentSessions.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue))
                }

                updateButtonState(hasAttendedToday)
            }
        }

        viewModel.hasAttendedToday.observe(this) { hasAttended ->
            this.hasAttendedToday = hasAttended
            updateButtonState(hasAttended)
        }

        viewModel.attendanceHistory.observe(this) { history ->
            binding.tvEmptyHistory.visibility = if (history.isNullOrEmpty()) View.VISIBLE else View.GONE
            historyAdapter.submitList(history)
        }

        viewModel.attendanceStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Kehadiran berhasil dicatat!", Toast.LENGTH_SHORT).show()
                studentId?.let { viewModel.fetchStudentDetails(it) }
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
            binding.btnMarkPresent.text = "SUDAH ABSEN HARI INI"
        } else {
            // Tombol akan selalu aktif jika belum absen hari ini
            binding.btnMarkPresent.isEnabled = true
            binding.btnMarkPresent.text = "Catat Kehadiran Hari Ini"
        }
    }

    companion object {
        const val EXTRA_STUDENT_ID = "extra_student_id"
        const val EXTRA_STUDENT_NAME = "extra_student_name"
    }
}