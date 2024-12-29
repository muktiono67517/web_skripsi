package com.example.bansos.bansos_program

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bansos.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class LupaPassword : AppCompatActivity() {

    private lateinit var inputNIK: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputUsername: EditText
    private lateinit var btnResetPassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lupa_password)

        // Mengaitkan EditText dengan ID yang ada di layout XML
        inputNIK = findViewById(R.id.inputNIK)
        inputEmail = findViewById(R.id.inputEmail)
        inputUsername = findViewById(R.id.inputUsername)
        btnResetPassword = findViewById(R.id.btnResetPassword)

        btnResetPassword.setOnClickListener {
            // Mengambil data dari EditText ketika tombol diklik
            val nik = inputNIK.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val username = inputUsername.text.toString().trim()

            // Debug Log untuk memeriksa nilai yang diambil
            Log.d("verifyData", "nik: $nik, email: $email, username: $username")

            // Memastikan data tidak kosong
            if (nik.isEmpty() || email.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Semua data harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Panggil verifyData dengan parameter yang diambil dari EditText
            verifyData(nik, email, username, this) { isValid ->
                if (isValid) {
                    // Data valid, lanjutkan dengan proses selanjutnya
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this, "Data valid. Proses dapat dilanjutkan.", Toast.LENGTH_SHORT).show()
//                        sendNewPassword()
                    }
                } else {
                    // Data tidak valid
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this, "Data tidak valid. Periksa kembali informasi yang dimasukkan.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun verifyData(nik: String, email: String, username: String, context: Context, callback: (Boolean) -> Unit) {
        val client = OkHttpClient()

        // Membuat JSON body dengan nik, email, username
        val jsonBody = JSONObject().apply {
            put("nik", nik)
            put("email", email)
            put("username", username)
        }

        Log.d("verifyData", "JSON body yang dikirim: $jsonBody")  // Log JSON body yang akan dikirim

        val body = jsonBody.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${RetrofitClient.BASE_URL}verifyusermobile")  // Ganti dengan URL endpoint backend Anda
            .post(body)
            .addHeader("Content-Type", "application/json")  // Menambahkan header Content-Type
            .build()

        Log.d("verifyData", "Request dibuat dengan URL: http://192.168.46.100:5000/verifyusermobile") // Log URL request

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("verifyData", "Request gagal: ${e.message}")  // Log error jika gagal
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Terjadi kesalahan, coba lagi.", Toast.LENGTH_SHORT).show()
                }
                callback(false)  // Jika gagal, anggap data tidak valid
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("verifyData", "Response diterima: ${response.message}")  // Log respons dari server

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("verifyData", "Response body: $responseBody")  // Log body respons

                    val jsonResponse = JSONObject(responseBody)
                    val isValid = jsonResponse.getBoolean("success")  // Sesuaikan dengan respons JSON dari backend

                    Log.d("verifyData", "Data valid: $isValid")  // Log hasil validasi

                    // Menampilkan Toast di thread utama
                    Handler(Looper.getMainLooper()).post {
                        if (isValid) {
                            Log.d("verifyData", "Menampilkan Toast: Data valid")
                            Toast.makeText(context, "Data valid", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("verifyData", "Menampilkan Toast: Data tidak valid")
                            Toast.makeText(context, "Data tidak valid", Toast.LENGTH_SHORT).show()
                        }
                    }

                    callback(isValid)  // Panggil callback dengan hasil validasi
                } else {
                    Log.e("verifyData", "Error: ${response.message}")  // Log error jika status response bukan 200
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Terjadi kesalahan, coba lagi.", Toast.LENGTH_SHORT).show()
                    }
                    callback(false)  // Jika ada error, anggap data tidak valid
                }
            }
        })
    }
    fun sendNewPassword(password: String, username: String, email: String, nik: String) {
        val client = OkHttpClient()

        // Membuat JSON body
        val jsonBody = JSONObject().apply {
            put("password", password)
            put("username", username)
            put("email", email)
            put("nik", nik)
        }

        val body = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        // Membuat request untuk memperbarui password di server
        val request = Request.Builder()
            .url("http://192.168.45.100:5000/sendnewpassword") // Ganti dengan URL server Anda
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("sendNewPassword", "Request gagal: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("sendNewPassword", "Password berhasil diperbarui di server: $responseBody")

                    // Kirim email setelah server berhasil memperbarui password
                    sendEmail(email, username, password)
                } else {
                    Log.e("sendNewPassword", "Error saat memperbarui password: ${response.message}")
                }
            }
        })
    }

    // Fungsi untuk mengirim email menggunakan SMTP Outlook
    fun sendEmail(recipientEmail: String, username: String, newPassword: String) {
        Thread {
            try {
                val properties = Properties().apply {
                    put("mail.smtp.host", "smtp.office365.com")
                    put("mail.smtp.port", "587")
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                }

                val session = Session.getInstance(properties, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication("tugasakhir366@outlook.com", "TugasAkhir7642763!!")
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress("your_outlook_email@outlook.com"))
                    setRecipients(
                        Message.RecipientType.TO,
                        InternetAddress.parse(recipientEmail)
                    )
                    subject = "Password Baru Anda"
                    setText(
                        """
                    Halo, $username
                    
                    Password Anda telah diperbarui. Berikut password baru Anda:
                    $newPassword
                    
                    Harap segera login dan ganti password Anda demi keamanan.
                    
                    Salam,
                    Tim Dukungan
                    """.trimIndent()
                    )
                }

                Transport.send(message)
                Log.d("sendEmail", "Email berhasil dikirim ke $recipientEmail")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("sendEmail", "Gagal mengirim email: ${e.message}")
            }
        }.start()
    }

}
