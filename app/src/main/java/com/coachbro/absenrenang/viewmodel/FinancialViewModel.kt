package com.coachbro.absenrenang.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachbro.absenrenang.data.model.FinancialReport
import com.coachbro.absenrenang.data.repository.StudentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    // Job untuk menangani pembatalan pencarian (Debounce)
    private var searchJob: Job? = null

    fun fetchFinancialReport() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getFinancialReport().onSuccess { reports ->
                // Simpan ke master data
                fullReportList = reports

                // Tampilkan data awal (Diurutkan dari yang terbaru, opsional)
                _reportData.postValue(reports)

                // Hitung total pemasukan dari semua laporan
                _totalRevenue.postValue(reports.sumOf { it.amount })
            }.onFailure {
                // Handle error
            }
            _isLoading.postValue(false)
        }
    }

    // ===============================================================
    // FITUR ANTI-LAG (DEBOUNCE SEARCH)
    // ===============================================================
    fun filterReports(query: String) {
        // 1. Batalkan pencarian sebelumnya jika user masih mengetik
        searchJob?.cancel()

        // 2. Mulai pencarian baru
        searchJob = viewModelScope.launch(Dispatchers.Default) {
            // 3. TUNGGU SEBENTAR (300ms). Jika user mengetik lagi dalam waktu ini,
            // proses ini akan dibatalkan di langkah 1, jadi tidak membebani HP.
            delay(300)

            val currentList = fullReportList

            if (query.isEmpty()) {
                _reportData.postValue(currentList)
            } else {
                // 4. Filter data
                val filtered = currentList.filter {
                    it.studentName.contains(query, ignoreCase = true)
                }

                // [OPSIONAL TAPI DISARANKAN]
                // Jika hasil pencarian > 100, ambil 100 teratas saja biar HP tidak berat merender UI
                // Hapus '.take(100)' jika ingin menampilkan semuanya.
                _reportData.postValue(filtered)
            }
        }
    }
}