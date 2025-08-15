// ui/attendance/AttendanceActivity.kt
package com.coachbro.absenrenang.ui.attendance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.coachbro.absenrenang.databinding.ActivityAttendanceBinding
import com.coachbro.absenrenang.ui.studentlist.StudentAdapter
import com.coachbro.absenrenang.viewmodel.AttendanceViewModel

class AttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceBinding
    private val viewModel: AttendanceViewModel by viewModels()
    private lateinit var studentAdapter: StudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    // Fungsi onResume untuk memastikan list selalu update jika ada perubahan dari halaman detail
    override fun onResume() {
        super.onResume()
        // Panggil ulang viewmodel untuk refresh data
        viewModel.fetchAllStudents()
    }


    private fun setupToolbar() {
        binding.toolbar.title = "Pilih Siswa untuk Absensi"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text -> viewModel.searchStudent(text.toString()) }
    }

    private fun setupRecyclerView() {
        studentAdapter = StudentAdapter { student ->
            // Buka halaman detail absensi
            val intent = Intent(this, AttendanceDetailActivity::class.java).apply {
                putExtra(AttendanceDetailActivity.EXTRA_STUDENT_ID, student.id)
                putExtra(AttendanceDetailActivity.EXTRA_STUDENT_NAME, student.name)
            }
            startActivity(intent)
        }
        binding.rvStudents.adapter = studentAdapter
        binding.rvStudents.layoutManager = LinearLayoutManager(this)
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.errorMessage.observe(this) { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        viewModel.filteredStudents.observe(this) { students ->
            binding.tvEmpty.visibility = if (students.isNullOrEmpty()) View.VISIBLE else View.GONE
            binding.rvStudents.visibility = if (students.isNullOrEmpty()) View.GONE else View.VISIBLE
            studentAdapter.submitList(students)
        }
    }
}