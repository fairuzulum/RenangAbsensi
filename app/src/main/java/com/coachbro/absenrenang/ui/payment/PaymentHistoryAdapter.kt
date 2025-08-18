// ui/payment/PaymentHistoryAdapter.kt
package com.coachbro.absenrenang.ui.payment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coachbro.absenrenang.data.model.Payment
import com.coachbro.absenrenang.databinding.ItemPaymentHistoryBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class PaymentHistoryAdapter : ListAdapter<Payment, PaymentHistoryAdapter.PaymentViewHolder>(PaymentDiffCallback()) {

    inner class PaymentViewHolder(private val binding: ItemPaymentHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(payment: Payment) {
            // Format tanggal
            payment.date?.let {
                val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
                binding.tvPaymentDate.text = dateFormat.format(it)
            }

            // Format mata uang Rupiah
            val localeID = Locale("in", "ID")
            val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
            binding.tvPaymentAmount.text = formatRupiah.format(payment.amount)

            binding.tvSessionsAdded.text = "+${payment.sessionsAdded} Pertemuan"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPaymentHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PaymentDiffCallback : DiffUtil.ItemCallback<Payment>() {
        override fun areItemsTheSame(oldItem: Payment, newItem: Payment): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Payment, newItem: Payment): Boolean {
            return oldItem == newItem
        }
    }
}