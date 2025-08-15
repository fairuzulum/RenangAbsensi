// ui/studentlist/StudentAdapter.kt
package com.coachbro.absenrenang.ui.studentlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.databinding.ItemStudentBinding

class StudentAdapter : ListAdapter<Student, StudentAdapter.StudentViewHolder>(StudentDiffCallback()) {

    // ViewHolder: Menampung referensi ke view di dalam setiap item
    inner class StudentViewHolder(private val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(student: Student) {
            binding.tvStudentName.text = student.name
            binding.tvRemainingSessions.text = "${student.remainingSessions} Sesi"

            // Tampilkan info tambahan jika ada
            val studentInfo = mutableListOf<String>()
            student.age?.let { studentInfo.add("Umur: $it") }
            student.parentName?.let { studentInfo.add("Ortu: $it") }
            binding.tvStudentInfo.text = studentInfo.joinToString(", ")
        }
    }

    // Dipanggil saat RecyclerView perlu ViewHolder baru
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentViewHolder(binding)
    }

    // Dipanggil untuk menampilkan data di posisi tertentu
    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // DiffUtil: Membantu ListAdapter mengetahui item mana yang berubah,
    // ditambahkan, atau dihapus secara efisien. Ini adalah best practice!
    class StudentDiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem == newItem
        }
    }
}