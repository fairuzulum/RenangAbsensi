package com.coachbro.absenrenang.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coachbro.absenrenang.R
import com.coachbro.absenrenang.data.model.Classification
import com.coachbro.absenrenang.data.repository.StudentRepository
import com.coachbro.absenrenang.databinding.ActivityClassificationManagementBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class ClassificationManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClassificationManagementBinding
    private val repository = StudentRepository()
    private lateinit var adapter: ClassificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassificationManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        adapter = ClassificationAdapter(
            onEdit = { classification -> showFolderDialog(classification) },
            onDelete = { classification -> deleteClassification(classification) }
        )
        binding.rvClassifications.apply {
            layoutManager = LinearLayoutManager(this@ClassificationManagementActivity)
            adapter = this@ClassificationManagementActivity.adapter
        }
    }

    private fun setupListeners() {
        binding.btnAddClassification.setOnClickListener {
            showFolderDialog()
        }
    }

    private fun loadData() {
        setLoading(true)
        lifecycleScope.launch {
            repository.getClassifications().onSuccess {
                adapter.submitList(it)
            }.onFailure {
                Toast.makeText(this@ClassificationManagementActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
            setLoading(false)
        }
    }

    private fun showFolderDialog(existing: Classification? = null) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_classification, null)
        dialog.setContentView(view)

        val tvTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        val etName = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName)
        val btnAdd = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAdd)
        val btnCancel = view.findViewById<View>(R.id.btnCancel)
        val layoutColorPresets = view.findViewById<android.widget.LinearLayout>(R.id.layoutColorPresets)

        var selectedColor = existing?.color ?: "#0084FF"
        
        if (existing != null) {
            tvTitle.text = "Edit Klasifikasi"
            etName.setText(existing.name)
            btnAdd.text = "Update"
        }

        // Handle color selection
        for (i in 0 until layoutColorPresets.childCount) {
            val colorView = layoutColorPresets.getChildAt(i)
            colorView.setOnClickListener {
                selectedColor = it.tag.toString()
                for (j in 0 until layoutColorPresets.childCount) {
                    val other = layoutColorPresets.getChildAt(j)
                    other.scaleX = if (other == it) 1.25f else 1.0f
                    other.scaleY = if (other == it) 1.25f else 1.0f
                    other.alpha = if (other == it) 1.0f else 0.5f
                }
            }
            // Initial state
            if (colorView.tag == selectedColor) {
                colorView.scaleX = 1.25f
                colorView.scaleY = 1.25f
                colorView.alpha = 1.0f
            } else {
                colorView.alpha = 0.5f
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isNotEmpty()) {
                setLoading(true)
                lifecycleScope.launch {
                    val result = if (existing == null) {
                        repository.addClassification(name, selectedColor)
                    } else {
                        repository.updateClassification(existing.copy(name = name, color = selectedColor))
                    }
                    
                    result.onSuccess {
                        loadData()
                        dialog.dismiss()
                    }.onFailure {
                        Toast.makeText(this@ClassificationManagementActivity, "Gagal memproses", Toast.LENGTH_SHORT).show()
                    }
                    setLoading(false)
                }
            }
        }
        dialog.show()
    }

    private fun deleteClassification(classification: Classification) {
        setLoading(true)
        lifecycleScope.launch {
            repository.deleteClassification(classification.id).onSuccess {
                loadData()
            }.onFailure {
                Toast.makeText(this@ClassificationManagementActivity, "Gagal menghapus", Toast.LENGTH_SHORT).show()
            }
            setLoading(false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingLayout.root.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}

class ClassificationAdapter(
    private val onEdit: (Classification) -> Unit,
    private val onDelete: (Classification) -> Unit
) : RecyclerView.Adapter<ClassificationAdapter.ViewHolder>() {
    private var list = listOf<Classification>()
    fun submitList(newList: List<Classification>) {
        list = newList
        notifyDataSetChanged()
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvClassificationName)
        val ivIcon: android.widget.ImageView = view.findViewById(R.id.ivClassificationIcon)
        val btnEdit: View = view.findViewById(R.id.btnEdit)
        val btnDelete: View = view.findViewById(R.id.btnDelete)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_classification, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvName.text = item.name
        try {
            holder.ivIcon.setColorFilter(android.graphics.Color.parseColor(item.color))
        } catch (e: Exception) {}
        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }
    override fun getItemCount() = list.size
}