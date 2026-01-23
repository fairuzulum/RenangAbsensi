// fairuzulum/renangabsensi/RenangAbsensi-f6a1f8a5bfe74466545d06b313d4e1e548504b00/app/src/main/java/com/coachbro/absenrenang/ui/report/AttendanceReportActivity.kt
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
import com.google.android.material.timepicker.MaterialTimePicker //
import com.google.android.material.timepicker.TimeFormat //
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
    private var startCalendar = Calendar.getInstance() //
    private var endCalendar = Calendar.getInstance() //
    private val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault()) //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setel waktu awal ke hari ini (pukul 00:00:00 - 23:59:59)
        startCalendar.set(Calendar.HOUR_OF_DAY, 0) //
        startCalendar.set(Calendar.MINUTE, 0) //
        startCalendar.set(Calendar.SECOND, 0) //
        startCalendar.set(Calendar.MILLISECOND, 0) //
        endCalendar.set(Calendar.HOUR_OF_DAY, 23) //
        endCalendar.set(Calendar.MINUTE, 59) //
        endCalendar.set(Calendar.SECOND, 59) //
        endCalendar.set(Calendar.MILLISECOND, 999) //

        startDate = startCalendar.time
        endDate = endCalendar.time
        binding.btnStartDate.text = sdfDate.format(startDate!!)
        binding.btnEndDate.text = sdfDate.format(endDate!!)
        binding.btnStartTime.text = sdfTime.format(startDate!!) //
        binding.btnEndTime.text = sdfTime.format(endDate!!) //


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

        binding.btnStartTime.setOnClickListener { //
            showTimePicker(true) //
        }

        binding.btnEndTime.setOnClickListener { //
            showTimePicker(false) //
        }

        binding.btnSearch.setOnClickListener {
            if (startDate != null && endDate != null && startDate!!.before(endDate) || startDate!!.equals(endDate)) {
                viewModel.fetchAttendanceReport(startDate!!, endDate!!)
            } else {
                Toast.makeText(this, "Silakan pilih rentang tanggal dan jam yang valid.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val selectedCalendar = if (isStartDate) startCalendar else endCalendar
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal")
            .setSelection(selectedCalendar.timeInMillis) //
            .build()

        datePicker.addOnPositiveButtonClickListener {
            val newCalendar = Calendar.getInstance()
            newCalendar.timeInMillis = it

            // Pertahankan jam dan menit yang sudah dipilih
            val hour = selectedCalendar.get(Calendar.HOUR_OF_DAY)
            val minute = selectedCalendar.get(Calendar.MINUTE)
            val second = if (isStartDate) 0 else 59
            val millisecond = if (isStartDate) 0 else 999

            newCalendar.set(Calendar.HOUR_OF_DAY, hour)
            newCalendar.set(Calendar.MINUTE, minute)
            newCalendar.set(Calendar.SECOND, second)
            newCalendar.set(Calendar.MILLISECOND, millisecond)

            if (isStartDate) {
                startCalendar = newCalendar
                startDate = startCalendar.time
                binding.btnStartDate.text = sdfDate.format(startDate!!)
            } else {
                endCalendar = newCalendar
                endDate = endCalendar.time
                binding.btnEndDate.text = sdfDate.format(endDate!!)
            }
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun showTimePicker(isStartTime: Boolean) { //
        val selectedCalendar = if (isStartTime) startCalendar else endCalendar
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(selectedCalendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(selectedCalendar.get(Calendar.MINUTE))
            .setTitleText("Pilih Jam dan Menit")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            if (isStartTime) {
                startCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                startCalendar.set(Calendar.MINUTE, timePicker.minute)
                startCalendar.set(Calendar.SECOND, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)
                startDate = startCalendar.time
                binding.btnStartTime.text = sdfTime.format(startDate!!)
            } else {
                endCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                endCalendar.set(Calendar.MINUTE, timePicker.minute)
                endCalendar.set(Calendar.SECOND, 59)
                endCalendar.set(Calendar.MILLISECOND, 999)
                endDate = endCalendar.time
                binding.btnEndTime.text = sdfTime.format(endDate!!)
            }
        }
        timePicker.show(supportFragmentManager, "TIME_PICKER")
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