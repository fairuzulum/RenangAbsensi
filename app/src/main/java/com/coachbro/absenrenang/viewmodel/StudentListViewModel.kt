// viewmodel/StudentListViewModel.kt
package com.coachbro.absenrenang.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.data.repository.StudentRepository
import kotlinx.coroutines.launch

class StudentListViewModel : ViewModel() {

    private val repository = StudentRepository()

    // LiveData untuk daftar siswa
    private val _students = MutableLiveData<List<Student>>()
    val students: LiveData<List<Student>> = _students

    // LiveData untuk status loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData untuk pesan error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Fungsi untuk memulai pengambilan data
    fun fetchAllStudents() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getAllStudents()
            result.onSuccess { studentList ->
                _students.postValue(studentList)
            }.onFailure { exception ->
                _errorMessage.postValue("Gagal memuat data: ${exception.message}")
            }
            _isLoading.postValue(false)
        }
    }
}