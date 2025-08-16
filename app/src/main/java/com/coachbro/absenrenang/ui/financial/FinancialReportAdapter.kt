// ui/financial/FinancialReportAdapter.kt
package com.coachbro.absenrenang.ui.financial

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coachbro.absenrenang.data.model.FinancialReport
import com.coachbro.absenrenang.databinding.ItemFinancialReportBinding
import java.text.NumberFormat
import java.util.Locale

class FinancialReportAdapter :
    ListAdapter<FinancialReport, FinancialReportAdapter.ReportViewHolder>(DiffCallback()) {

    inner class ReportViewHolder(private val binding: ItemFinancialReportBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(report: FinancialReport) {
            binding.tvStudentName.text = report.studentName

            val localeID = Locale("in", "ID")
            val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
            binding.tvTotalAmount.text = formatRupiah.format(report.totalAmount)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemFinancialReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<FinancialReport>() {
        override fun areItemsTheSame(oldItem: FinancialReport, newItem: FinancialReport): Boolean {
            return oldItem.studentName == newItem.studentName
        }

        override fun areContentsTheSame(oldItem: FinancialReport, newItem: FinancialReport): Boolean {
            return oldItem == newItem
        }
    }
}