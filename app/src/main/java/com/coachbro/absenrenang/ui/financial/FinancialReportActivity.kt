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
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.filterReports(text.toString())
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingLayout.root.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) binding.tvEmpty.visibility = View.GONE
        }

        viewModel.totalRevenue.observe(this) { total ->
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            val formatted = formatRupiah.format(total).replace("Rp", "Rp ").trim()
            binding.tvTotalRevenue.text = formatted
        }

        viewModel.reportData.observe(this) { reports ->
            val isEmpty = reports.isNullOrEmpty()
            binding.tvEmpty.visibility = if (isEmpty && viewModel.isLoading.value != true) View.VISIBLE else View.GONE
            
            if (!isEmpty) {
                reportAdapter.submitList(reports)
                
                // Update Summary Stats
                binding.tvTransactionCount.text = reports.size.toString()
                val average = if (reports.isNotEmpty()) reports.sumOf { it.amount } / reports.size else 0L
                val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                binding.tvAverageTransaction.text = formatRupiah.format(average).replace("Rp", "Rp ").trim()
            } else {
                binding.tvTransactionCount.text = "0"
                binding.tvAverageTransaction.text = "Rp 0"
            }
        }
    }
}