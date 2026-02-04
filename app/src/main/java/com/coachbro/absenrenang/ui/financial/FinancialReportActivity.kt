package com.coachbro.absenrenang.ui.financial

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.coachbro.absenrenang.databinding.ActivityFinancialReportBinding
import com.coachbro.absenrenang.viewmodel.FinancialViewModel
import java.text.NumberFormat
import java.util.Locale

class FinancialReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFinancialReportBinding
    private val viewModel: FinancialViewModel by viewModels()
    private lateinit var reportAdapter: FinancialReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinancialReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        observeViewModel()

        viewModel.fetchFinancialReport()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        reportAdapter = FinancialReportAdapter()
        binding.rvFinancialReport.apply {
            layoutManager = LinearLayoutManager(this@FinancialReportActivity)
            adapter = reportAdapter
        }
    }

    private fun setupSearch() {
        // Mencegah keyboard muncul otomatis
        binding.etSearch.clearFocus()

        // Listener ketika teks berubah
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.filterReports(text.toString())
            // Tampilkan/sembunyikan tombol clear
            binding.tvClearSearch.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        // Tombol clear search diklik
        binding.tvClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
            binding.etSearch.clearFocus()
        }

        // Atur agar ketika klik di luar EditText, keyboard akan tersembunyi
        binding.root.setOnClickListener {
            binding.etSearch.clearFocus()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // Jangan tampilkan empty state saat loading
            if (isLoading) {
                binding.emptyStateLayout.visibility = View.GONE
            }
        }

        viewModel.totalRevenue.observe(this) { total ->
            val localeID = Locale("in", "ID")
            val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
            // Hilangkan kode mata uang dan tambahkan "Rp" secara manual
            val formatted = formatRupiah.format(total)
                .replace("Rp", "") // Hapus Rp dari format (kalau ada)
                .trim()
            binding.tvTotalRevenue.text = "Rp $formatted"
        }

        viewModel.reportData.observe(this) { reports ->
            val isEmpty = reports.isNullOrEmpty()
            // Tampilkan/sembunyikan empty state layout
            binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE

            // Tampilkan data jika ada
            if (!isEmpty) {
                reportAdapter.submitList(reports)
            }
        }
    }
}