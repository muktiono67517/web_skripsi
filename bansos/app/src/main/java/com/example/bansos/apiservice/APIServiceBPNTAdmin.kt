import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface APIServiceBPNTAdmin {

    // Menambahkan metode GET untuk mengambil data BPNT berdasarkan NIK
    @GET("/bpntadminmobile/{kalurahan}")
    fun getbpntDataAdmin(
        @Path("kalurahan")kalurahan: String
    ): Call<ApiResponseBPNTAdmin>

    // Update status menggunakan PUT request
    @PUT("/bpntadminmobile/{username}/status")
    fun updateStatus(
        @Path("username") username: String,
        @Body komentar: RequestBody  // Menggunakan @Body untuk mengirim data teks (komentar)
    ): Call<ApiResponseBPNTAdmin>

    // Update komentar menggunakan PUT request
    @PUT("/bpntadminmobile/{username}/komentar")
    fun updateKomentar(
        @Path("username") username: String,
        @Body komentar: RequestBody  // Menggunakan @Body untuk mengirim data teks (komentar)
    ): Call<ApiResponseBPNTAdmin>
}

/// Response API untuk BPNT Admin
data class ApiResponseBPNTAdmin(
    val message: String,
    val data: List<BpntDataAdmin> // Ganti BpntDataAdmin menjadi List untuk menangani array
)

// Data class untuk BPNT Admin
data class BpntDataAdmin(
    val nama_lengkap: String,
    val alamat: String,
    val provinsi: String,  // Tambahkan tipe data untuk provinsi
    val kabupaten: String, // Tambahkan tipe data untuk kabupaten
    val kapanewon: String, // Tambahkan tipe data untuk kapanewon
    val kalurahan: String, // Tambahkan tipe data untuk kalurahan
    val no_kk: String,
    val nik: String,
    val kebutuhan_pangan: String?,
    val alasan_usulan: String?,
    val tanggal_usulan: String?,
    var rumah_tampak_depan_image_path: String?,
    var rumah_tampak_dalam_image_path: String?,
    var rumah_tampak_belakang_image_path: String?,
    var ktp_image_path: String?,
    var kk_image_path: String?,
    val username: String?,
    val status: String?,
    val komentar: String?
)
