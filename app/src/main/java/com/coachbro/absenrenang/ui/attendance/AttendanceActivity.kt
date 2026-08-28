package com.coachbro.absenrenang.ui.attendance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.coachbro.absenrenang.R
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.data.model.Classification
import com.coachbro.absenrenang.databinding.ActivityAttendanceBinding
import com.coachbro.absenrenang.ui.studentlist.StudentAdapter
import com.coachbro.absenrenang.ui.studentlist.FolderAdapter
import com.coachbro.absenrenang.viewmodel.AttendanceViewModel
import com.coachbro.absenrenang.utils.startPulse
import com.coachbro.absenrenang.utils.stopPulse

class AttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceBinding
    private val viewModel: AttendanceViewModel by viewModels()
    private lateinit var studentAdapter: StudentAdapter
    private lateinit var folderAdapter: FolderAdapter

    private var isShowingStudents = false
    private var currentFolderName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isShowingStudents) {
                    showFolders()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        observeViewModel()
        
        showFolders()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchAllStudents()
        viewModel.loadClassifications()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { 
            if (isShowingStudents) {
                showFolders()
            } else {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text -> 
            if (isShowingStudents) {
                viewModel.searchStudent(text.toString())
            }
        }
    }

    private fun setupRecyclerView() {
        // Students
        studentAdapter = StudentAdapter { student, _ ->
            val intent = Intent(this, AttendanceDetailActivity::class.java).apply {
                putExtra(AttendanceDetailActivity.EXTRA_STUDENT_ID, student.id)
                putExtra(AttendanceDetailActivity.EXTRA_STUDENT_NAME, student.name)
            }
            startActivity(intent)
        }
        binding.rvStudents.apply {
            layoutManager = LinearLayoutManager(this@AttendanceActivity)
            adapter = studentAdapter
        }

        // Folders
        folderAdapter = FolderAdapter { classification ->
            showStudentsInFolder(classification)
        }
        binding.rvFolders.apply {
            layoutManager = GridLayoutManager(this@AttendanceActivity, 2)
            adapter = folderAdapter
        }
    }

    private fun showFolders() {
        isShowingStudents = false
        binding.viewFlipper.displayedChild = 0
        binding.cardSearch.visibility = View.GONE
        binding.toolbar.title = "Pilih Siswa (Absensi)"
        viewModel.loadClassifications()
    }

    private fun showStudentsInFolder(classification: Classification) {
        isShowingStudents = true
        currentFolderName = classification.name
        binding.viewFlipper.displayedChild = 1
        binding.cardSearch.visibility = View.VISIBLE
        binding.toolbar.title = currentFolderName
        filterStudentsByFolder(currentFolderName)
    }

    private fun filterStudentsByFolder(folderName: String) {
        val allStudents = viewModel.students.value ?: emptyList()
        val filtered = when (folderName) {
            "Umum" -> allStudents
            "⚠️ Sesi Habis" -> allStudents.filter { it.remainingSessions <= 0 }
            else -> allStudents.filter { it.classification == folderName }
        }
        studentAdapter.submitList(filtered)
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                if (isShowingStudents) {
                    binding.layoutSkeletonStudents.visibility = View.VISIBLE
                    binding.layoutSkeletonFolders.visibility = View.GONE
                    binding.layoutSkeletonStudents.startPulse()
                } else {
                    binding.layoutSkeletonFolders.visibility = View.VISIBLE
                    binding.layoutSkeletonStudents.visibility = View.GONE
                    binding.layoutSkeletonFolders.startPulse()
                }
                binding.viewFlipper.visibility = View.GONE
            } else {
                binding.layoutSkeletonFolders.visibility = View.GONE
                binding.layoutSkeletonStudents.visibility = View.GONE
                binding.layoutSkeletonFolders.stopPulse()
                binding.layoutSkeletonStudents.stopPulse()
                binding.viewFlipper.visibility = View.VISIBLE
            }
        }

        viewModel.students.observe(this) { 
            updateFolders()
            if (isShowingStudents) {
                filterStudentsByFolder(currentFolderName)
            }
        }

        viewModel.classifications.observe(this) { 
            updateFolders()
        }

        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateFolders() {
        val classifications = viewModel.classifications.value ?: emptyList()
        val allStudents = viewModel.students.value ?: emptyList()
        val folderList = classifications.toMutableList()
        folderList.add(0, Classification(id = "umum", name = "Umum", color = "#0084FF"))
        val hasExpiredSessions = allStudents.any { it.remainingSessions <= 0 }
        if (hasExpiredSessions) {
            folderList.add(1, Classification(id = "expired", name = "⚠️ Sesi Habis", color = "#FF4B55"))
        }
        folderAdapter.submitList(folderList)
    }
}