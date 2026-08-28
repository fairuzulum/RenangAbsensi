package com.coachbro.absenrenang.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coachbro.absenrenang.R
import com.coachbro.absenrenang.data.model.NotificationSettings
import com.coachbro.absenrenang.data.repository.StudentRepository
import com.coachbro.absenrenang.databinding.ActivityNotificationManagementBinding
import com.coachbro.absenrenang.utils.TelegramService
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class NotificationManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationManagementBinding
    private val repository = StudentRepository()
    private var currentSettings = NotificationSettings()
    private lateinit var idAdapter: TelegramIdAdapter
    private var botUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadSettings()

        // Auto-show guide on first enter (per session)
        if (savedInstanceState == null) {
            showGuideDialog()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupListeners() {
        binding.btnShowGuide.setOnClickListener { showGuideDialog() }

        binding.btnAddRecipient.setOnClickListener {
            showSetupWizardStep1()
        }
    }

    private fun showGuideDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_telegram_guide, null)
        dialog.setContentView(view)

        view.findViewById<View>(R.id.btnOpenUserInfoBot).setOnClickListener {
            openTelegramUrl("https://t.me/id_bot")
        }

        val btnOpenOurBot = view.findViewById<MaterialButton>(R.id.btnOpenOurBot)
        if (botUsername != null) {
            btnOpenOurBot.setOnClickListener {
                openTelegramUrl("https://t.me/$botUsername")
            }
        } else {
            btnOpenOurBot.isEnabled = false
            btnOpenOurBot.alpha = 0.5f
            btnOpenOurBot.text = "Bot Belum Terkoneksi"
        }

        view.findViewById<View>(R.id.btnCloseGuide).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // --- WIZARD FLOW ---

    private fun showSetupWizardStep1() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_setup_step_1, null)
        dialog.setContentView(view)

        view.findViewById<View>(R.id.btnGoToTelegram).setOnClickListener {
            openTelegramUrl("https://t.me/id_bot")
        }

        view.findViewById<View>(R.id.btnNextStep).setOnClickListener {
            dialog.dismiss()
            showAddIdDialog()
        }
        dialog.show()
    }

    private fun showAddIdDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_telegram_id, null)
        dialog.setContentView(view)

        val etChatId = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etChatId)
        val btnAdd = view.findViewById<View>(R.id.btnAdd)
        val btnCancel = view.findViewById<View>(R.id.btnCancel)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnAdd.setOnClickListener {
            val id = etChatId.text.toString().trim()
            if (id.isNotEmpty()) {
                val newList = currentSettings.recipientIds.toMutableList()
                if (!newList.contains(id)) {
                    newList.add(id)
                    saveSettings(currentSettings.copy(recipientIds = newList))
                    dialog.dismiss()
                    showSetupWizardStep3() // Go to last step
                } else {
                    Toast.makeText(this, "ID sudah terdaftar", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun showSetupWizardStep3() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_setup_step_3, null)
        dialog.setContentView(view)

        val btnActivateBot = view.findViewById<MaterialButton>(R.id.btnActivateBot)
        if (botUsername != null) {
            btnActivateBot.setOnClickListener {
                openTelegramUrl("https://t.me/$botUsername")
            }
        } else {
            btnActivateBot.isEnabled = false
            btnActivateBot.text = "Klik START di Bot Coach"
        }

        view.findViewById<View>(R.id.btnDone).setOnClickListener { 
            dialog.dismiss()
            Toast.makeText(this, "Silakan klik tombol TEST pada ID baru Anda", Toast.LENGTH_LONG).show()
        }
        dialog.show()
    }

    // --- HELPER METHODS ---

    private fun openTelegramUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Aplikasi Telegram tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        idAdapter = TelegramIdAdapter(
            onTest = { chatId -> testId(chatId) },
            onDelete = { chatId -> removeId(chatId) }
        )
        binding.rvRecipientIds.apply {
            layoutManager = LinearLayoutManager(this@NotificationManagementActivity)
            adapter = idAdapter
        }
    }

    private fun loadSettings() {
        setLoading(true)
        lifecycleScope.launch {
            repository.getNotificationSettings().onSuccess { settings ->
                currentSettings = settings
                idAdapter.submitList(settings.recipientIds)
                TelegramService.getBotInfo(settings.botToken).onSuccess { botUsername = it }
            }.onFailure {
                Toast.makeText(this@NotificationManagementActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
            setLoading(false)
        }
    }

    private fun saveSettings(newSettings: NotificationSettings) {
        setLoading(true)
        lifecycleScope.launch {
            repository.updateNotificationSettings(newSettings).onSuccess {
                currentSettings = newSettings
                idAdapter.submitList(newSettings.recipientIds)
            }.onFailure {
                Toast.makeText(this@NotificationManagementActivity, "Gagal menyimpan", Toast.LENGTH_SHORT).show()
            }
            setLoading(false)
        }
    }

    private fun removeId(chatId: String) {
        val newList = currentSettings.recipientIds.toMutableList()
        newList.remove(chatId)
        saveSettings(currentSettings.copy(recipientIds = newList))
    }

    private fun testId(chatId: String) {
        Toast.makeText(this, "Sedang mencoba mengirim pesan test...", Toast.LENGTH_SHORT).show()
        setLoading(true)
        lifecycleScope.launch {
            val result = TelegramService.sendMessage(
                botToken = currentSettings.botToken,
                chatId = chatId,
                message = "✅ <b>KONEKSI TEST BERHASIL</b>\n\nID: <code>$chatId</code>\nBot ini sekarang sudah bisa mengirimkan notifikasi absensi ke akun Anda."
            )
            result.onSuccess {
                Toast.makeText(this@NotificationManagementActivity, "Pesan test BERHASIL terkirim!", Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(this@NotificationManagementActivity, "GAGAL: ${it.message}", Toast.LENGTH_LONG).show()
            }
            setLoading(false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingLayout.root.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}

class TelegramIdAdapter(
    private val onTest: (String) -> Unit,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<TelegramIdAdapter.ViewHolder>() {
    private var list = listOf<String>()
    fun submitList(newList: List<String>) {
        list = newList
        notifyDataSetChanged()
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvChatId: TextView = view.findViewById(R.id.tvChatId)
        val btnTest: View = view.findViewById(R.id.btnTest)
        val btnDelete: View = view.findViewById(R.id.btnDelete)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_telegram_id, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvChatId.text = item
        holder.btnTest.setOnClickListener { onTest(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }
    override fun getItemCount() = list.size
}