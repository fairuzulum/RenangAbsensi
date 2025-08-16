// ui/studentlist/StudentAdapter.kt
package com.coachbro.absenrenang.ui.studentlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.databinding.ItemStudentBinding

class StudentAdapter(
    private val onItemClick: (Student) -> Unit
) : ListAdapter<Student, StudentAdapter.StudentViewHolder>(StudentDiffCallback()) {

    inner class StudentViewHolder(private val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(bindingAdapterPosition))
                }
            }
        }

        /**
         * Fungsi 'bind' ini dipanggil untuk setiap item yang akan ditampilkan.
         * Di sinilah kita akan menambahkan logika untuk nama panggilan.
         */
        fun bind(student: Student) {
            binding.tvStudentName.text = student.name
            binding.tvRemainingSessions.text = student.remainingSessions.toString()

            // ===============================================================
            // LOGIKA BARU UNTUK MENAMPILKAN NAMA PANGGILAN ADA DI SINI
            // ===============================================================
            if (student.nickname.isNullOrBlank()) {
                // Jika nama panggilan kosong atau null, sembunyikan TextView-nya
                binding.tvStudentNickname.visibility = View.GONE
            } else {
                // Jika ada, tampilkan TextView-nya dan isi datanya
                binding.tvStudentNickname.visibility = View.VISIBLE
                binding.tvStudentNickname.text = "(${student.nickname})"
            }
            // ===============================================================
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