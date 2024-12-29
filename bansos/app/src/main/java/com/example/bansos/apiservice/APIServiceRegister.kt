import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface APIServiceRegister {

    // Endpoint untuk registrasi pengguna baru
    @FormUrlEncoded
    @POST("/registerpostmobile")
    fun registerUser(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("nik") nik: String,
        @Field("email") email: String
    ): Call<ApiResponseRegister>

}

// Response untuk registrasi pengguna
data class ApiResponseRegister(
    val errorMessage: String, // Pesan dari server
    val success: Boolean, // Status keberhasilan
    val user: User? // Data pengguna jika berhasil
)

// Data pengguna
data class User(
    val nik: String,
    val email: String,
    val username: String
)
