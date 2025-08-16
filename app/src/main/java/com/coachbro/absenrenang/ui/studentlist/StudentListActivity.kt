// ui/studentlist/StudentListActivity.kt
package com.coachbro.absenrenang.ui.studentlist

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.coachbro.absenrenang.R
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.databinding.ActivityStudentListBinding
import com.coachbro.absenrenang.viewmodel.StudentListViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class StudentListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentListBinding
    private val viewModel: StudentListViewModel by viewModels()
    private lateinit var studentAdapter: StudentAdapter
    private var detailDialog: Dialog? = null // Variabel untuk menyimpan referensi dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchAllStudents()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text -> viewModel.searchStudent(text.toString()) }
    }

    private fun setupRecyclerView() {
        studentAdapter = StudentAdapter { student ->
            showStudentDetailDialog(student)
        }
        binding.rvStudents.apply {
            layoutManager = LinearLayoutManager(this@StudentListActivity)
            adapter = studentAdapter
        }
    }

    private fun observeViewModel() {
        // Mengamati status loading untuk menampilkan/menyembunyikan ProgressBar
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Mengamati daftar siswa
        viewModel.students.observe(this) { students ->
            if (students.isNullOrEmpty()) {
                // Jika daftar kosong, tampilkan pesan dan sembunyikan RecyclerView
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvStudents.visibility = View.GONE
            } else {
                // Jika ada data, sembunyikan pesan dan tampilkan RecyclerView
                binding.tvEmpty.visibility = View.GONE
                binding.rvStudents.visibility = View.VISIBLE
                studentAdapter.submitList(students)
            }
        }

        // Mengamati pesan error
        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

        // Observer baru untuk status penghapusan
        viewModel.deleteStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Siswa berhasil dihapus!", Toast.LENGTH_SHORT).show()
                viewModel.fetchAllStudents() // Refresh list setelah hapus
            }.onFailure {
                Toast.makeText(this, "Gagal menghapus: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showStudentDetailDialog(student: Student) {
        // Menggunakan inflate untuk membuat view dari layout XML dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_student_detail, null)

        // Mencari semua view di dalam dialog
        val tvName = dialogView.findViewById<TextView>(R.id.tvDetailStudentName)
        val tvSessions = dialogView.findViewById<TextView>(R.id.tvDetailRemainingSessions)
        val tvAge = dialogView.findViewById<TextView>(R.id.tvDetailAge)
        val tvParentName = dialogView.findViewById<TextView>(R.id.tvDetailParentName)
        val tvParentPhone = dialogView.findViewById<TextView>(R.id.tvDetailParentPhone)
        val btnEdit = dialogView.findViewById<Button>(R.id.btnEdit)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)

        // Mengisi data ke dalam view
        tvName.text = student.name
        tvSessions.text = "Sisa Sesi: ${student.remainingSessions}"
        tvAge.text = student.age?.toString()?.plus(" Tahun") ?: "-"
        tvParentName.text = student.parentName ?: "-"
        tvParentPhone.text = student.parentPhone ?: "-"

        // Membuat dialog menggunakan MaterialAlertDialogBuilder
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        // Memberi aksi pada tombol Edit
        btnEdit.setOnClickListener {
            dialog.dismiss() // Tutup dialog saat ini
            // Buka EditStudentActivity dan kirim data siswa
            val intent = Intent(this, EditStudentActivity::class.java).apply {
                putExtra(EditStudentActivity.EXTRA_STUDENT, student)
            }
            startActivity(intent)
        }

        // Memberi aksi pada tombol Hapus
        btnDelete.setOnClickListener {
            dialog.dismiss() // Tutup dialog detail dulu
            showDeleteConfirmationDialog(student) // Tampilkan dialog konfirmasi hapus
        }

        dialog.show() // Menampilkan dialog ke layar
    }

    private fun showDeleteConfirmationDialog(student: Student) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Siswa")
            .setMessage("Apakah Anda yakin ingin menghapus data '${student.name}'? Aksi ini tidak dapat dibatalkan.")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Ya, Hapus") { _, _ ->
                viewModel.deleteStudent(student.id)
            }
            .show()
    }
}