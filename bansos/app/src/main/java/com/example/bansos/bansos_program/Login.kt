package com.example.bansos.bansos_program



import SessionManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bansos.R
import com.example.bansos.bansos_program.admin.HeaderAdmin


import com.example.bansos.bansos_program.users.HeaderUser


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Inisialisasi SessionManager
        sessionManager = SessionManager(this)

        // Cek apakah pengguna sudah login
        if (sessionManager.isUserLoggedIn()) {
            // Ambil data session
            val userSession = sessionManager.getUserSession()
            val userRole = userSession["role"] ?: "user" // Ambil role dari session

            // Tentukan halaman tujuan berdasarkan role
            val targetActivity = if (userRole == "admin") {
                HeaderAdmin::class.java // Halaman Admin
            } else {
                HeaderUser::class.java // Halaman User
            }

            // Arahkan ke halaman yang sesuai
            val intent = Intent(this@Login, targetActivity)
            startActivity(intent)
            finish() // Hentikan aktivitas login untuk mencegah kembali ke halaman login
        }

        val progressBar = findViewById<ProgressBar>(R.id.progress_Bar_Spinning_Login)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            val username = findViewById<EditText>(R.id.inputUsername).text.toString()
            val password = findViewById<EditText>(R.id.inputPassword).text.toString()

            // Memanggil fungsi login
            login(username, password)

            // Simulasi proses loading
            Handler(Looper.getMainLooper()).postDelayed({
                // Menyembunyikan ProgressBar setelah loading selesai
                progressBar.visibility = View.GONE

                // Aksi setelah loading selesai
            }, 3000) // Menunggu selama 3 detik untuk simulasi loading
        }

        // Menambahkan fungsi register dan lupa password
        register()
        lupaPassword()
    }

    private fun login(username: String, password: String) {
        // Cek apakah user sudah login sebelumnya
        if (sessionManager.isUserLoggedIn()) {
            // Jika sudah login, ambil data session dan navigasi ke halaman sesuai role
            val userSession = sessionManager.getUserSession()
            val userRoleId = userSession["role_id"]?.toString() ?: "2" // Default ke "2" (user)

            // Tentukan halaman yang akan dituju berdasarkan role_id
            val targetActivity = if (userRoleId == "1") {
                HeaderAdmin::class.java // Halaman Admin
            } else {
                HeaderUser::class.java // Halaman User
            }

            // Navigasi ke halaman sesuai role
            val intent = Intent(this@Login, targetActivity)
            startActivity(intent)
            finish() // Hentikan aktivitas login setelah navigasi
            return
        }

        // Jika belum login, lanjutkan dengan login API
        val call = RetrofitClient.apiServiceLogin.login(username, password)
        Log.d("Login", "Login attempt with username: $username")

        call.enqueue(object : Callback<LoginAuth> {
            override fun onResponse(call: Call<LoginAuth>, response: Response<LoginAuth>) {
                runOnUiThread {
                    // Cek apakah response sukses
                    if (response.isSuccessful) {
                        val loginAuth = response.body()
                        Log.d("Login", "Response body: $loginAuth")

                        loginAuth?.let {
                            if (it.success) {
                                // Jika login berhasil, simpan data session
                                Log.d("Login", "Login successful. Saving session.")
                                sessionManager.saveUserSession(it.message, it.user)

                                // Navigasi ke halaman sesuai role user
                                when (it.user?.role) {
                                    "admin" -> {
                                        Log.d("Login", "Navigating to Admin screen")
                                        val intent = Intent(this@Login, HeaderAdmin::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    "user"-> {
                                        Log.d("Login", "Navigating to User screen")
                                        val intent = Intent(this@Login, HeaderUser::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    else -> {
                                        Log.e("Login", "Unknown role: ${it.user?.role}")
                                        Toast.makeText(this@Login, "Role tidak valid.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                // Jika login gagal
                                Log.d("Login", "Login failed: ${it.message}")
                                Toast.makeText(applicationContext, it.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // Jika response gagal
                        Log.e("Login", "Response failed. Code: ${response.code()}")
                        val errorMessage = when (response.code()) {
                            401 -> "Username atau password salah."
                            500 -> "Terjadi kesalahan pada server."
                            else -> "Login gagal. Silakan coba lagi."
                        }
                        Toast.makeText(this@Login, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<LoginAuth>, t: Throwable) {
                runOnUiThread {
                    // Jika request API gagal
                    Log.e("Login", "onFailure: ", t)
                    Toast.makeText(this@Login, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }




    private fun register() {
        val textViewRegister = findViewById<TextView>(R.id.registrasi_text)
        textViewRegister.setOnClickListener {
            val intent = Intent(this@Login, Registrasi::class.java)
            startActivity(intent)
        }
    }

    private fun lupaPassword() {
        val textViewPassword = findViewById<TextView>(R.id.lupa_password_text)
        textViewPassword.setOnClickListener {
            val intent = Intent(this@Login, LupaPassword::class.java)
            startActivity(intent)
        }
    }







}
