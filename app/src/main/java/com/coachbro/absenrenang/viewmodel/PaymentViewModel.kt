// viewmodel/PaymentViewModel.kt
package com.coachbro.absenrenang.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.data.repository.StudentRepository
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {
    private val repository = StudentRepository()

    private val _originalStudents = MutableLiveData<List<Student>>()

    private val _filteredStudents = MutableLiveData<List<Student>>()
    val filteredStudents: LiveData<List<Student>> = _filteredStudents

    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    init {
        fetchAllStudents()
    }

    private fun fetchAllStudents() {
        isLoading.value = true
        viewModelScope.launch {
            repository.getAllStudents().onSuccess {
                _originalStudents.postValue(it)
                _filteredStudents.postValue(it)
            }.onFailure {
                errorMessage.postValue("Gagal memuat data: ${it.message}")
            }
            isLoading.postValue(false)
        }
    }

    fun searchStudent(query: String) {
        val list = _originalStudents.value
        if (query.isBlank()) {
            _filteredStudents.value = list
        } else {
            _filteredStudents.value = list?.filter { student ->
                // Cari di nama lengkap ATAU di nama panggilan
                student.name.contains(query, ignoreCase = true) ||
                        student.nickname?.contains(query, ignoreCase = true) == true
            }
        }
    }
}