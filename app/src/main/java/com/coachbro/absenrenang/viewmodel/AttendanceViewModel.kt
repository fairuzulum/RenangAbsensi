package com.coachbro.absenrenang.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.data.model.Classification
import com.coachbro.absenrenang.data.repository.StudentRepository
import kotlinx.coroutines.launch

class AttendanceViewModel : ViewModel() {

    private val repository = StudentRepository()

    private val _originalStudents = MutableLiveData<List<Student>>()

    private val _filteredStudents = MutableLiveData<List<Student>>()
    val students: LiveData<List<Student>> = _filteredStudents

    private val _classifications = MutableLiveData<List<Classification>>()
    val classifications: LiveData<List<Classification>> = _classifications

    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    init {
        fetchAllStudents()
        loadClassifications()
    }

    fun fetchAllStudents(forceRefresh: Boolean = false) {
        isLoading.value = true
        viewModelScope.launch {
            repository.getAllStudents(forceRefresh).onSuccess { studentList ->
                _originalStudents.postValue(studentList)
                _filteredStudents.postValue(studentList)
            }.onFailure { exception ->
                errorMessage.postValue("Gagal memuat data: ${exception.message}")
            }
            isLoading.postValue(false)
        }
    }

    fun loadClassifications() {
        viewModelScope.launch {
            repository.getClassifications().onSuccess {
                _classifications.postValue(it)
            }
        }
    }

    fun searchStudent(query: String) {
        val list = _originalStudents.value
        if (query.isBlank()) {
            _filteredStudents.value = list ?: emptyList()
        } else {
            _filteredStudents.value = list?.filter { student ->
                student.name.contains(query, ignoreCase = true) ||
                        student.nickname?.contains(query, ignoreCase = true) == true ||
                        student.phoneNumber.contains(query)
            }
        }
    }
}