// viewmodel/RegisterViewModel.kt
package com.coachbro.absenrenang.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.data.repository.StudentRepository
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val repository = StudentRepository()
    private val _registrationStatus = MutableLiveData<Result<Unit>>()
    val registrationStatus: LiveData<Result<Unit>> = _registrationStatus

    // Fungsi disederhanakan hanya menerima nama lengkap, panggilan, klasifikasi, dan nomor hp
    fun registerStudent(fullName: String, nickname: String, classification: String, phoneNumber: String) {
        if (fullName.isBlank()) {
            _registrationStatus.value = Result.failure(Exception("Nama lengkap tidak boleh kosong"))
            return
        }

        val student = Student(
            name = fullName,
            nickname = nickname.ifBlank { null },
            classification = classification,
            phoneNumber = phoneNumber
        )

        viewModelScope.launch {
            val result = repository.registerStudent(student)
            _registrationStatus.postValue(result)
        }
    }

    private val _classifications = MutableLiveData<List<com.coachbro.absenrenang.data.model.Classification>>()
    val classifications: LiveData<List<com.coachbro.absenrenang.data.model.Classification>> = _classifications

    fun loadClassifications() {
        viewModelScope.launch {
            repository.getClassifications().onSuccess {
                _classifications.postValue(it)
            }
        }
    }
}