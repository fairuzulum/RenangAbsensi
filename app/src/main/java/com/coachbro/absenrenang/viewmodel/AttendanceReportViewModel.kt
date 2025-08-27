package com.coachbro.absenrenang.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachbro.absenrenang.data.model.AttendanceReport
import com.coachbro.absenrenang.data.repository.StudentRepository
import kotlinx.coroutines.launch
import java.util.Date

class AttendanceReportViewModel : ViewModel() {

    private val repository = StudentRepository()

    private val _attendanceReport = MutableLiveData<List<AttendanceReport>>()
    val attendanceReport: LiveData<List<AttendanceReport>> = _attendanceReport

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchAttendanceReport(startDate: Date, endDate: Date) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getAttendanceReport(startDate, endDate).onSuccess {
                _attendanceReport.postValue(it)
            }.onFailure {
                _errorMessage.postValue("Gagal memuat laporan: ${it.message}")
            }
            _isLoading.postValue(false)
        }
    }
}