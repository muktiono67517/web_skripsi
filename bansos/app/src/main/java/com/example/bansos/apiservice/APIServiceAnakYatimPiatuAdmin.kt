import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface APIServiceAnakYatimPiatuAdmin {

    // Menambahkan metode GET untuk mengambil data BPNT berdasarkan NIK
    @GET("/anakyatimpiatuadminmobile/{kalurahan}")
    fun getanakyatimpiatuDataAdmin(
        @Path("kalurahan")kalurahan: String
    ): Call<ApiResponseAnakYatimPiatuAdmin>

    // Update status menggunakan PUT request
    @PUT("/anakyatimpiatuadminmobile/{username}/status")
    fun updateStatus(
        @Path("username") username: String,
        @Body komentar: RequestBody  // Menggunakan @Body untuk mengirim data teks (komentar)
    ): Call<ApiResponseAnakYatimPiatuAdmin>

    // Update komentar menggunakan PUT request
    @PUT("/anakyatimpiatuadminmobile/{username}/komentar")
    fun updateKomentar(
        @Path("username") username: String,
        @Body komentar: RequestBody  // Menggunakan @Body untuk mengirim data teks (komentar)
    ): Call<ApiResponseAnakYatimPiatuAdmin>
}

/// Response API untuk BPNT Admin
data class ApiResponseAnakYatimPiatuAdmin(
    val message: String,
    val data: List<AnakYatimPiatuDataAdmin> // Ganti BpntDataAdmin menjadi List untuk menangani array
)

// Data class untuk BPNT Admin
data class AnakYatimPiatuDataAdmin(
    val nama_anak: String,
    val alamat: String,
    val no_kk: String,
    val nik: String,
    val kondisi_orangtua: String,
    val tanggal_usulan: String,
    val kk_image_path: String,
    val ktp_wali_penanggungjawab_image_path: String,
    val tempat_tanggal_lahir: String,
    val umur: String,
    val pendidikan_sekarang: String,
    val status: String?,
    val komentar: String?,
    val username: String,
)
