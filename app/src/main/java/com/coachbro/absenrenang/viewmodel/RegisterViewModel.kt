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

    // LiveData untuk status registrasi
    // Private MutableLiveData agar hanya bisa diubah dari dalam ViewModel
    private val _registrationStatus = MutableLiveData<Result<Unit>>()
    // Public LiveData agar bisa diobservasi dari luar (oleh Activity)
    val registrationStatus: LiveData<Result<Unit>> = _registrationStatus

    fun registerStudent(name: String, ageStr: String, parentName: String, parentPhone: String) {
        // Validasi dasar, nama tidak boleh kosong
        if (name.isBlank()) {
            _registrationStatus.value = Result.failure(Exception("Nama siswa tidak boleh kosong"))
            return
        }

        // Mengubah string umur menjadi Int, atau null jika kosong
        val age = ageStr.toIntOrNull()

        val student = Student(
            name = name,
            age = age,
            parentName = parentName.ifBlank { null }, // jadi null jika kosong
            parentPhone = parentPhone.ifBlank { null } // jadi null jika kosong
        )

        // Menjalankan proses penyimpanan di background thread menggunakan Coroutines
        viewModelScope.launch {
            val result = repository.registerStudent(student)
            _registrationStatus.postValue(result) // Post value ke LiveData
        }
    }
}