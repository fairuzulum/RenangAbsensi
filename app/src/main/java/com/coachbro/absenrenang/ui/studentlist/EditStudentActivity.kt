// ui/studentlist/EditStudentActivity.kt
package com.coachbro.absenrenang.ui.studentlist

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.databinding.ActivityEditStudentBinding
import com.coachbro.absenrenang.viewmodel.StudentListViewModel

class EditStudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditStudentBinding
    private val viewModel: StudentListViewModel by viewModels()
    private var currentStudent: Student? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data siswa dari Intent
        currentStudent = intent.getParcelableExtra(EXTRA_STUDENT)

        if (currentStudent == null) {
            Toast.makeText(this, "Data siswa tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        populateForm()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun populateForm() {
        currentStudent?.let {
            binding.etName.setText(it.name)
            binding.etAge.setText(it.age?.toString() ?: "")
            binding.etParentName.setText(it.parentName ?: "")
            binding.etParentPhone.setText(it.parentPhone ?: "")
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val updatedStudent = currentStudent!!.copy(
                name = binding.etName.text.toString().trim(),
                age = binding.etAge.text.toString().trim().toIntOrNull(),
                parentName = binding.etParentName.text.toString().trim(),
                parentPhone = binding.etParentPhone.text.toString().trim()
            )
            viewModel.updateStudent(updatedStudent)
        }
    }

    private fun observeViewModel() {
        viewModel.updateStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Data berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke halaman list siswa
            }.onFailure {
                Toast.makeText(this, "Gagal memperbarui data: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val EXTRA_STUDENT = "extra_student"
    }
}