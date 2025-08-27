package com.coachbro.absenrenang.ui.report

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.coachbro.absenrenang.databinding.ActivityAttendanceReportBinding
import com.coachbro.absenrenang.viewmodel.AttendanceReportViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AttendanceReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceReportBinding
    private val viewModel: AttendanceReportViewModel by viewModels()
    private lateinit var attendanceReportAdapter: AttendanceReportAdapter

    private var startDate: Date? = null
    private var endDate: Date? = null
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        attendanceReportAdapter = AttendanceReportAdapter()
        binding.rvAttendanceReport.apply {
            layoutManager = LinearLayoutManager(this@AttendanceReportActivity)
            adapter = attendanceReportAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnStartDate.setOnClickListener {
            showDatePicker(true)
        }

        binding.btnEndDate.setOnClickListener {
            showDatePicker(false)
        }

        binding.btnSearch.setOnClickListener {
            if (startDate != null && endDate != null) {
                viewModel.fetchAttendanceReport(startDate!!, endDate!!)
            } else {
                Toast.makeText(this, "Silakan pilih rentang tanggal terlebih dahulu.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal")
            .build()

        datePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it
            if (isStartDate) {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                startDate = calendar.time
                binding.btnStartDate.text = sdf.format(startDate!!)
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                endDate = calendar.time
                binding.btnEndDate.text = sdf.format(endDate!!)
            }
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }

        viewModel.attendanceReport.observe(this) {
            if (it.isNullOrEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvAttendanceReport.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvAttendanceReport.visibility = View.VISIBLE
                attendanceReportAdapter.submitList(it)
            }
        }
    }
}