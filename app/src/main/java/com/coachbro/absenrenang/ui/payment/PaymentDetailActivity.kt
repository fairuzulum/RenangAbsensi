// ui/payment/PaymentDetailActivity.kt
package com.coachbro.absenrenang.ui.payment

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
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
            hint = "Contoh: 250.000"
        }

        // Tambahkan TextWatcher untuk format Rupiah
        editText.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.toString() != current) {
                    editText.removeTextChangedListener(this)

                    // Hapus semua karakter non-digit
                    val cleanString = s.toString().replace("[^0-9]".toRegex(), "")

                    // Format ke xxx.xxx.xxx
                    val formatted = formatRupiah(cleanString)

                    current = formatted
                    editText.setText(formatted)
                    editText.setSelection(formatted.length)

                    editText.addTextChangedListener(this)
                }
            }
        })

        MaterialAlertDialogBuilder(this)
            .setTitle("Tambah Pembayaran Baru")
            .setMessage("Masukkan jumlah pembayaran (kelipatan 250.000).")
            .setView(editText)
            .setNegativeButton("Batal", null)
            .setPositiveButton("Bayar") { _, _ ->
                val amountString = editText.text.toString().replace("[^0-9]".toRegex(), "")
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

    // Fungsi untuk format ke xxx.xxx.xxx
    private fun formatRupiah(input: String): String {
        if (input.isEmpty()) return ""
        val number = input.toLongOrNull() ?: return input
        val result = StringBuilder()
        val reversedInput = input.reversed()
        for (i in reversedInput.indices) {
            result.append(reversedInput[i])
            if (i % 3 == 2 && i != reversedInput.length - 1) {
                result.append('.')
            }
        }
        return result.reverse().toString()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.student.observe(this) { student ->
            student?.let {
                binding.tvStudentName.text = it.name
                binding.tvCurrentSessions.text = "${it.remainingSessions} Pertemuan"
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