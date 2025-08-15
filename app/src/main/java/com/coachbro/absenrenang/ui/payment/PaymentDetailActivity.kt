// ui/payment/PaymentDetailActivity.kt
package com.coachbro.absenrenang.ui.payment

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.coachbro.absenrenang.databinding.ActivityPaymentDetailBinding
import com.coachbro.absenrenang.viewmodel.PaymentDetailViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.Locale

class PaymentDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentDetailBinding
    private val viewModel: PaymentDetailViewModel by viewModels()
    private lateinit var paymentHistoryAdapter: PaymentHistoryAdapter

    // Variabel untuk menyimpan ID & Nama siswa dari Intent
    private var studentId: String? = null
    private var studentName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data dari Intent
        studentId = intent.getStringExtra(EXTRA_STUDENT_ID)
        studentName = intent.getStringExtra(EXTRA_STUDENT_NAME)

        if (studentId == null) {
            Toast.makeText(this, "ID Siswa tidak valid!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        // Pertama kali dibuka, langsung ambil data
        viewModel.fetchStudentDetails(studentId!!)
    }

    private fun setupToolbar() {
        binding.toolbar.title = studentName ?: "Detail Pembayaran"
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        paymentHistoryAdapter = PaymentHistoryAdapter()
        binding.rvPaymentHistory.apply {
            layoutManager = LinearLayoutManager(this@PaymentDetailActivity)
            adapter = paymentHistoryAdapter
        }
    }

    private fun setupListeners() {
        binding.btnAddPayment.setOnClickListener {
            showPaymentDialog()
        }
    }

    private fun showPaymentDialog() {
        val editText = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Contoh: 250000"
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Tambah Pembayaran Baru")
            .setMessage("Masukkan jumlah pembayaran (kelipatan 250.000).")
            .setView(editText)
            .setNegativeButton("Batal", null)
            .setPositiveButton("Bayar") { _, _ ->
                val amountString = editText.text.toString()
                if (amountString.isBlank()) {
                    Toast.makeText(this, "Jumlah tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val amount = amountString.toLongOrNull()
                if (amount == null || amount % 250000 != 0L) {
                    Toast.makeText(this, "Jumlah harus kelipatan 250.000", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Panggil ViewModel untuk memproses pembayaran
                studentId?.let { viewModel.addPayment(it, amount) }
            }
            .show()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.student.observe(this) { student ->
            student?.let {
                binding.tvStudentName.text = it.name
                binding.tvCurrentSessions.text = "${it.remainingSessions} Sesi"
            }
        }

        viewModel.paymentHistory.observe(this) { history ->
            binding.tvEmptyHistory.visibility = if (history.isNullOrEmpty()) View.VISIBLE else View.GONE
            paymentHistoryAdapter.submitList(history)
        }

        viewModel.paymentStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Pembayaran berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                // PENTING: Ambil ulang data untuk me-refresh layar
                studentId?.let { viewModel.fetchStudentDetails(it) }
            }.onFailure {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Companion object untuk menyimpan key Intent agar konsisten
    companion object {
        const val EXTRA_STUDENT_ID = "extra_student_id"
        const val EXTRA_STUDENT_NAME = "extra_student_name"
    }
}

// **CATATAN**: Anda perlu membuat layout activity_payment_detail.xml yang berisi
// Toolbar, TextView (tvStudentName, tvCurrentSessions), Button (btnAddPayment),
// RecyclerView (rvPaymentHistory), ProgressBar, dan TextView (tvEmptyHistory).