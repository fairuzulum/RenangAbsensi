// ui/financial/FinancialReportActivity.kt
package com.coachbro.absenrenang.ui.financial

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.totalRevenue.observe(this) { total ->
            val localeID = Locale("in", "ID")
            val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
            binding.tvTotalRevenue.text = formatRupiah.format(total)
        }

        viewModel.reportData.observe(this) { reports ->
            binding.tvEmpty.visibility = if (reports.isNullOrEmpty()) View.VISIBLE else View.GONE
            reportAdapter.submitList(reports)
        }
    }
}