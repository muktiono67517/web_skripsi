import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.bansos.bansos_program.User

class SessionManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)

    // Fungsi untuk menyimpan session pengguna
    fun saveUserSession(message: String, user: User?) {
        val editor = sharedPreferences.edit()
        editor.putString("message", message)
        user?.let {
            editor.putString("username", it.username)
            editor.putString("nik", it.nik)
            editor.putString("role", it.role.toString()) // Simpan role_id asli (1 atau 2)
            Log.d("UserSession", "Saved: username=${it.username}, nik=${it.nik}, role_id=${it.role}")
        }
        editor.apply()

        // Debugging untuk mencetak semua data yang disimpan
        Log.d("SessionData", "Saved Data: message=$message, username=${user?.username}, nik=${user?.nik}, role_id=${user?.role}")
    }


    // Fungsi untuk mendapatkan session pengguna
    fun getUserSession(): Map<String, String?> {
        val user = mutableMapOf<String, String?>()
        user["message"] = sharedPreferences.getString("message", null)
        user["username"] = sharedPreferences.getString("username", null)
        user["nik"] = sharedPreferences.getString("nik", null)
        user["role"] = sharedPreferences.getString("role", null)

        // Log data sesi pengguna
        Log.d("UserSession", "Sesi Pengguna: $user")

        return user
    }


    // Fungsi untuk menghapus session pengguna
    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.clear()  // Menghapus semua data dalam session
        editor.apply()
    }

    fun clearAndUpdateNik(newNik: String) {
        val editor = sharedPreferences.edit()

//         Hapus hanya NIK yang ada di session
        editor.remove("nik")  // Menghapus data NIK lama

        // Simpan NIK baru
        editor.putString("nik", newNik)  // Menyimpan NIK baru

        // Debug: Menampilkan nilai terbaru dari session
        val updatedNik = sharedPreferences.getString("nik", "NIK tidak ada")
        Log.d("ClearAndUpdateNik", "NIK setelah diperbarui: $updatedNik")

        // Menampilkan seluruh isi SharedPreferences untuk debugging
        val allPreferences = sharedPreferences.all
        Log.d("ClearAndUpdateNik", "Isi session setelah update: $allPreferences")
        editor.apply()
    }

    // Fungsi untuk mengecek apakah pengguna sudah login
    fun isUserLoggedIn(): Boolean {
        val username = sharedPreferences.getString("username", null)
        return username != null  // Jika username ada, artinya user sudah login
    }
}
