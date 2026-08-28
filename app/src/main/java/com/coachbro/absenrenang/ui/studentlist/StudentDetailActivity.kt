package com.coachbro.absenrenang.ui.studentlist

import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coachbro.absenrenang.R
import com.coachbro.absenrenang.data.model.Attendance
import com.coachbro.absenrenang.data.model.Payment
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.databinding.ActivityStudentDetailBinding
import com.coachbro.absenrenang.viewmodel.StudentDetailViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class StudentDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentDetailBinding
    private val viewModel: StudentDetailViewModel by viewModels()

    private lateinit var attendanceAdapter: DetailAttendanceAdapter
    private lateinit var paymentAdapter: DetailPaymentAdapter

    private var currentStudentId: String? = null
    private var currentStudent: Student? = null

    companion object {
        const val EXTRA_STUDENT_ID = "extra_student_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentStudentId = intent.getStringExtra(EXTRA_STUDENT_ID)

        if (currentStudentId == null) {
            Toast.makeText(this, "ID Siswa tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerViews()
        setupActions()
        observeViewModel()

        viewModel.loadStudentData(currentStudentId!!)
    }

    override fun onResume() {
        super.onResume()
        currentStudentId?.let { viewModel.loadStudentData(it) }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerViews() {
        attendanceAdapter = DetailAttendanceAdapter()
        binding.rvAttendanceHistory.apply {
            layoutManager = LinearLayoutManager(this@StudentDetailActivity)
            adapter = attendanceAdapter
        }

        paymentAdapter = DetailPaymentAdapter()
        binding.rvPaymentHistory.apply {
            layoutManager = LinearLayoutManager(this@StudentDetailActivity)
            adapter = paymentAdapter
        }
    }

    private fun setupActions() {
        // Tombol Update Biodata
        binding.btnUpdateBiodata.setOnClickListener {
            currentStudent?.let {
                val intent = Intent(this, EditStudentActivity::class.java).apply {
                    putExtra(EditStudentActivity.EXTRA_STUDENT, it)
                }
                startActivity(intent)
            }
        }

        // Tombol Sesuaikan Sesi
        binding.btnEditSessions.setOnClickListener {
            showEditSessionsDialog()
        }

        // Accordions
        binding.btnToggleAttendance.setOnClickListener {
            toggleSection(binding.layoutAttendanceContent, binding.ivAttendanceArrow)
        }

        binding.btnTogglePayment.setOnClickListener {
            toggleSection(binding.layoutPaymentContent, binding.ivPaymentArrow)
        }

        binding.btnOpenWA.setOnClickListener {
            currentStudent?.phoneNumber?.let { openWhatsApp(it) }
        }
    }

    private fun openWhatsApp(number: String) {
        if (number.isBlank()) {
            Toast.makeText(this, "Nomor WA tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        var cleanNumber = number.replace(Regex("[^0-9]"), "")
        if (cleanNumber.startsWith("0")) {
            cleanNumber = "62" + cleanNumber.substring(1)
        }
        
        try {
            val url = "https://wa.me/$cleanNumber"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse(url)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membuka WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSection(contentView: View, arrowView: View) {
        val isVisible = contentView.visibility == View.VISIBLE
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup, AutoTransition())
        if (isVisible) {
            contentView.visibility = View.GONE
            arrowView.animate().rotation(0f).setDuration(300).start()
        } else {
            contentView.visibility = View.VISIBLE
            arrowView.animate().rotation(180f).setDuration(300).start()
        }
    }

    private fun showEditSessionsDialog() {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_sessions, null)
        dialog.setContentView(dialogView)

        val etSessions = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSessions)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<View>(R.id.btnSave)

        etSessions.setText(currentStudent?.remainingSessions?.toString() ?: "0")

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val newVal = etSessions.text.toString().toIntOrNull()
            if (newVal != null) {
                currentStudentId?.let { id -> viewModel.updateStudentSessions(id, newVal) }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Input tidak valid", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingLayout.root.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrBlank()) Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }

        viewModel.student.observe(this) { student ->
            if (student != null) {
                currentStudent = student
                binding.tvDetailName.text = student.name
                if (!student.nickname.isNullOrBlank()) {
                    binding.tvDetailNickname.text = "(${student.nickname})"
                    binding.tvDetailNickname.visibility = View.VISIBLE
                } else {
                    binding.tvDetailNickname.visibility = View.GONE
                }

                binding.tvSessionCount.text = student.remainingSessions.toString()
                
                // Bubble color adjustment if needed (Optional)
                if (student.remainingSessions <= 0) {
                    binding.bubbleSession.setCardBackgroundColor(ContextCompat.getColor(this, R.color.error))
                } else {
                    binding.bubbleSession.setCardBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue))
                }

                if (student.phoneNumber.isNotBlank()) {
                    binding.cardStudentInfo.visibility = View.VISIBLE
                    binding.tvDetailPhone.text = student.phoneNumber
                } else {
                    binding.cardStudentInfo.visibility = View.GONE
                }
            }
        }

        viewModel.attendanceHistory.observe(this) { list ->
            binding.tvAttendanceHeader.text = "Riwayat Kehadiran (${list.size})"
            if (list.isEmpty()) {
                binding.tvEmptyAttendance.visibility = View.VISIBLE
                binding.rvAttendanceHistory.visibility = View.GONE
            } else {
                binding.tvEmptyAttendance.visibility = View.GONE
                binding.rvAttendanceHistory.visibility = View.VISIBLE
                attendanceAdapter.submitList(list)
            }
        }

        viewModel.paymentHistory.observe(this) { list ->
            binding.tvPaymentHeader.text = "Riwayat Pembayaran (${list.size})"
            if (list.isEmpty()) {
                binding.tvEmptyPayment.visibility = View.VISIBLE
                binding.rvPaymentHistory.visibility = View.GONE
            } else {
                binding.tvEmptyPayment.visibility = View.GONE
                binding.rvPaymentHistory.visibility = View.VISIBLE
                paymentAdapter.submitList(list)
            }
        }

        viewModel.updateSessionStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Sisa pertemuan berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this, "Gagal update: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class DetailAttendanceAdapter : RecyclerView.Adapter<DetailAttendanceAdapter.ViewHolder>() {
    private var list = listOf<Attendance>()
    fun submitList(newList: List<Attendance>) {
        list = newList
        notifyDataSetChanged()
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMain: TextView = view.findViewById(R.id.tvMainInfo)
        val tvSub: TextView = view.findViewById(R.id.tvSubInfo)
        val tvDetail: TextView = view.findViewById(R.id.tvDetailInfo)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_simple_history, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm", Locale("id", "ID"))
        holder.tvMain.text = try { dateFormat.format(item.date) } catch (e: Exception) { "Tanggal Invalid" }
        holder.tvSub.visibility = View.GONE
        holder.tvDetail.visibility = View.GONE
    }
    override fun getItemCount() = list.size
}

class DetailPaymentAdapter : RecyclerView.Adapter<DetailPaymentAdapter.ViewHolder>() {
    private var list = listOf<Payment>()
    fun submitList(newList: List<Payment>) {
        list = newList
        notifyDataSetChanged()
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMain: TextView = view.findViewById(R.id.tvMainInfo)
        val tvSub: TextView = view.findViewById(R.id.tvSubInfo)
        val tvDetail: TextView = view.findViewById(R.id.tvDetailInfo)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_simple_history, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val dateFormat = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        currencyFormat.maximumFractionDigits = 0
        holder.tvMain.text = try { dateFormat.format(item.date) } catch (e: Exception) { "-" }
        holder.tvSub.text = "+${item.sessionsAdded} Sesi"
        holder.tvSub.visibility = View.VISIBLE
        holder.tvSub.setTextColor(holder.itemView.context.resources.getColor(android.R.color.holo_green_dark, null))
        holder.tvDetail.text = currencyFormat.format(item.amount)
        holder.tvDetail.visibility = View.VISIBLE
    }
    override fun getItemCount() = list.size
}