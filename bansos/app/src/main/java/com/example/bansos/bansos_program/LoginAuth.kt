package com.example.bansos.bansos_program

data class LoginAuth(
    val success: Boolean,   // Status login berhasil atau tidak
    val message: String,    // Pesan dari server (misalnya, login berhasil atau gagal)
    val user: User? = null  // Data pengguna (username, nik, role) jika login berhasil
)

data class User(
    val username: String,   // Username pengguna
    val nik: String,        // NIK pengguna
    val role: String   // Role pengguna (admin/user)
)
