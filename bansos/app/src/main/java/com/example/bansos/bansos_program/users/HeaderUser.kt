package com.example.bansos.bansos_program.users


import SessionManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import androidx.fragment.app.Fragment
import com.example.bansos.R
import com.example.bansos.bansos_program.Login
import com.example.bansos.bansos_program.RetrofitClient
import com.example.bansos.bansos_program.User
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class HeaderUser : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.header_user)

        // Mengakses TextView dengan findViewById
        val headerTitle: TextView = findViewById(R.id.header_title)

        // Mengakses session menggunakan SessionManager
        val sessionManager = SessionManager(this)

        // Mendapatkan session pengguna
        val userSession = sessionManager.getUserSession()

        // Mendapatkan username dari session
        val username = userSession["username"] ?: "Pengguna"  // Default jika username null
        val nik = userSession["nik"] ?: "Null"  // Default jika username null

        // Mengatur teks pada headerTitle dengan username
        headerTitle.text = "Selamat datang, $username"

        val buttonProfile: Button = findViewById(R.id.btn_profile)
        buttonProfile.setOnClickListener {
            showProfileSettingsDialog(this,username,nik)
        }
        val buttonBeranda: Button = findViewById(R.id.button_beranda)
        buttonBeranda.setOnClickListener {
            loadFragment(BerandaUserFragment())
        }

        // Mengakses Button dan menetapkan listener
        val buttonBpnt: Button = findViewById(R.id.button_bpnt)
        buttonBpnt.setOnClickListener {
            loadFragment(BPNTUserFragment())
        }

        val buttonDisabilitas: Button = findViewById(R.id.button_disabilitas)
        buttonDisabilitas.setOnClickListener {
            loadFragment(DisabilitasUserFragment())
        }

        val buttonAnakYatimPiatu: Button = findViewById(R.id.button_anak_yatim)
        buttonAnakYatimPiatu.setOnClickListener {
            loadFragment(AnakYatimPiatuUserFragment())
        }



        val btnLogout = findViewById<Button>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            logout()  // Memanggil fungsi logout saat tombol diklik
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.form_container_user, fragment)
            .commit()
    }

    private fun showProfileSettingsDialog(context: Context, usernameSession: String, nikSession: String) {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.profil_user, null)

        val nikErrorText: TextView = dialogView.findViewById(R.id.nik_error_text)
        val passwordLamaErrorText: TextView = dialogView.findViewById(R.id.password_lama_error_text)
        val passwordErrorText: TextView = dialogView.findViewById(R.id.password_error_text)
        val confirmPasswordErrorText: TextView = dialogView.findViewById(R.id.confirm_password_error_text)

        val submitButton: Button = dialogView.findViewById(R.id.submit_button)
        val closeButton: Button = dialogView.findViewById(R.id.close_button)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val username: EditText = dialogView.findViewById(R.id.username)
        username.setText(usernameSession)
        username.setTextColor(Color.BLACK)

        val nikInput: EditText = dialogView.findViewById(R.id.nik)
        nikInput.setText(nikSession)
        nikInput.setTextColor(Color.BLACK)

        val oldPasswordInput: EditText = dialogView.findViewById(R.id.old_password)
        val newPasswordInput: EditText = dialogView.findViewById(R.id.new_password)
        val confirmPasswordInput: EditText = dialogView.findViewById(R.id.confirm_password)

        // Validasi NIK secara real-time
        nikInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val nik = s.toString().trim()
                if (nik.length != 16 || !nik.matches(Regex("\\d{16}"))) {
                    nikErrorText.text = "NIK harus terdiri dari 16 angka"
                    nikErrorText.setTextColor(Color.RED)
                } else {
                    nikErrorText.text = ""
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Validasi password baru secara real-time
        newPasswordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (!isValidPassword(password)) {
                    passwordErrorText.text = "Password baru harus minimal 8 karakter dan mengandung simbol"
                    passwordErrorText.setTextColor(Color.RED)
                } else {
                    passwordErrorText.text = ""
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Validasi konfirmasi password secara real-time
        confirmPasswordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (newPasswordInput.text.toString() != s.toString()) {
                    confirmPasswordErrorText.text = "Password baru dan konfirmasi tidak cocok"
                    confirmPasswordErrorText.setTextColor(Color.RED)
                } else {
                    confirmPasswordErrorText.text = ""
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Tombol Simpan
        submitButton.setOnClickListener {
            val oldPassword = oldPasswordInput.text.toString()
            val newPassword = newPasswordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()
            val nik = nikInput.text.toString().trim()

            var isValid = true


            // Validasi NIK
            if (nik.isEmpty() || nik.length != 16 || !nik.matches(Regex("\\d{16}"))) {
                nikErrorText.text = "NIK harus terdiri dari 16 angka"
                nikErrorText.setTextColor(Color.RED)
                isValid = false
            } else {
                nikErrorText.text = ""
            }

            // Validasi password lama jika ada perubahan password
            if (oldPassword.isNotEmpty()) {
                if (newPassword.isEmpty() || confirmPassword.isEmpty() || newPassword != confirmPassword || !isValidPassword(newPassword)) {
                    passwordErrorText.text = "Password baru harus valid dan cocok dengan konfirmasi"
                    passwordErrorText.setTextColor(Color.RED)
                    isValid = false
                } else {
                    passwordErrorText.text = ""
                }

                // Validasi password lama
                validateOldPassword(oldPassword, context) { isOldPasswordValid ->
                    (context as? Activity)?.runOnUiThread {
                        // Periksa apakah password lama valid dan tidak kosong
                        if (isOldPasswordValid && isValid && oldPassword.isNotBlank() && oldPassword.length >= 6) {
                            // Password lama valid, jadi hapus pesan error

                            // Lanjutkan dengan update profil
                            val sessionManager = SessionManager(context)

                            // Menghapus session NIK lama dan menyimpan yang baru
                            sessionManager.clearAndUpdateNik(nik)

                            // Memanggil fungsi untuk memperbarui NIK ke server
                            updateNik()

                            // Update profil jika diperlukan
                            updateProfile(nik, newPassword, context)

                            // Perbarui session setelah update NIK
                            sessionManager.getUserSession()

                            // Reload SharedPreferences
                            reloadSharedPreferences()

                            // Tutup dialog dan restart aktivitas
                            dialog.dismiss()
                            context.recreate()  // Memaksa aktivitas untuk dimulai ulang
                        } else {
                            // Password lama tidak valid atau kosong, tampilkan pesan error
                            passwordLamaErrorText.text = "Password lama tidak valid"
                            passwordLamaErrorText.setTextColor(Color.RED)

                            // Menampilkan pesan selama 3 detik, kemudian menghapusnya
                            Handler(Looper.getMainLooper()).postDelayed({
                                passwordLamaErrorText.text = ""  // Menghapus pesan error setelah 3 detik
                            }, 3000)  // Delay 3000 ms (3 detik)
                        }
                    }
                }
            } else {
                // Jika password lama tidak diisi, hanya update NIK tanpa perubahan password
                (context as? Activity)?.runOnUiThread {
                    // Update NIK tanpa perlu password lama
                    val sessionManager = SessionManager(context)

                    // Menghapus session NIK lama dan menyimpan yang baru
                    sessionManager.clearAndUpdateNik(nik)

                    // Memanggil fungsi untuk memperbarui NIK ke server
                    updateNik()

                    // Update profil jika diperlukan
                    updateProfile(nik, "", context)

                    // Perbarui session setelah update NIK
                    sessionManager.getUserSession()

                    // Reload SharedPreferences
                    reloadSharedPreferences()

                    // Tutup dialog dan restart aktivitas
                    dialog.dismiss()
                    context.recreate()  // Memaksa aktivitas untuk dimulai ulang
                }
            }
        }

        // Tombol Tutup
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun validateOldPassword(oldPassword: String, context: Context, callback: (Boolean) -> Unit) {
        val client = OkHttpClient()

        // Mendapatkan username dari session
        val sessionManager = SessionManager(context)
        val userSession = sessionManager.getUserSession()
        val usernameSession = userSession["username"] ?: "Pengguna"  // Default jika username null

        // Membuat JSON body dengan username dan password lama
        val jsonBody = JSONObject()
        jsonBody.put("username", usernameSession)
        jsonBody.put("old_password", oldPassword)

        val body = jsonBody.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("http://192.168.46.100:5000/validate_old_password_mobile") // Ganti dengan URL endpoint Anda
            .post(body)
            .addHeader("Content-Type", "application/json")  // Menambahkan header Content-Type
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(false)  // Jika gagal, anggap password tidak valid
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody)
                    val isValid = jsonResponse.getBoolean("valid")
                    callback(isValid)  // Panggil callback dengan hasil validasi
                } else {
                    callback(false)  // Jika ada error, anggap password tidak valid
                }
            }
        })
    }

    fun updateProfile(nik: String, password: String, context: Context) {
        val client = OkHttpClient()

        // Mendapatkan username dari session
        val sessionManager = SessionManager(context)
        val userSession = sessionManager.getUserSession()
        val usernameSession = userSession["username"] ?: "Pengguna"  // Default jika username null

        // Membuat JSON body dengan username, nik, dan password
        val jsonBody = JSONObject()
        jsonBody.put("username", usernameSession)  // Menambahkan username ke dalam JSON
        jsonBody.put("nik", nik)
        jsonBody.put("password", password)

        val body = jsonBody.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${RetrofitClient.BASE_URL}update_profile_mobile") // Ganti dengan URL endpoint Anda
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody)
                    val message = jsonResponse.getString("message")
                }
            }
        })
    }

    fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,}$".toRegex()
        return passwordRegex.matches(password)
    }



    private fun updateNik() {
        val sessionManager = SessionManager(this)
        val userSession = sessionManager.getUserSession()

        // Periksa apakah pengguna telah login
        if (!sessionManager.isUserLoggedIn()) {
            Toast.makeText(this, "Harap login terlebih dahulu.", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil username dari sesi
        val username = userSession["username"]
        if (username.isNullOrEmpty()) {
            Toast.makeText(this, "Username tidak ditemukan di sesi.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("UpdateNik", "Mengirim permintaan untuk mendapatkan NIK terbaru dengan username: $username")

        // Memanggil fungsi untuk mendapatkan NIK terbaru
        getUpdatedNikFromServer(username) { success, message, updatedNik ->
            if (success) {
                // Ambil data user dari sesi lama dan perbarui NIK
                val updatedUser = userSession["username"]?.let { username ->
                    userSession["role"]?.let { role ->
                        User(
                            username = username,
                            nik = updatedNik,
                            role = role
                        )
                    }
                }

                if (updatedUser != null) {
                    // Simpan kembali sesi dengan NIK terbaru
                    sessionManager.saveUserSession(message, updatedUser)
                    Log.d("UpdateNik", "Session berhasil diperbarui: ${sessionManager.getUserSession()}")

                    Toast.makeText(this, "NIK berhasil diperbarui.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("UpdateNik", "Data user session tidak valid. Tidak dapat memperbarui NIK.")
                    Toast.makeText(this, "Terjadi kesalahan dalam memperbarui sesi.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("UpdateNik", "Gagal memperbarui NIK: $message")
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getUpdatedNikFromServer(username: String, callback: (Boolean, String, String) -> Unit) {
        Thread {
            try {
                // URL endpoint untuk mendapatkan NIK terbaru
                val url = URL("http://192.168.46.100:5000/get_updated_nik_mobile?username=$username")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.doInput = true

                val responseCode = connection.responseCode
                val response = connection.inputStream.bufferedReader().use { it.readText() }

                Log.d("UpdateNikResponse", "Response Code: $responseCode")
                Log.d("UpdateNikResponse", "Response: $response")

                runOnUiThread {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        try {
                            val jsonResponse = JSONObject(response)
                            val success = jsonResponse.getBoolean("success")
                            val message = jsonResponse.getString("message")
                            val updatedNik = jsonResponse.optString("nik", "")

                            Log.d("UpdateNikResponse", "Success: $success")
                            Log.d("UpdateNikResponse", "Message: $message")
                            Log.d("UpdateNikResponse", "Updated NIK: $updatedNik")

                            // Memanggil callback dengan hasil
                            callback(success, message, updatedNik)
                        } catch (e: Exception) {
                            Log.e("UpdateNik", "Error parsing response: ${e.message}")
                            callback(false, "Terjadi kesalahan dalam memproses data dari server.", "")
                        }
                    } else {
                        Log.e("UpdateNik", "Gagal dengan kode respons: $responseCode")
                        callback(false, "Terjadi kesalahan pada server. Silakan coba lagi.", "")
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("UpdateNik", "Error: ${e.message}")
                runOnUiThread {
                    callback(false, "Terjadi kesalahan jaringan: ${e.message}", "")
                }
            }
        }.start()
    }


    private fun reloadSharedPreferences() {
        val sessionManager = SessionManager(this)
        val userSession = sessionManager.getUserSession()
        // Reload SharedPreferences, pastikan data session baru digunakan
        val nikFromSession = userSession["nik"]
        val usernameFromSession = userSession["username"]
        Log.d("ReloadSharedPreferences", "Username: $usernameFromSession, NIK: $nikFromSession")

    }


    // Fungsi logout
    // Fungsi logout
    fun logout() {
        // Hapus session pengguna
        val sessionManager = SessionManager(this)
        sessionManager.clearSession()

        // Arahkan kembali ke halaman login
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()  // Menghentikan aktivitas saat ini agar tidak bisa kembali
    }


}
