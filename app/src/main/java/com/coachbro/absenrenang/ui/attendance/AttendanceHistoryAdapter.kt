// ui/attendance/AttendanceHistoryAdapter.kt
package com.coachbro.absenrenang.ui.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coachbro.absenrenang.data.model.Attendance
import com.coachbro.absenrenang.databinding.ItemAttendanceHistoryBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AttendanceHistoryAdapter : ListAdapter<Attendance, AttendanceHistoryAdapter.AttendanceViewHolder>(DiffCallback()) {

    class AttendanceViewHolder(private val binding: ItemAttendanceHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(attendance: Attendance) {
            attendance.date?.let {
                val format = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
                binding.tvAttendanceDate.text = format.format(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val binding = ItemAttendanceHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttendanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Attendance>() {
        override fun areItemsTheSame(oldItem: Attendance, newItem: Attendance): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Attendance, newItem: Attendance): Boolean = oldItem == newItem
    }
}