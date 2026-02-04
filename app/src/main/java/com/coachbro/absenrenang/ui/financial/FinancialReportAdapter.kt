package com.coachbro.absenrenang.ui.financial

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coachbro.absenrenang.data.model.FinancialReport
import com.coachbro.absenrenang.databinding.ItemFinancialReportBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class FinancialReportAdapter :
    ListAdapter<FinancialReport, FinancialReportAdapter.ReportViewHolder>(DiffCallback()) {

    // ===============================================================
    // OPTIMASI: Inisialisasi formatter di sini (Level Class)
    // agar hanya dibuat sekali, bukan berulang-ulang saat scroll.
    // ===============================================================
    private val dateLocale = Locale("id", "ID")
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", dateLocale)
    private val currencyFormat = NumberFormat.getCurrencyInstance(dateLocale)

    inner class ReportViewHolder(private val binding: ItemFinancialReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: FinancialReport) {
            binding.tvStudentName.text = report.studentName

            // Menggunakan formatter yang sudah disiapkan di atas
            binding.tvPaymentDate.text = report.paymentDate?.let { dateFormat.format(it) } ?: "-"

            // Format angka ke Rupiah
            // Tips: replace("Rp", "") jika ingin formatnya tanpa simbol mata uang,
            // tapi standar getCurrencyInstance sudah cukup rapi.
            binding.tvTotalAmount.text = currencyFormat.format(report.amount)
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
            // Pastikan ID unik yang dibandingkan
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FinancialReport, newItem: FinancialReport): Boolean {
            // Membandingkan seluruh isi objek untuk mengecek perubahan data
            return oldItem == newItem
        }
    }
}