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
import com.google.android.material.textfield.TextInputEditText

class StudentListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentListBinding
    private val viewModel: StudentListViewModel by viewModels()
    private lateinit var studentAdapter: StudentAdapter

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
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.students.observe(this) { students ->
            if (students.isNullOrEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvStudents.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvStudents.visibility = View.VISIBLE
                studentAdapter.submitList(students)
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

        viewModel.deleteStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Siswa berhasil dihapus!", Toast.LENGTH_SHORT).show()
                viewModel.fetchAllStudents() // Refresh list setelah hapus
            }.onFailure {
                Toast.makeText(this, "Gagal menghapus: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Menambahkan observer untuk status update, termasuk update sesi dan edit data
        viewModel.updateStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Data berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                viewModel.fetchAllStudents() // Refresh list setelah update
            }.onFailure {
                Toast.makeText(this, "Gagal memperbarui data: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showStudentDetailDialog(student: Student) {
        // Menggunakan inflate untuk membuat view dari layout XML dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_student_detail, null)

        // Referensi ke semua view di dalam dialog, termasuk yang baru
        val tvName = dialogView.findViewById<TextView>(R.id.tvDetailStudentName)
        val tvNickname = dialogView.findViewById<TextView>(R.id.tvDetailNickname)
        val etSessions = dialogView.findViewById<TextInputEditText>(R.id.etSessions)
        val btnSaveSessions = dialogView.findViewById<Button>(R.id.btnSaveSessions)
        val btnEdit = dialogView.findViewById<Button>(R.id.btnEdit)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)

        // Mengisi data awal ke dalam view
        tvName.text = student.name
        etSessions.setText(student.remainingSessions.toString())

        if (student.nickname.isNullOrBlank()) {
            tvNickname.visibility = View.GONE
        } else {
            tvNickname.visibility = View.VISIBLE
            tvNickname.text = "(${student.nickname})"
        }

        // Membuat dialog menggunakan MaterialAlertDialogBuilder
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        // Memberi aksi pada tombol Simpan Sesi
        btnSaveSessions.setOnClickListener {
            val newSessionString = etSessions.text.toString()
            if (newSessionString.isBlank()) {
                Toast.makeText(this, "Jumlah pertemuan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newSessionCount = newSessionString.toInt()
            viewModel.updateStudentSessions(student.id, newSessionCount)
            dialog.dismiss() // Tutup dialog setelah aksi
        }

        // Memberi aksi pada tombol Edit
        btnEdit.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, EditStudentActivity::class.java).apply {
                putExtra(EditStudentActivity.EXTRA_STUDENT, student)
            }
            startActivity(intent)
        }

        // Memberi aksi pada tombol Hapus
        btnDelete.setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmationDialog(student)
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