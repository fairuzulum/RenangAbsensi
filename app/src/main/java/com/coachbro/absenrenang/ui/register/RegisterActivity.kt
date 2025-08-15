// ui/register/RegisterActivity.kt
package com.coachbro.absenrenang.ui.register

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.coachbro.absenrenang.databinding.ActivityRegisterBinding
import com.coachbro.absenrenang.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        // Aksi untuk tombol kembali di toolbar
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val age = binding.etAge.text.toString().trim()
            val parentName = binding.etParentName.text.toString().trim()
            val parentPhone = binding.etParentPhone.text.toString().trim()

            viewModel.registerStudent(name, age, parentName, parentPhone)
        }
    }

    private fun observeViewModel() {
        viewModel.registrationStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Siswa berhasil didaftarkan!", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke halaman sebelumnya (Home)
            }.onFailure { exception ->
                // Tampilkan pesan error
                binding.tilName.error = if(exception.message?.contains("kosong") == true) exception.message else null
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}