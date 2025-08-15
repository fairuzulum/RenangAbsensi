// viewmodel/AttendanceDetailViewModel.kt
package com.coachbro.absenrenang.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachbro.absenrenang.data.model.Attendance
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.data.repository.StudentRepository
import kotlinx.coroutines.launch

class AttendanceDetailViewModel : ViewModel() {
    private val repository = StudentRepository()

    private val _student = MutableLiveData<Student?>()
    val student: LiveData<Student?> = _student

    private val _attendanceHistory = MutableLiveData<List<Attendance>>()
    val attendanceHistory: LiveData<List<Attendance>> = _attendanceHistory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // ===============================================================
    // LiveData BARU untuk menyimpan status absensi hari ini
    // ===============================================================
    private val _hasAttendedToday = MutableLiveData<Boolean>()
    val hasAttendedToday: LiveData<Boolean> = _hasAttendedToday

    private val _attendanceStatus = MutableLiveData<Result<Unit>>()
    val attendanceStatus: LiveData<Result<Unit>> = _attendanceStatus

    fun fetchStudentDetails(studentId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            // Mengambil semua data yang dibutuhkan secara bersamaan
            repository.getStudentById(studentId).onSuccess { _student.postValue(it) }
            repository.getAttendanceHistory(studentId).onSuccess { _attendanceHistory.postValue(it) }

            // ===============================================================
            // Panggil fungsi repository yang baru untuk mengecek absensi
            // ===============================================================
            repository.checkIfStudentAttendedToday(studentId).onSuccess {
                _hasAttendedToday.postValue(it)
            }

            _isLoading.postValue(false)
        }
    }

    fun markAsPresent(studentId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.processAttendance(studentId)
            _attendanceStatus.postValue(result)

            // ===============================================================
            // Optimasi: Jika absensi berhasil, langsung update status di ViewModel
            // tanpa perlu query ulang. Ini membuat UI lebih responsif.
            // ===============================================================
            if (result.isSuccess) {
                _hasAttendedToday.postValue(true)
            }

            _isLoading.postValue(false)
        }
    }
}