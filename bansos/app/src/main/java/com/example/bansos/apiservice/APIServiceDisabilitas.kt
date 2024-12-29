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


interface APIServiceDisabilitas {

    // Menambahkan metode GET untuk mengambil data BPNT berdasarkan NIK
    @GET("/disabilitasmobile")
    fun getDisabilitasData(
        @Query("nik") nik: String
    ): Call<ApiResponseDisabilitas>

    @Multipart
    @POST("/disabilitaspostmobile")
    fun submitDisabilitasForm(
        @Part("nama_lengkap") namaLengkap: RequestBody,
        @Part("alamat_text") alamatText: RequestBody,
        @Part("nik") nik: RequestBody,
        @Part("no_kk") noKk: RequestBody,
        @Part("keterangan_disabilitas") keteranganDisabilitas: RequestBody?, // Field relevan
        @Part("tingkat_disabilitas") tingkatDisabilitas: RequestBody?, // Field relevan
        @Part("username") username: RequestBody, // Menambahkan parameter username
        @Part("provinsi") provinsi: RequestBody, // Menambahkan parameter provinsi
        @Part("kabupaten") kabupaten: RequestBody, // Menambahkan parameter kabupaten
        @Part("kapanewon") kapanewon: RequestBody, // Menambahkan parameter kapanewon
        @Part("kalurahan") kalurahan: RequestBody, // Menambahkan parameter kalurahan
        @Part ktpImage: MultipartBody.Part,
        @Part kkImage: MultipartBody.Part
    ): Call<ApiResponseDisabilitas>

    @Multipart
    @PUT("/disabilitasupdatemobile")
    fun updateDisabilitasForm(
        @Part("nama_lengkap") namaLengkap: RequestBody,
        @Part("alamat_text") alamatText: RequestBody,
        @Part("nik") nik: RequestBody,
        @Part("no_kk") noKk: RequestBody,
        @Part("keterangan_disabilitas") keteranganDisabilitas: RequestBody?, // Field relevan
        @Part("tingkat_disabilitas") tingkatDisabilitas: RequestBody?, // Field relevan
        @Part("username") username: RequestBody, // Menambahkan parameter username
        @Part("provinsi") provinsi: RequestBody, // Menambahkan parameter provinsi
        @Part("kabupaten") kabupaten: RequestBody, // Menambahkan parameter kabupaten
        @Part("kapanewon") kapanewon: RequestBody, // Menambahkan parameter kapanewon
        @Part("kalurahan") kalurahan: RequestBody, // Menambahkan parameter kalurahan
        @Part ktpImage: MultipartBody.Part,
        @Part kkImage: MultipartBody.Part
    ): Call<ApiResponseDisabilitas>


    @DELETE("/disabilitasdeletemobile")
    fun deleteImage(
        @Query("username") username: String?,  // Menggunakan query parameter untuk username
        @Query("filePath") filePath: String   // Menggunakan query parameter untuk filePath
    ): Call<ResponseBody>
}


data class ApiResponseDisabilitas(
    val message: String,
    val data: Disabilitas
)

data class Disabilitas(
    val nama_lengkap: String,
    val alamat: String,
    val provinsi: String,  // Menambahkan tipe data untuk provinsi
    val kabupaten: String, // Menambahkan tipe data untuk kabupaten
    val kapanewon: String, // Menambahkan tipe data untuk kapanewon
    val kalurahan: String, // Menambahkan tipe data untuk kalurahan
    val no_kk: String,
    val nik: String,
    val keterangan_disabilitas: String?,
    val tingkat_disabilitas: String?,
    var ktp_image_path: String?,
    var kk_image_path: String?,
    val username: String?,
    val status: String?,
    val komentar: String?
)
