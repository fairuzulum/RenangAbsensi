// viewmodel/PaymentDetailViewModel.kt
package com.coachbro.absenrenang.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachbro.absenrenang.data.model.Payment
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.data.repository.StudentRepository
import kotlinx.coroutines.launch

class PaymentDetailViewModel : ViewModel() {
    private val repository = StudentRepository()

    private val _student = MutableLiveData<Student?>()
    val student: LiveData<Student?> = _student

    private val _paymentHistory = MutableLiveData<List<Payment>>()
    val paymentHistory: LiveData<List<Payment>> = _paymentHistory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData untuk memberitahu UI status proses pembayaran
    private val _paymentStatus = MutableLiveData<Result<Unit>>()
    val paymentStatus: LiveData<Result<Unit>> = _paymentStatus

    fun fetchStudentDetails(studentId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            // Mengambil data siswa dan riwayat pembayaran secara bersamaan
            repository.getStudentById(studentId).onSuccess { _student.postValue(it) }
            repository.getPaymentHistory(studentId).onSuccess { _paymentHistory.postValue(it) }
            _isLoading.postValue(false)
        }
    }

    fun addPayment(studentId: String, amount: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.processPayment(studentId, amount)
            _paymentStatus.postValue(result) // Kirim hasilnya ke UI
            _isLoading.postValue(false)
        }
    }
}