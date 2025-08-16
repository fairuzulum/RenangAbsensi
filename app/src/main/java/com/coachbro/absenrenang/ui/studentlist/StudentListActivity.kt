// ui/studentlist/StudentListActivity.kt
package com.coachbro.absenrenang.ui.studentlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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
        // Panggil fetchAllStudents di onResume agar data selalu yang terbaru
        // setiap kali pengguna kembali ke layar ini.
        viewModel.fetchAllStudents()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            viewModel.searchStudent(text.toString())
        }
    }

    private fun setupRecyclerView() {
        studentAdapter = StudentAdapter { student ->
            // Saat item di-klik, panggil fungsi untuk menampilkan dialog detail.
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
    }

    /**
     * Fungsi untuk membuat dan menampilkan dialog detail siswa.
     * @param student Objek data siswa yang akan ditampilkan.
     */
    private fun showStudentDetailDialog(student: Student) {
        // 1. Inflate layout custom dialog_student_detail.xml
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_student_detail, null)

        // 2. Referensi ke semua TextView di dalam dialog
        val tvName = dialogView.findViewById<TextView>(R.id.tvDetailStudentName)
        val tvSessions = dialogView.findViewById<TextView>(R.id.tvDetailRemainingSessions)
        val tvAge = dialogView.findViewById<TextView>(R.id.tvDetailAge)
        val tvParentName = dialogView.findViewById<TextView>(R.id.tvDetailParentName)
        val tvParentPhone = dialogView.findViewById<TextView>(R.id.tvDetailParentPhone)

        // 3. Mengisi data ke dalam TextView
        tvName.text = student.name
        tvSessions.text = "Sisa Sesi: ${student.remainingSessions}"

        // Logika untuk menampilkan data opsional: jika ada, tampilkan. Jika tidak, tampilkan "-".
        tvAge.text = student.age?.toString()?.plus(" Tahun") ?: "-"
        tvParentName.text = student.parentName ?: "-"
        tvParentPhone.text = student.parentPhone ?: "-"

        // 4. Membangun dan menampilkan MaterialAlertDialog
        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("Tutup") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}