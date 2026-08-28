// ui/studentlist/StudentAdapter.kt
package com.coachbro.absenrenang.ui.studentlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.databinding.ItemStudentBinding

class StudentAdapter(
    private val onItemClick: (Student, View) -> Unit
) : ListAdapter<Student, StudentAdapter.StudentViewHolder>(StudentDiffCallback()) {

    inner class StudentViewHolder(private val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(bindingAdapterPosition), binding.tvStudentName)
                }
            }

            binding.btnWhatsApp.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    val student = getItem(bindingAdapterPosition)
                    openWhatsApp(student.phoneNumber)
                }
            }
        }

        private fun openWhatsApp(number: String) {
            if (number.isBlank()) {
                Toast.makeText(binding.root.context, "Nomor WA tidak tersedia", Toast.LENGTH_SHORT).show()
                return
            }

            val context = binding.root.context
            var cleanNumber = number.replace(Regex("[^0-9]"), "")
            if (cleanNumber.startsWith("0")) {
                cleanNumber = "62" + cleanNumber.substring(1)
            }
            
            try {
                val url = "https://wa.me/$cleanNumber"
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                intent.data = android.net.Uri.parse(url)
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal membuka WhatsApp", Toast.LENGTH_SHORT).show()
            }
        }

        fun bind(student: Student) {
            binding.tvStudentName.text = student.name
            binding.tvRemainingSessions.text = student.remainingSessions.toString()
            
            // Red Alert if sessions <= 0
            if (student.remainingSessions <= 0) {
                binding.bubbleRemaining.setCardBackgroundColor(android.graphics.Color.parseColor("#FF4B55"))
                binding.root.strokeColor = android.graphics.Color.parseColor("#FF4B55")
                binding.root.strokeWidth = 2
            } else {
                binding.bubbleRemaining.setCardBackgroundColor(android.graphics.Color.parseColor("#0084FF"))
                binding.root.strokeColor = android.graphics.Color.parseColor("#DEE3EB") // divider color
                binding.root.strokeWidth = 1
            }

            binding.tvStudentName.transitionName = "name_${student.id}"

            if (student.nickname.isNullOrBlank()) {
                binding.tvStudentNickname.visibility = View.GONE
            } else {
                binding.tvStudentNickname.visibility = View.VISIBLE
                binding.tvStudentNickname.text = "(${student.nickname})"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StudentDiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem == newItem
        }
    }
}