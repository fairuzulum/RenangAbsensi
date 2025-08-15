// ui/payment/PaymentActivity.kt
package com.coachbro.absenrenang.ui.payment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.coachbro.absenrenang.databinding.ActivityPaymentBinding
import com.coachbro.absenrenang.ui.studentlist.StudentAdapter
import com.coachbro.absenrenang.viewmodel.PaymentViewModel

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private val viewModel: PaymentViewModel by viewModels()
    private lateinit var studentAdapter: StudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        observeViewModel()
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
        // Inisialisasi adapter dengan aksi klik
        studentAdapter = StudentAdapter { student ->
            // Saat item siswa diklik, buka PaymentDetailActivity
            val intent = Intent(this, PaymentDetailActivity::class.java).apply {
                // Kirim ID dan Nama siswa ke activity berikutnya
                putExtra(PaymentDetailActivity.EXTRA_STUDENT_ID, student.id)
                putExtra(PaymentDetailActivity.EXTRA_STUDENT_NAME, student.name)
            }
            startActivity(intent)
        }

        binding.rvStudents.apply {
            layoutManager = LinearLayoutManager(this@PaymentActivity)
            adapter = studentAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

        viewModel.filteredStudents.observe(this) { students ->
            // Tampilkan/sembunyikan pesan "kosong" berdasarkan data
            binding.tvEmpty.visibility = if (students.isNullOrEmpty()) View.VISIBLE else View.GONE
            binding.rvStudents.visibility = if (students.isNullOrEmpty()) View.GONE else View.VISIBLE

            // Kirim data ke adapter
            studentAdapter.submitList(students)
        }
    }
}