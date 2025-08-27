package com.coachbro.absenrenang.ui.report

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coachbro.absenrenang.data.model.AttendanceReport
import com.coachbro.absenrenang.databinding.ItemAttendanceReportBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AttendanceReportAdapter :
    ListAdapter<AttendanceReport, AttendanceReportAdapter.ReportViewHolder>(DiffCallback()) {

    inner class ReportViewHolder(private val binding: ItemAttendanceReportBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(report: AttendanceReport) {
            binding.tvStudentName.text = report.studentName
            report.attendanceDate?.let {
                val format = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
                binding.tvAttendanceDate.text = format.format(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemAttendanceReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<AttendanceReport>() {
        override fun areItemsTheSame(oldItem: AttendanceReport, newItem: AttendanceReport): Boolean {
            return oldItem.studentName == newItem.studentName && oldItem.attendanceDate == newItem.attendanceDate
        }

        override fun areContentsTheSame(oldItem: AttendanceReport, newItem: AttendanceReport): Boolean {
            return oldItem == newItem
        }
    }
}