package com.coachbro.absenrenang.data.model

import com.google.firebase.firestore.PropertyName

// Anotasi @PropertyName memastikan mapping dari Firestore ke properti Kotlin berjalan benar.
data class MenuSetting(
    @get:PropertyName("isEnabled") @set:PropertyName("isEnabled") var isEnabled: Boolean = false,
    @get:PropertyName("pin") @set:PropertyName("pin") var pin: String = ""
)

data class MenuPasswords(
    @get:PropertyName("absensi") @set:PropertyName("absensi") var absensi: MenuSetting = MenuSetting(),
    @get:PropertyName("keuangan") @set:PropertyName("keuangan") var keuangan: MenuSetting = MenuSetting(),
    @get:PropertyName("listSiswa") @set:PropertyName("listSiswa") var listSiswa: MenuSetting = MenuSetting(),
    @get:PropertyName("pembayaran") @set:PropertyName("pembayaran") var pembayaran: MenuSetting = MenuSetting(),
    @get:PropertyName("register") @set:PropertyName("register") var register: MenuSetting = MenuSetting(),
    @get:PropertyName("laporan") @set:PropertyName("laporan") var laporan: MenuSetting = MenuSetting()
)