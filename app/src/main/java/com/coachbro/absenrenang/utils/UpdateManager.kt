package com.coachbro.absenrenang.utils

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.coachbro.absenrenang.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object UpdateManager {

    private const val UPDATE_URL = "https://raw.githubusercontent.com/fairuzulum/absensi-renang-update/main/version.json"
    private var downloadId: Long = -1
    private var progressDialog: AlertDialog? = null
    private var appContext: Context? = null

    fun checkUpdate(context: Context) {
        this.appContext = context.applicationContext
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val finalUrl = "$UPDATE_URL?t=${System.currentTimeMillis()}"
                val response = fetchUpdateInfo(finalUrl)
                if (response != null) {
                    val json = JSONObject(response)
                    val remoteVersionCode = json.getInt("versionCode")
                    val remoteVersionName = json.getString("versionName")
                    val changelog = json.getString("changelog")
                    val apkUrl = json.getString("apkUrl")

                    val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
                    } else {
                        @Suppress("DEPRECATION")
                        context.packageManager.getPackageInfo(context.packageName, 0).versionCode
                    }

                    if (remoteVersionCode > currentVersionCode) {
                        withContext(Dispatchers.Main) {
                            showUpdateDialog(context, remoteVersionName, changelog, apkUrl)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateManager", "Error checking update: ${e.message}")
            }
        }
    }

    private fun fetchUpdateInfo(urlStr: String): String? {
        return try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.useCaches = false
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun showUpdateDialog(context: Context, versionName: String, changelog: String, apkUrl: String) {
        MaterialAlertDialogBuilder(context, R.style.Theme_App_AlertDialog)
            .setTitle("Update Tersedia (v$versionName)")
            .setMessage("Apa yang baru:\n$changelog\n\nApakah Anda ingin mengupdate sekarang?")
            .setPositiveButton("Update Sekarang") { _, _ ->
                downloadAndInstall(context, apkUrl)
            }
            .setNegativeButton("Nanti Saja", null)
            .setCancelable(false)
            .show()
    }

    @SuppressLint("Range")
    private fun downloadAndInstall(context: Context, url: String) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_download_progress, null)
        val tvPercent = view.findViewById<TextView>(R.id.tvProgressPercent)
        val progressIndicator = view.findViewById<LinearProgressIndicator>(R.id.progressIndicator)

        progressDialog = MaterialAlertDialogBuilder(context, R.style.Theme_App_AlertDialog)
            .setTitle("Mengunduh Update")
            .setView(view)
            .setCancelable(false)
            .create()
        
        progressDialog?.show()

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("FSS SwimTrack Update")
            .setDescription("Mengunduh versi terbaru...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "FSS_SwimTrack_Update.apk")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        
        // Remove old file from public location
        val oldFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "FSS_SwimTrack_Update.apk")
        if (oldFile.exists()) oldFile.delete()

        downloadId = downloadManager.enqueue(request)

        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.Main) {
            var downloading = true
            while (downloading) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    val bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    
                    if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                        downloading = false
                    }

                    if (bytesTotal > 0) {
                        val progress = (bytesDownloaded * 100L / bytesTotal).toInt()
                        progressIndicator.progress = progress
                        tvPercent.text = String.format("%d%%", progress)
                    }
                }
                cursor?.close()
                delay(500)
            }
        }

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadId == id) {
                    progressDialog?.dismiss()
                    showSuccessDialog(context) // Use original activity context for dialog
                    try {
                        context.unregisterReceiver(this)
                    } catch (e: Exception) {}
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun showSuccessDialog(context: Context) {
        MaterialAlertDialogBuilder(context, R.style.Theme_App_AlertDialog)
            .setTitle("Unduhan Selesai")
            .setMessage("Versi terbaru telah berhasil diunduh ke folder 'Download' HP Anda. Silakan buka folder tersebut dan klik file 'FSS_SwimTrack_Update.apk' untuk menginstal.")
            .setPositiveButton("Buka Folder Download") { _, _ ->
                try {
                    val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal membuka folder Download", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Tutup", null)
            .setCancelable(false)
            .show()
    }
}