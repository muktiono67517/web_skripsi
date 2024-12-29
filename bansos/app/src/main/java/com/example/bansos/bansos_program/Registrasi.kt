package com.example.bansos.bansos_program
import APIServiceRegister
import ApiResponseRegister
import SessionManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bansos.R
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@Suppress("Deprecation")
class Registrasi : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registrasi)

        sessionManager = SessionManager(this)

        // Inisialisasi ID elemen-elemen dari XML
        val inputNIK = findViewById<EditText>(R.id.inputNIK)
        val inputEmail = findViewById<EditText>(R.id.inputEmail)
        val inputUsername = findViewById<EditText>(R.id.inputUsername)
        val inputPassword = findViewById<EditText>(R.id.inputPassword)
        val inputConfirmPassword = findViewById<EditText>(R.id.inputConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val loginLink = findViewById<TextView>(R.id.login_link)
        val lupaPasswordLink = findViewById<TextView>(R.id.lupa_password_link)

        // Set onClickListener untuk tombol daftar
        btnRegister.setOnClickListener {
            val nik = inputNIK.text.toString()
            val email = inputEmail.text.toString()
            val username = inputUsername.text.toString()
            val password = inputPassword.text.toString()
            val confirmPassword = inputConfirmPassword.text.toString()

            // Validasi input sebelum mengirim
            if (isValidInput(nik, email, username, password, confirmPassword)) {
                registerUser(nik, email, username, password, confirmPassword)
            }
        }

        // Set onClickListener untuk link login
        loginLink.setOnClickListener {
            // Navigasi ke halaman login
            val intent = Intent(this@Registrasi, Login::class.java)
            startActivity(intent)
        }

        // Set onClickListener untuk link lupa password
        lupaPasswordLink.setOnClickListener {
            // Navigasi ke halaman lupa password
            val intent = Intent(this@Registrasi, LupaPassword::class.java)
            startActivity(intent)
        }
    }
    private fun registerUser(nik: String, email: String, username: String, password: String, confirmPassword: String) {
        // Memanggil API untuk registrasi dengan mengirimkan data langsung sebagai parameter
        val call = RetrofitClient.apiServiceRegister.registerUser(
            username, password, nik, email
        )

        call.enqueue(object : Callback<ApiResponseRegister> {
            override fun onResponse(call: Call<ApiResponseRegister>, response: Response<ApiResponseRegister>) {
                if (response.isSuccessful) {
                    val registerResponse = response.body()

                    registerResponse?.let {
                        if (it.success) {
                            // Jika registrasi berhasil, navigasikan ke halaman login
                            val intent = Intent(this@Registrasi, Login::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Menampilkan pesan error jika registrasi gagal
                            displayErrorMessages(it.errorMessage)
                            Toast.makeText(applicationContext, "Error: ${it.errorMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Tangani kesalahan jika respons tidak sukses
                    val errorMessage = "Terjadi kesalahan pada server. Silakan coba lagi."
                    Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponseRegister>, t: Throwable) {
                // Tangani error jaringan atau kesalahan lainnya
                Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun isValidInput(nik: String, email: String, username: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        // Validasi NIK (16 digit)
        if (nik.isEmpty() || nik.length != 16) {
            Toast.makeText(this, "NIK harus 16 digit", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Validasi Email (format email valid)
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email tidak valid", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Validasi Username
        if (username.isEmpty()) {
            Toast.makeText(this, "Username harus diisi", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Validasi Password (minimal 8 karakter dan mengandung simbol)
        if (password.isEmpty() || password.length < 8 || !password.matches(".*[!@#\$%^&*(),.?\":{}|<>].*".toRegex())) {
            Toast.makeText(this, "Password harus minimal 8 karakter dan mengandung setidaknya 1 simbol", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Validasi Konfirmasi Password
        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Konfirmasi Password harus diisi", Toast.LENGTH_SHORT).show()
            isValid = false
        } else if (password != confirmPassword) {
            Toast.makeText(this, "Password dan Konfirmasi Password tidak cocok", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }


    private fun displayErrorMessages(errorMessages: String) {
        // Menampilkan pesan error hanya jika errorMessages tidak kosong
        if (!errorMessages.isNullOrEmpty()) {
            Toast.makeText(this, errorMessages, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Terjadi kesalahan, silakan coba lagi.", Toast.LENGTH_SHORT).show()
        }
    }

}

