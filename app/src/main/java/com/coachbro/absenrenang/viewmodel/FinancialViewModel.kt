// viewmodel/FinancialViewModel.kt
package com.coachbro.absenrenang.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachbro.absenrenang.data.model.FinancialReport
import com.coachbro.absenrenang.data.repository.StudentRepository
import kotlinx.coroutines.launch

class FinancialViewModel : ViewModel() {

    private val repository = StudentRepository()

    private val _reportData = MutableLiveData<List<FinancialReport>>()
    val reportData: LiveData<List<FinancialReport>> = _reportData

    private val _totalRevenue = MutableLiveData<Long>()
    val totalRevenue: LiveData<Long> = _totalRevenue

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchFinancialReport() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getFinancialReport().onSuccess { reports ->
                _reportData.postValue(reports)
                // Hitung total pemasukan dari semua laporan
                _totalRevenue.postValue(reports.sumOf { it.totalAmount })
            }.onFailure {
                // Handle error
            }
            _isLoading.postValue(false)
        }
    }
}