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

    // Menyimpan daftar siswa asli dari Firestore
    private val _originalStudents = MutableLiveData<List<Student>>()

    // Mengganti nama _students menjadi _filteredStudents agar lebih jelas
    private val _filteredStudents = MutableLiveData<List<Student>>()
    val students: LiveData<List<Student>> = _filteredStudents // nama public tetap 'students' agar tidak error di activity

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // ===============================================================
    // LiveData BARU untuk status update dan delete
    // ===============================================================
    private val _updateStatus = MutableLiveData<Result<Unit>>()
    val updateStatus: LiveData<Result<Unit>> = _updateStatus

    private val _deleteStatus = MutableLiveData<Result<Unit>>()
    val deleteStatus: LiveData<Result<Unit>> = _deleteStatus

    fun fetchAllStudents() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getAllStudents()
            result.onSuccess { studentList ->
                _originalStudents.postValue(studentList)
                _filteredStudents.postValue(studentList) // Isi kedua list saat pertama kali fetch
            }.onFailure { exception ->
                _errorMessage.postValue("Gagal memuat data: ${exception.message}")
            }
            _isLoading.postValue(false)
        }
    }

    // ===============================================================
    // FUNGSI BARU UNTUK SEARCH
    // ===============================================================
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

    // ===============================================================
    // FUNGSI BARU UNTUK MEMANGGIL REPOSITORY UPDATE
    // ===============================================================
    fun updateStudent(student: Student) {
        viewModelScope.launch {
            val result = repository.updateStudent(student)
            _updateStatus.postValue(result)
        }
    }

    // ===============================================================
    // FUNGSI BARU UNTUK MEMANGGIL REPOSITORY DELETE
    // ===============================================================
    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            val result = repository.deleteStudent(studentId)
            _deleteStatus.postValue(result)
        }
    }

    // ===============================================================
    // FUNGSI BARU UNTUK MEMANGGIL REPOSITORY UPDATE SESI
    // ===============================================================
    fun updateStudentSessions(studentId: String, newSessionCount: Int) {
        viewModelScope.launch {
            val result = repository.updateStudentSessions(studentId, newSessionCount)
            // Kita bisa gunakan _updateStatus yang sudah ada untuk memberitahu UI
            _updateStatus.postValue(result)
        }
    }
}