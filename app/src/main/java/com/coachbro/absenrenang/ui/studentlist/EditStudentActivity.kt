// ui/studentlist/EditStudentActivity.kt
package com.coachbro.absenrenang.ui.studentlist

import android.os.Build
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
        // Menggunakan binding untuk layout activity_edit_student
        binding = ActivityEditStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mengambil objek Student dari Intent
        currentStudent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_STUDENT, Student::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_STUDENT)
        }


        // Pengaman jika tidak ada data yang dikirim
        if (currentStudent == null) {
            Toast.makeText(this, "Gagal memuat data siswa", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        populateFormWithData()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    // Mengisi form dengan data siswa yang sudah ada
    private fun populateFormWithData() {
        currentStudent?.let {
            binding.etName.setText(it.name)
            binding.etNickname.setText(it.nickname ?: "") // Tampilkan nickname jika ada
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val fullName = binding.etName.text.toString().trim()
            val nickname = binding.etNickname.text.toString().trim()

            if (fullName.isBlank()) {
                binding.tilName.error = "Nama lengkap tidak boleh kosong"
                return@setOnClickListener
            } else {
                binding.tilName.error = null
            }

            // Buat objek student baru dengan data yang sudah diupdate
            val updatedStudent = currentStudent!!.copy(
                name = fullName,
                nickname = nickname.ifBlank { null }
            )
            viewModel.updateStudent(updatedStudent)
        }
    }

    private fun observeViewModel() {
        // Mengamati status dari proses update
        viewModel.updateStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Data siswa berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke halaman daftar siswa setelah sukses
            }.onFailure {
                Toast.makeText(this, "Gagal memperbarui data: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val EXTRA_STUDENT = "extra_student"
    }
}