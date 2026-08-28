package com.coachbro.absenrenang.ui.report

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.coachbro.absenrenang.R
import com.coachbro.absenrenang.databinding.ActivityAttendanceReportBinding
import com.coachbro.absenrenang.viewmodel.AttendanceReportViewModel
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
    private var startCalendar = Calendar.getInstance()
    private var endCalendar = Calendar.getInstance()
    private val sdfDate = SimpleDateFormat("dd MMM", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDates()
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun initDates() {
        startCalendar.set(Calendar.HOUR_OF_DAY, 0)
        startCalendar.set(Calendar.MINUTE, 0)
        startCalendar.set(Calendar.SECOND, 0)
        startCalendar.set(Calendar.MILLISECOND, 0)
        
        endCalendar.set(Calendar.HOUR_OF_DAY, 23)
        endCalendar.set(Calendar.MINUTE, 59)
        endCalendar.set(Calendar.SECOND, 59)
        endCalendar.set(Calendar.MILLISECOND, 999)

        startDate = startCalendar.time
        endDate = endCalendar.time
        
        binding.btnStartDate.text = sdfDate.format(startDate!!)
        binding.btnEndDate.text = sdfDate.format(endDate!!)
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
        binding.btnStartDate.setOnClickListener { showDatePicker(true) }
        binding.btnEndDate.setOnClickListener { showDatePicker(false) }
        binding.btnSearch.setOnClickListener {
            viewModel.fetchAttendanceReport(startDate!!, endDate!!)
        }
        
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.filterReports(text.toString())
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_custom_date_picker, null)
        dialog.setContentView(view)

        val calendarView = view.findViewById<android.widget.CalendarView>(R.id.calendarView)
        val btnCancel = view.findViewById<View>(R.id.btnCancel)
        val btnSelect = view.findViewById<View>(R.id.btnSelect)

        val selectedCalendar = if (isStartDate) startCalendar else endCalendar
        calendarView.date = selectedCalendar.timeInMillis

        var tempDate = selectedCalendar.timeInMillis
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            tempDate = cal.timeInMillis
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSelect.setOnClickListener {
            val newCalendar = Calendar.getInstance()
            newCalendar.timeInMillis = tempDate

            if (isStartDate) {
                startCalendar.set(Calendar.YEAR, newCalendar.get(Calendar.YEAR))
                startCalendar.set(Calendar.MONTH, newCalendar.get(Calendar.MONTH))
                startCalendar.set(Calendar.DAY_OF_MONTH, newCalendar.get(Calendar.DAY_OF_MONTH))
                startDate = startCalendar.time
                binding.btnStartDate.text = sdfDate.format(startDate!!)
            } else {
                endCalendar.set(Calendar.YEAR, newCalendar.get(Calendar.YEAR))
                endCalendar.set(Calendar.MONTH, newCalendar.get(Calendar.MONTH))
                endCalendar.set(Calendar.DAY_OF_MONTH, newCalendar.get(Calendar.DAY_OF_MONTH))
                endDate = endCalendar.time
                binding.btnEndDate.text = sdfDate.format(endDate!!)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) {
            binding.loadingLayout.root.visibility = if (it) View.VISIBLE else View.GONE
            if (it) {
                binding.tvEmpty.visibility = View.GONE
                binding.cardSearch.visibility = View.GONE
            }
        }

        viewModel.attendanceReport.observe(this) { reports ->
            val isEmpty = reports.isNullOrEmpty()
            binding.tvEmpty.visibility = if (isEmpty && viewModel.isLoading.value != true) View.VISIBLE else View.GONE
            
            if (isEmpty) {
                binding.rvAttendanceReport.visibility = View.GONE
                binding.tvTotalAttendance.text = "0"
                binding.tvActiveStudents.text = "0"
                // Hide search if absolutely no data for this date range
                if (viewModel.isLoading.value != true && binding.etSearch.text.isNullOrEmpty()) {
                    binding.cardSearch.visibility = View.GONE
                }
            } else {
                binding.rvAttendanceReport.visibility = View.VISIBLE
                attendanceReportAdapter.submitList(reports)
                
                // Show search if we have data
                binding.cardSearch.visibility = View.VISIBLE
                
                // Update Summary Stats
                binding.tvTotalAttendance.text = reports.size.toString()
                binding.tvActiveStudents.text = reports.map { it.studentName }.distinct().size.toString()
            }
        }
    }
}