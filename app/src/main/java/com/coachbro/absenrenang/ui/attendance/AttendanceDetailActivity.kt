// ui/attendance/AttendanceDetailActivity.kt
package com.coachbro.absenrenang.ui.attendance

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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

    // ===============================================================
    // Variabel lokal untuk menyimpan status absensi
    // ===============================================================
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
        binding.toolbar.title = studentName ?: "Detail Absensi"
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
            // ===============================================================
            // Validasi di sisi UI sebelum menampilkan dialog
            // ===============================================================
            if (hasAttendedToday) {
                Toast.makeText(this, "Siswa ini sudah absen hari ini.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                .setTitle("Konfirmasi Absensi")
                .setMessage("Apakah Anda yakin ingin mencatat kehadiran untuk ${studentName}? Sisa sesi akan berkurang 1.")
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

        // --- Perubahan di observer student ---
        viewModel.student.observe(this) { student ->
            student?.let {
                binding.tvStudentName.text = it.name
                // Mengubah cara set teks, hanya angkanya saja
                binding.tvCurrentSessions.text = it.remainingSessions.toString()
                updateButtonState(it.remainingSessions, hasAttendedToday)
            }
        }

        viewModel.hasAttendedToday.observe(this) { hasAttended ->
            this.hasAttendedToday = hasAttended
            viewModel.student.value?.let { student ->
                updateButtonState(student.remainingSessions, hasAttended)
            }
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
    // Fungsi helper BARU untuk mengelola semua kondisi tombol
    // ===============================================================
    private fun updateButtonState(remainingSessions: Int, hasAttended: Boolean) {
        if (hasAttended) {
            binding.btnMarkPresent.isEnabled = false
            binding.btnMarkPresent.text = "SUDAH ABSEN HARI INI"
        } else if (remainingSessions <= 0) {
            binding.btnMarkPresent.isEnabled = false
            binding.btnMarkPresent.text = "Sesi Habis"
            binding.btnMarkPresent.icon = null // Sembunyikan ikon jika sesi habis
        } else {
            binding.btnMarkPresent.isEnabled = true
            binding.btnMarkPresent.text = "Catat Kehadiran Hari Ini"
            binding.btnMarkPresent.setIconResource(R.drawable.ic_checklist)
        }
    }

    companion object {
        const val EXTRA_STUDENT_ID = "extra_student_id"
        const val EXTRA_STUDENT_NAME = "extra_student_name"
    }
}