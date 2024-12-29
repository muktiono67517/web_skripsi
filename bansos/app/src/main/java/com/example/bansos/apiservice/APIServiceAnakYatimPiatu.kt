import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query


// Interface APIService untuk Anak Yatim Piatu
interface APIServiceAnakYatimPiatu {

    // Menambahkan metode GET untuk mengambil data BPNT berdasarkan NIK
    @GET("/anakyatimpiatumobile")
    fun getAnakYatimPiatuData(
        @Query("nik") nik: String
    ): Call<ApiResponseAnakYatimPiatu>

    @Multipart
    @POST("/anakyatimpiatupostmobile")
    fun submitAnakYatimPiatuForm(
        @Part("nama_anak") namaAnak: RequestBody,
        @Part("alamat_text") alamatText: RequestBody,
        @Part("provinsi") provinsi: RequestBody,
        @Part("kabupaten") kabupaten: RequestBody,
        @Part("kapanewon") kapanewon: RequestBody,
        @Part("kalurahan") kalurahan: RequestBody,
        @Part("nik") nik: RequestBody,
        @Part("no_kk") noKk: RequestBody,
        @Part("kondisi_orangtua") kondisiOrangtua: RequestBody?,
        @Part kkImage: MultipartBody.Part, // Hapus nama parameter "kk_image_path" di sini
        @Part ktpImage: MultipartBody.Part, // Hapus nama parameter "ktp_wali_penanggungjawab_image_path" di sini
        @Part("tempat_tanggal_lahir") tempatTanggalLahir: RequestBody?,
        @Part("pendidikan_sekarang") pendidikanSekarang: RequestBody?,
        @Part("username") username: RequestBody
    ): Call<ApiResponseAnakYatimPiatu>


    @Multipart
    @PUT("/anakyatimpiatuupdatemobile")
    fun updateAnakYatimPiatuForm(
        @Part("nama_anak") namaAnak: RequestBody,
        @Part("alamat_text") alamatText: RequestBody,
        @Part("provinsi") provinsi: RequestBody, // Menambahkan parameter provinsi
        @Part("kabupaten") kabupaten: RequestBody, // Menambahkan parameter kabupaten
        @Part("kapanewon") kapanewon: RequestBody, // Menambahkan parameter kapanewon
        @Part("kalurahan") kalurahan: RequestBody, // Menambahkan parameter kalurahan
        @Part("nik") nik: RequestBody,
        @Part("no_kk") noKk: RequestBody,
        @Part("kondisi_orangtua") kondisiOrangtua: RequestBody?,
        @Part kkImage: MultipartBody.Part,
        @Part ktpImage: MultipartBody.Part,
        @Part("tempat_tanggal_lahir") tempatTanggalLahir: RequestBody?,
        @Part("pendidikan_sekarang") pendidikanSekarang: RequestBody?,
        @Part("username") username: RequestBody,
    ): Call<ApiResponseAnakYatimPiatu>

    @DELETE("/anakyatimpiatudeletemobile")
    fun deleteAnakYatimPiatuImage(
        @Query("username") username: String?,  // Menggunakan query parameter untuk username
        @Query("filePath") filePath: String   // Menggunakan query parameter untuk filePath
    ): Call<ResponseBody>
}


// Data model API Response dan Anak Yatim Piatu
data class ApiResponseAnakYatimPiatu(
    val message: String,
    val data: AnakYatimPiatu
)

data class AnakYatimPiatu(
    val nama_anak: String,
    val alamat: String,
    val provinsi: String,  // Menambahkan tipe data untuk provinsi
    val kabupaten: String, // Menambahkan tipe data untuk kabupaten
    val kapanewon: String, // Menambahkan tipe data untuk kapanewon
    val kalurahan: String, // Menambahkan tipe data untuk kalurahan
    val no_kk: String,
    val nik: String,
    val kondisi_orangtua: String?, // Kondisi orangtua
    val tanggal_usulan: String?,  // Tanggal usulan
    val id_wilayah_kalurahan: Int, // ID wilayah kalurahan
    val kk_image_path: String?,  // Path image untuk KK
    val ktp_wali_penanggungjawab_image_path: String?, // Path image untuk KTP Wali
    val tempat_tanggal_lahir: String?, // Tempat dan tanggal lahir
    val umur: String?, // Umur
    val pendidikan_sekarang: String?, // Pendidikan saat ini
    val username: String?,
    val tahun: Int?, // Tahun
    val status: String?,
    val komentar: String?
)