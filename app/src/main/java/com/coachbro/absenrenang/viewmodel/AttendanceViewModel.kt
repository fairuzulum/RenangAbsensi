// viewmodel/AttendanceViewModel.kt
package com.coachbro.absenrenang.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.data.repository.StudentRepository
import kotlinx.coroutines.launch

class AttendanceViewModel : ViewModel() {

    private val repository = StudentRepository()

    // Menyimpan daftar asli siswa dari Firestore
    private val _originalStudents = MutableLiveData<List<Student>>()

    // Menyimpan daftar siswa yang sudah difilter (untuk ditampilkan di UI)
    private val _filteredStudents = MutableLiveData<List<Student>>()
    val filteredStudents: LiveData<List<Student>> = _filteredStudents

    // LiveData untuk state loading dan pesan error
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    // Blok init dipanggil saat ViewModel pertama kali dibuat
    init {
        fetchAllStudents()
    }

    // Fungsi untuk mengambil semua data siswa dari repository
    fun fetchAllStudents() {
        isLoading.value = true
        viewModelScope.launch {
            repository.getAllStudents().onSuccess { studentList ->
                // Jika sukses, simpan di daftar asli dan juga tampilkan di daftar filter
                _originalStudents.postValue(studentList)
                _filteredStudents.postValue(studentList)
            }.onFailure { exception ->
                errorMessage.postValue("Gagal memuat data: ${exception.message}")
            }
            isLoading.postValue(false)
        }
    }

    // Fungsi untuk memfilter daftar siswa berdasarkan input pencarian
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