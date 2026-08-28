package com.coachbro.absenrenang.ui.studentlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coachbro.absenrenang.R

class FolderAdapter(private val onFolderClick: (com.coachbro.absenrenang.data.model.Classification) -> Unit) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {
    private var list = listOf<com.coachbro.absenrenang.data.model.Classification>()
    fun submitList(newList: List<com.coachbro.absenrenang.data.model.Classification>) {
        list = newList
        notifyDataSetChanged()
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvFolderName)
        val ivIcon: android.widget.ImageView = view.findViewById(R.id.ivFolderIcon)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_folder, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvName.text = item.name
        try {
            holder.ivIcon.setColorFilter(android.graphics.Color.parseColor(item.color))
        } catch (e: Exception) {
            holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#0084FF"))
        }
        holder.itemView.setOnClickListener { onFolderClick(item) }
    }
    override fun getItemCount() = list.size
}