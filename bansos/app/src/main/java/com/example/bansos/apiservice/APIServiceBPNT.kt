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


interface APIServiceBPNT {

   // Menambahkan metode GET untuk mengambil data BPNT berdasarkan NIK
    @GET("/bpntmobile")
    fun getBpntData(
        @Query("nik") nik: String
    ): Call<ApiResponseBPNT>

    @Multipart
    @POST("/bpntpostmobile")
    fun submitBpntForm(
        @Part("nama_lengkap") namaLengkap: RequestBody,
        @Part("alamat_text") alamatText: RequestBody,
        @Part("nik") nik: RequestBody,
        @Part("no_kk") noKk: RequestBody,
        @Part("kebutuhan_pangan") kebutuhanPangan: RequestBody?,
        @Part("alasan_usulan") alasanUsulan: RequestBody?,
        @Part("username") username: RequestBody, // Menambahkan parameter username
        @Part("provinsi") provinsi: RequestBody, // Menambahkan parameter provinsi
        @Part("kabupaten") kabupaten: RequestBody, // Menambahkan parameter kabupaten
        @Part("kapanewon") kapanewon: RequestBody, // Menambahkan parameter kapanewon
        @Part("kalurahan") kalurahan: RequestBody, // Menambahkan parameter kalurahan
        @Part rumahTampakDepanImage: MultipartBody.Part,
        @Part rumahTampakDalamImage: MultipartBody.Part,
        @Part rumahTampakBelakangImage: MultipartBody.Part,
        @Part ktpImage: MultipartBody.Part,
        @Part kkImage: MultipartBody.Part
    ): Call<ApiResponseBPNT>



    @Multipart
    @PUT("/bpntupdatemobile")
    fun UpdateBPNTForm(
        @Part("nama_lengkap") namaLengkap: RequestBody,
        @Part("alamat_text") alamatText: RequestBody,
        @Part("nik") nik: RequestBody,
        @Part("no_kk") noKk: RequestBody,
        @Part("kebutuhan_pangan") kebutuhanPangan: RequestBody?,
        @Part("alasan_usulan") alasanUsulan: RequestBody?,
        @Part("username") username: RequestBody, // Menambahkan parameter username
        @Part("provinsi") provinsi: RequestBody, // Menambahkan parameter provinsi
        @Part("kabupaten") kabupaten: RequestBody, // Menambahkan parameter kabupaten
        @Part("kapanewon") kapanewon: RequestBody, // Menambahkan parameter kapanewon
        @Part("kalurahan") kalurahan: RequestBody, // Menambahkan parameter kalurahan
        @Part rumahTampakDepanImage: MultipartBody.Part,
        @Part rumahTampakDalamImage: MultipartBody.Part,
        @Part rumahTampakBelakangImage: MultipartBody.Part,
        @Part ktpImage: MultipartBody.Part,
        @Part kkImage: MultipartBody.Part
    ): Call<ApiResponseBPNT>

    @DELETE("/bpntdeletemobile")
    fun deleteImage(
        @Query("username") username: String?,  // Menggunakan query parameter untuk username
        @Query("filePath") filePath: String   // Menggunakan query parameter untuk filePath
    ): Call<ResponseBody>

}


data class ApiResponseBPNT(
    val message: String,
    val data: BpntData
)

data class BpntData(
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
    var rumah_tampak_depan_image_path: String?,
    var rumah_tampak_dalam_image_path: String?,
    var rumah_tampak_belakang_image_path: String?,
    var ktp_image_path: String?,
    var kk_image_path: String?,
    val username: String?,
    val status:String?,
    val komentar:String?
)

