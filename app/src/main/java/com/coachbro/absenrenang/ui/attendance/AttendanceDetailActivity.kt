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

    // Menggunakan ViewBinding untuk mengakses view dengan aman dan efisien
    private lateinit var binding: ActivityAttendanceDetailBinding

    // Menggunakan delegasi 'by viewModels()' untuk mendapatkan instance ViewModel
    private val viewModel: AttendanceDetailViewModel by viewModels()

    private lateinit var historyAdapter: AttendanceHistoryAdapter

    // Variabel untuk menyimpan data yang diterima dari Intent
    private var studentId: String? = null
    private var studentName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mengambil data yang dikirim dari activity sebelumnya
        studentId = intent.getStringExtra(EXTRA_STUDENT_ID)
        studentName = intent.getStringExtra(EXTRA_STUDENT_NAME)

        // Pengaman: Jika tidak ada ID siswa, tutup activity ini karena tidak bisa berfungsi
        if (studentId == null) {
            Toast.makeText(this, "ID Siswa tidak valid!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Memanggil fungsi-fungsi setup
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        // Memulai pengambilan data dari Firestore saat activity pertama kali dibuat
        viewModel.fetchStudentDetails(studentId!!)
    }

    private fun setupToolbar() {
        binding.toolbar.title = studentName ?: "Detail Absensi"
        binding.toolbar.setNavigationOnClickListener {
            // Aksi untuk tombol kembali
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = AttendanceHistoryAdapter()
        binding.rvAttendanceHistory.apply {
            // Menggunakan LinearLayoutManager untuk daftar vertikal standar
            layoutManager = LinearLayoutManager(this@AttendanceDetailActivity)
            adapter = historyAdapter
        }
    }

    private fun setupListeners() {
        binding.btnMarkPresent.setOnClickListener {
            // Menampilkan dialog konfirmasi sebelum melakukan absensi
            MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                .setTitle("Konfirmasi Absensi")
                .setMessage("Apakah Anda yakin ingin mencatat kehadiran untuk ${studentName}? Sisa sesi akan berkurang 1.")
                .setNegativeButton("Batal", null) // Tombol "Batal" tidak melakukan apa-apa
                .setPositiveButton("Ya, Hadir") { _, _ ->
                    // Jika dikonfirmasi, panggil fungsi di ViewModel
                    studentId?.let { viewModel.markAsPresent(it) }
                }
                .show()
        }
    }

    // Fungsi ini adalah inti dari reaktivitas UI
    private fun observeViewModel() {
        // Mengamati perubahan status loading
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Mengamati perubahan data siswa (nama, sisa sesi)
        viewModel.student.observe(this) { student ->
            student?.let {
                binding.tvStudentName.text = it.name
                binding.tvCurrentSessions.text = "${it.remainingSessions} Sesi"

                // Logika UI: Non-aktifkan tombol jika sesi habis
                binding.btnMarkPresent.isEnabled = it.remainingSessions > 0
            }
        }

        // Mengamati perubahan data riwayat kehadiran
        viewModel.attendanceHistory.observe(this) { history ->
            binding.tvEmptyHistory.visibility = if (history.isNullOrEmpty()) View.VISIBLE else View.GONE
            // Mengirim daftar baru ke adapter untuk ditampilkan
            historyAdapter.submitList(history)
        }

        // Mengamati status hasil proses absensi
        viewModel.attendanceStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Kehadiran berhasil dicatat!", Toast.LENGTH_SHORT).show()
                // PENTING: Ambil ulang data untuk me-refresh layar setelah sukses
                studentId?.let { viewModel.fetchStudentDetails(it) }
            }.onFailure { exception ->
                // Menampilkan pesan error dari repository (misal: "Sesi habis")
                Toast.makeText(this, "Gagal: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Best practice: Gunakan companion object untuk menyimpan key Intent
    companion object {
        const val EXTRA_STUDENT_ID = "extra_student_id"
        const val EXTRA_STUDENT_NAME = "extra_student_name"
    }
}