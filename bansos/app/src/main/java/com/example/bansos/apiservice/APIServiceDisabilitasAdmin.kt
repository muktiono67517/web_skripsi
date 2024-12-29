import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface APIServiceDisabilitasAdmin{

    // Menambahkan metode GET untuk mengambil data BPNT berdasarkan NIK
    @GET("/disabilitasadminmobile/{kalurahan}")
    fun getdisabititasDataAdmin(
        @Path("kalurahan")kalurahan: String
    ): Call<ApiResponseDisabilitasAdmin>

    // Update status menggunakan PUT request
    @PUT("/disabilitasadminmobile/{username}/status")
    fun updateStatus(
        @Path("username") username: String,
        @Body komentar: RequestBody  // Menggunakan @Body untuk mengirim data teks (komentar)
    ): Call<ApiResponseDisabilitasAdmin>

    // Update komentar menggunakan PUT request
    @PUT("/bpntadminmobile/{username}/komentar")
    fun updateKomentar(
        @Path("username") username: String,
        @Body komentar: RequestBody  // Menggunakan @Body untuk mengirim data teks (komentar)
    ): Call<ApiResponseDisabilitasAdmin>
}

/// Response API untuk BPNT Admin
data class ApiResponseDisabilitasAdmin(
    val message: String,
    val data: List<DisabilitasDataAdmin> //
)

// Data class untuk BPNT Admin
data class DisabilitasDataAdmin(
    val nama_lengkap: String,              // Nama Lengkap
    val alamat: String,                    // Alamat
    val no_kk: String,                     // Nomor KK
    val nik: String,                       // NIK
    val keterangan_disabilitas: String?,   // Keterangan Disabilitas (nullable)
    val tingkat_disabilitas: String?,      // Tingkat Disabilitas (nullable)
    val tanggal_usulan: String?,           // Tanggal Usulan (nullable)
    var ktp_image_path: String?,           // Path Gambar KTP (nullable)
    var kk_image_path: String?,            // Path Gambar Kartu Keluarga (nullable)
    val username: String?,                 // Username (nullable)
    val tahun: String?,                    // Tahun (nullable)
    val status: String?,                   // Status (nullable)
    val komentar: String?                  // Komentar (nullable)
)

