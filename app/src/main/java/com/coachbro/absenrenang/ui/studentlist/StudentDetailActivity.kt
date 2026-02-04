package com.coachbro.absenrenang.ui.studentlist

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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

    // Adapter didefinisikan di bagian bawah file ini
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

        // Load data mirip useEffect di React
        viewModel.loadStudentData(currentStudentId!!)
    }

    override fun onResume() {
        super.onResume()
        // Refresh data jika user kembali setelah Edit Data
        currentStudentId?.let { viewModel.loadStudentData(it) }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Setup menu Edit & Delete di pojok kanan atas
        binding.toolbar.inflateMenu(R.menu.menu_student_detail)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    currentStudent?.let {
                        val intent = Intent(this, EditStudentActivity::class.java).apply {
                            putExtra(EditStudentActivity.EXTRA_STUDENT, it)
                        }
                        startActivity(intent)
                    }
                    true
                }
                R.id.action_delete -> {
                    showDeleteConfirmation()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerViews() {
        // Setup List Absensi
        attendanceAdapter = DetailAttendanceAdapter()
        binding.rvAttendanceHistory.apply {
            layoutManager = LinearLayoutManager(this@StudentDetailActivity)
            adapter = attendanceAdapter
        }

        // Setup List Pembayaran
        paymentAdapter = DetailPaymentAdapter()
        binding.rvPaymentHistory.apply {
            layoutManager = LinearLayoutManager(this@StudentDetailActivity)
            adapter = paymentAdapter
        }
    }

    private fun setupActions() {
        // Logic Simpan Sesi Manual
        binding.btnSaveSession.setOnClickListener {
            val newSessionStr = binding.etEditSession.text.toString()
            if (newSessionStr.isBlank()) {
                Toast.makeText(this, "Masukkan angka valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newSession = newSessionStr.toIntOrNull()
            if (newSession == null) {
                Toast.makeText(this, "Harus berupa angka", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            currentStudentId?.let { id ->
                viewModel.updateStudentSessions(id, newSession)
            }
        }

        // --- Logic Dropdown / Accordion ---

        // Klik Header Absensi -> Buka/Tutup
        binding.btnToggleAttendance.setOnClickListener {
            toggleSection(binding.layoutAttendanceContent, binding.ivAttendanceArrow)
        }

        // Klik Header Pembayaran -> Buka/Tutup
        binding.btnTogglePayment.setOnClickListener {
            toggleSection(binding.layoutPaymentContent, binding.ivPaymentArrow)
        }
    }

    // Helper Function: Animasi Buka/Tutup & Putar Panah
    private fun toggleSection(contentView: View, arrowView: View) {
        if (contentView.visibility == View.VISIBLE) {
            // Tutup
            contentView.visibility = View.GONE
            arrowView.animate().rotation(0f).setDuration(200).start()
        } else {
            // Buka
            contentView.visibility = View.VISIBLE
            arrowView.animate().rotation(180f).setDuration(200).start()
        }
    }

    private fun observeViewModel() {
        // Loading State
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Error Message
        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrBlank()) Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }

        // Data Siswa
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

                // Warna teks sisa sesi (Hijau jika > 0, Merah jika <= 0)
                val colorRes = if (student.remainingSessions > 0) android.R.color.holo_green_dark else android.R.color.holo_red_dark
                binding.tvSessionCount.setTextColor(resources.getColor(colorRes, null))

                // Isi EditText hanya jika kosong (biar tidak overwrite saat user ngetik)
                if (binding.etEditSession.text?.isEmpty() ?: true) {
                    binding.etEditSession.setText(student.remainingSessions.toString())
                }
            }
        }

        // Data Riwayat Absensi
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

        // Data Riwayat Pembayaran
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

        // Hasil Update Sesi
        viewModel.updateSessionStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Sisa pertemuan berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                binding.etEditSession.text?.clear() // Kosongkan input setelah sukses
            }.onFailure {
                Toast.makeText(this, "Gagal update: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Hasil Delete Siswa
        viewModel.deleteStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Siswa berhasil dihapus", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke halaman list
            }.onFailure {
                Toast.makeText(this, "Gagal hapus: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Siswa")
            .setMessage("Apakah Anda yakin ingin menghapus data ${currentStudent?.name}? Aksi ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                currentStudentId?.let { viewModel.deleteStudent(it) }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}

// ================= ADAPTERS =================

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
        // Pastikan layout item_simple_history.xml sudah dibuat
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_simple_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm", Locale("id", "ID"))

        holder.tvMain.text = try { dateFormat.format(item.date) } catch (e: Exception) { "Tanggal Invalid" }
        holder.tvSub.visibility = View.GONE // Absen biasanya hanya tanggal
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