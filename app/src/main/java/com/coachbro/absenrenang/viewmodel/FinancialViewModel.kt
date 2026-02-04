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

    // Variabel untuk menyimpan list lengkap (master data)
    private var fullReportList: List<FinancialReport> = emptyList()

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
                // Simpan ke master data
                fullReportList = reports

                // Tampilkan data awal
                _reportData.postValue(reports)

                // Hitung total pemasukan dari semua laporan
                _totalRevenue.postValue(reports.sumOf{ it.amount })
            }.onFailure {
                // Handle error
            }
            _isLoading.postValue(false)
        }
    }

    // Fungsi untuk memfilter data berdasarkan pencarian
    fun filterReports(query: String) {
        if (query.isEmpty()) {
            _reportData.value = fullReportList
        } else {
            val filtered = fullReportList.filter {
                it.studentName.contains(query, ignoreCase = true)
            }
            _reportData.value = filtered
        }
    }
}