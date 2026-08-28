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
        
        viewModel.loadClassifications()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val fullName = binding.etName.text.toString().trim()
            val nickname = binding.etNickname.text.toString().trim()
            val classification = binding.actClassification.text.toString().trim()
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()

            viewModel.registerStudent(fullName, nickname, classification, phoneNumber)
        }
    }

    private fun observeViewModel() {
        viewModel.registrationStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Siswa berhasil didaftarkan!", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { exception ->
                binding.tilName.error = if(exception.message?.contains("kosong") == true) exception.message else null
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.classifications.observe(this) { list ->
            val names = list.map { it.name }.toMutableList()
            if (!names.contains("Umum")) names.add(0, "Umum")
            
            val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
            binding.actClassification.setAdapter(adapter)
            
            if (binding.actClassification.text.isEmpty()) {
                binding.actClassification.setText("Umum", false)
            }
        }
    }
}