// ui/studentlist/StudentAdapter.kt
package com.coachbro.absenrenang.ui.studentlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.databinding.ItemStudentBinding

// TAMBAHKAN parameter onItemClick di constructor
class StudentAdapter(
    private val onItemClick: (Student) -> Unit
) : ListAdapter<Student, StudentAdapter.StudentViewHolder>(StudentDiffCallback()) {

    inner class StudentViewHolder(private val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root) {

        // TAMBAHKAN blok init untuk mendaftarkan listener klik
        init {
            binding.root.setOnClickListener {
                // Pastikan posisi item valid sebelum memanggil listener
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(bindingAdapterPosition))
                }
            }
        }

        fun bind(student: Student) {
            binding.tvStudentName.text = student.name
            binding.tvRemainingSessions.text = "${student.remainingSessions} Sesi"

            val studentInfo = mutableListOf<String>()
            student.age?.let { studentInfo.add("Umur: $it") }
            student.parentName?.let { studentInfo.add("Ortu: $it") }
//            binding.tvStudentInfo.text = studentInfo.joinToString(", ")
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