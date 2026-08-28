package com.coachbro.absenrenang.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object TelegramService {

    suspend fun sendMessage(botToken: String, chatId: String, message: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val encodedMessage = URLEncoder.encode(message, "UTF-8")
                val urlString = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage&parse_mode=HTML"
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("TelegramService", "Pesan berhasil terkirim ke Telegram (ID: $chatId)")
                    Result.success(Unit)
                } else {
                    val errorMsg = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
                    Log.e("TelegramService", "Gagal kirim pesan ke $chatId: $responseCode - $errorMsg")
                    Result.failure(Exception("Error $responseCode: $errorMsg"))
                }
            } catch (e: Exception) {
                Log.e("TelegramService", "Error Telegram (ID: $chatId): ${e.message}")
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }
    suspend fun getBotInfo(botToken: String): Result<String> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("https://api.telegram.org/bot$botToken/getMe")
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    // Simple JSON parsing to get username
                    val username = response.substringAfter("\"username\":\"").substringBefore("\"")
                    Result.success(username)
                } else {
                    Result.failure(Exception("Error $responseCode"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }
}