package com.coachbro.absenrenang.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachbro.absenrenang.data.model.Attendance
import com.coachbro.absenrenang.data.model.Payment
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.data.repository.StudentRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class StudentDetailViewModel : ViewModel() {

    private val repository = StudentRepository()

    private val _student = MutableLiveData<Student?>()
    val student: LiveData<Student?> = _student

    private val _attendanceHistory = MutableLiveData<List<Attendance>>()
    val attendanceHistory: LiveData<List<Attendance>> = _attendanceHistory

    private val _paymentHistory = MutableLiveData<List<Payment>>()
    val paymentHistory: LiveData<List<Payment>> = _paymentHistory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _updateSessionStatus = MutableLiveData<Result<Unit>>()
    val updateSessionStatus: LiveData<Result<Unit>> = _updateSessionStatus

    private val _deleteStatus = MutableLiveData<Result<Unit>>()
    val deleteStatus: LiveData<Result<Unit>> = _deleteStatus

    // Meniru Promise.all di Web: Fetch semua data secara paralel
    fun loadStudentData(studentId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Jalankan request secara paralel agar cepat
                val studentDeferred = async { repository.getStudentById(studentId) }
                val attendanceDeferred = async { repository.getAttendanceHistory(studentId) }
                val paymentDeferred = async { repository.getPaymentHistory(studentId) }

                val studentResult = studentDeferred.await()
                val attendanceResult = attendanceDeferred.await()
                val paymentResult = paymentDeferred.await()

                if (studentResult.isSuccess) {
                    _student.value = studentResult.getOrNull()
                } else {
                    _errorMessage.value = "Gagal memuat data siswa: ${studentResult.exceptionOrNull()?.message}"
                }

                if (attendanceResult.isSuccess) {
                    _attendanceHistory.value = attendanceResult.getOrNull() ?: emptyList()
                }

                if (paymentResult.isSuccess) {
                    _paymentHistory.value = paymentResult.getOrNull() ?: emptyList()
                }

            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Logic update sesi manual
    fun updateStudentSessions(studentId: String, newCount: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.updateStudentSessions(studentId, newCount)
            _updateSessionStatus.value = result
            if (result.isSuccess) {
                // Update local data biar UI langsung berubah
                val currentStudent = _student.value
                if (currentStudent != null) {
                    _student.value = currentStudent.copy(remainingSessions = newCount)
                }
            }
            _isLoading.value = false
        }
    }

    fun deleteStudent(studentId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.deleteStudent(studentId)
            _deleteStatus.value = result
            _isLoading.value = false
        }
    }
}