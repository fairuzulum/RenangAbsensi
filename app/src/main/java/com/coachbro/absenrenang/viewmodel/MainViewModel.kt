package com.coachbro.absenrenang.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachbro.absenrenang.data.model.MenuPasswords
import com.coachbro.absenrenang.data.repository.StudentRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = StudentRepository()

    private val _menuPasswords = MutableLiveData<MenuPasswords?>()
    val menuPasswords: LiveData<MenuPasswords?> = _menuPasswords

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        fetchMenuPasswords()
    }

    private fun fetchMenuPasswords() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getMenuPasswords().onSuccess {
                _menuPasswords.postValue(it)
            }.onFailure {
                _errorMessage.postValue("Gagal memuat pengaturan: ${it.message}")
                // Jika gagal, set ke null agar tidak mengunci menu secara tidak sengaja
                _menuPasswords.postValue(null)
            }
            _isLoading.postValue(false)
        }
    }
}