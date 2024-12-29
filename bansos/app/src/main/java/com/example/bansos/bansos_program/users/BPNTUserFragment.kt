package com.example.bansos.bansos_program.users


import ApiResponseBPNT
import SessionManager
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.ImageLoader
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.example.bansos.R
import com.example.bansos.bansos_program.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

@Suppress("Deprecation")
class BPNTUserFragment : Fragment() {
    private lateinit var SwipeRefreshLayoute:SwipeRefreshLayout
    private lateinit var teks_peringatan_mode_update:TextView
    private lateinit var namaLengkapInput: EditText
    private lateinit var alamatInput: EditText
    private lateinit var nikInput: EditText
    private lateinit var noKkInput: EditText
    private lateinit var kebutuhanpangan: EditText
    private lateinit var alasanusulan: EditText
    private lateinit var provinsiSpinner: Spinner
    private lateinit var kabupatenSpinner: Spinner
    private lateinit var kapanewonSpinner: Spinner
    private lateinit var kalurahanSpinner: Spinner
    private lateinit var fotoRumahTampakDepan: Button
    private lateinit var fotoRumahTampakBelakang: Button
    private lateinit var fotoRumahTampakDalam: Button
    private lateinit var fotoKk: Button
    private lateinit var fotoKtp: Button
    private lateinit var submitButton: Button
    private lateinit var updateButton: Button

    private lateinit var thumbRumahTampakDepan: ImageView
    private lateinit var thumbRumahTampakBelakang: ImageView
    private lateinit var thumbRumahTampakDalam: ImageView
    private lateinit var thumbKk: ImageView
    private lateinit var thumbKtp: ImageView
    private lateinit var deleteFotoRumahTampakDepan: ImageButton
    private lateinit var deleteFotoRumahTampakBelakang: ImageButton
    private lateinit var deleteFotoRumahTampakDalam: ImageButton
    private lateinit var deleteFotoKk: ImageButton
    private lateinit var deleteFotoKtp: ImageButton

    private  lateinit var statusTextView:TextView
    private  lateinit var komentarTextView:TextView




    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageType: String? = null

    private var fotoRumahTampakDepanUri: Uri? = null
    private var fotoRumahTampakBelakangUri: Uri? = null
    private var fotoRumahTampakDalamUri: Uri? = null
    private var fotoKkUri: Uri? = null
    private var fotoKtpUri: Uri? = null
    private val imageUrlsSimpan = mutableMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bpnt_user, container, false)

        // SwipeRefreshLayout
        val swipeRefreshLayout: SwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            // Refresh the data when swipe is detected
            fetchDataFromServer()
            swipeRefreshLayout.isRefreshing = false // stop refreshing
        }
        // Initialize UI elements
        initializeUIElements(view)
        // Check session and populate NIK if available
        populateSessionData()
        // Setup Spinners
        setupSpinner()


//
        // Handle submit button click
        submitButton.setOnClickListener {
            val spinnerValues = getSelectedSpinnerValues()
            if (spinnerValues != null && validateForm()) {
                submitDataToServer(spinnerValues)
                fetchDataFromServer()

            } else {
                Toast.makeText(requireContext(), "Formulir tidak lengkap atau spinner belum dipilih", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle submit button click
        updateButton.setOnClickListener {
            val spinnerValues = getSelectedSpinnerValues()
            if (spinnerValues != null && validateFormUpdate()) {
                updateDataToServer(spinnerValues)
                fetchDataFromServer()
            } else {
                Toast.makeText(requireContext(), "Formulir tidak lengkap", Toast.LENGTH_SHORT).show()
            }
        }
        // Setup image upload buttons
        setupImageUploadButtons()

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Call fetchDataFromServer() after the view is created and bound
        fetchDataFromServer()
    }


    private fun initializeUIElements(view: View) {
        // Input fields
        SwipeRefreshLayoute=view.findViewById(R.id.swipeRefreshLayout)
        teks_peringatan_mode_update=view.findViewById(R.id.teks_peringatan_mode_update)
        namaLengkapInput = view.findViewById(R.id.nama_lengkap)

        alamatInput = view.findViewById(R.id.alamat_text)
        alamatInput.apply {
            hint="Contoh:RT 01 RW 02 Anjir"
            setHintTextColor(Color.GRAY)

            // Memblokir input koma
            filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
                if (source.contains(",")) {
                    return@InputFilter ""  // Mengembalikan string kosong jika ada koma
                }
                null  // Jika tidak ada koma, biarkan input diterima
            })
        }

        nikInput = view.findViewById(R.id.nik_input)
        noKkInput = view.findViewById(R.id.no_kk_input)

        kebutuhanpangan = view.findViewById(R.id.kebutuhan_pangan_input)
        kebutuhanpangan.hint="Contoh:Beras,Minyak Goreng"
        kebutuhanpangan.setHintTextColor(Color.GRAY)

        alasanusulan = view.findViewById(R.id.alasan_usulan_input)
        alasanusulan.hint="Contoh:Penghasilan Tidak Mencukupi"
        kebutuhanpangan.setHintTextColor(Color.GRAY)

        provinsiSpinner = view.findViewById(R.id.provinsi_spinner)
        kabupatenSpinner = view.findViewById(R.id.kabupaten_spinner)
        kapanewonSpinner = view.findViewById(R.id.kapanewon_spinner)
        kalurahanSpinner = view.findViewById(R.id.kalurahan_spinner)
        fotoRumahTampakDepan = view.findViewById(R.id.upload_foto_rumah_tampak_depan)
        fotoRumahTampakBelakang = view.findViewById(R.id.upload_foto_rumah_tampak_belakang)
        fotoRumahTampakDalam = view.findViewById(R.id.upload_foto_rumah_tampak_dalam)
        fotoKk = view.findViewById(R.id.upload_foto_kk)
        fotoKtp = view.findViewById(R.id.upload_foto_ktp)
        submitButton = view.findViewById(R.id.submit_button)
        updateButton = view.findViewById(R.id.update_button)

        // Thumbnail ImageViews
        thumbRumahTampakDepan = view.findViewById(R.id.thumb_foto_rumah_tampak_depan)
        thumbRumahTampakBelakang = view.findViewById(R.id.thumb_foto_rumah_tampak_belakang)
        thumbRumahTampakDalam = view.findViewById(R.id.thumb_foto_rumah_tampak_dalam)
        thumbKk = view.findViewById(R.id.thumb_foto_kk)
        thumbKtp = view.findViewById(R.id.thumb_foto_ktp)

        // Delete Buttons (Tombol X)
        deleteFotoRumahTampakDepan = view.findViewById(R.id.delete_foto_rumah_tampak_depan)
        deleteFotoRumahTampakBelakang = view.findViewById(R.id.delete_foto_rumah_tampak_belakang)
        deleteFotoRumahTampakDalam = view.findViewById(R.id.delete_foto_rumah_tampak_dalam)
        deleteFotoKk = view.findViewById(R.id.delete_foto_kk)
        deleteFotoKtp = view.findViewById(R.id.delete_foto_ktp)
        statusTextView=view.findViewById(R.id.status_bantuan_pangan)
        komentarTextView=view.findViewById(R.id.pesan_admin_bantuan_pangan)


    }




    private fun fetchDataFromServer() {
        val sessionManager = SessionManager(requireContext())
        val nik = sessionManager.getUserSession()["nik"] ?: return
        Log.d("NIK ANDA SESI", nik)

        val call = RetrofitClient.apiServiceBPNT.getBpntData(nik)
        call.enqueue(object : Callback<ApiResponseBPNT> {
            override fun onResponse(call: Call<ApiResponseBPNT>, response: Response<ApiResponseBPNT>) {
                Log.d("onResponse", "Response received from server")

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()?.data
                    Log.d("Data Received", "Data from server: $data")

                    if (data != null) {
                        // Update EditText fields
                        Log.d("Updating EditTexts", "Setting EditText values")

                        namaLengkapInput.setText(data.nama_lengkap)
                        alamatInput.setText(data.alamat)
                        nikInput.setText(data.nik)
                        noKkInput.setText(data.no_kk)
                        kebutuhanpangan.setText(data.kebutuhan_pangan ?: "")
                        alasanusulan.setText(data.alasan_usulan ?: "")

                        // Update Spinner fields
                        Log.d("Updating Spinners", "Setting Spinner values")

// Provinsi Spinner
                        val provinsiAdapter = object : ArrayAdapter<String>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            listOf(data.provinsi)
                        ) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = super.getView(position, convertView, parent)
                                val textView = view.findViewById<TextView>(android.R.id.text1)
                                textView.setTextColor(Color.BLACK) // Set warna teks menjadi hitam
                                return view
                            }
                        }
                        provinsiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        provinsiSpinner.adapter = provinsiAdapter

// Kabupaten Spinner
                        val kabupatenAdapter = object : ArrayAdapter<String>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            listOf(data.kabupaten)
                        ) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = super.getView(position, convertView, parent)
                                val textView = view.findViewById<TextView>(android.R.id.text1)
                                textView.setTextColor(Color.BLACK) // Set warna teks menjadi hitam
                                return view
                            }
                        }
                        kabupatenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        kabupatenSpinner.adapter = kabupatenAdapter

// Kapanewon Spinner
                        val kapanewonAdapter = object : ArrayAdapter<String>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            listOf(data.kapanewon)
                        ) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = super.getView(position, convertView, parent)
                                val textView = view.findViewById<TextView>(android.R.id.text1)
                                textView.setTextColor(Color.BLACK) // Set warna teks menjadi hitam
                                return view
                            }
                        }
                        kapanewonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        kapanewonSpinner.adapter = kapanewonAdapter

// Kalurahan Spinner
                        val kalurahanList = arrayOf("Hargorejo", "Hargomulyo", "Hargotirto", "Hargowilis", "Kalirejo")
                        val kalurahanAdapter = object : ArrayAdapter<String>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            kalurahanList
                        ) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = super.getView(position, convertView, parent)
                                val textView = view.findViewById<TextView>(android.R.id.text1)
                                textView.setTextColor(Color.BLACK) // Set warna teks menjadi hitam
                                return view
                            }
                        }
                        kalurahanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        kalurahanSpinner.adapter = kalurahanAdapter

                        val serverValue = data.kalurahan // Misalnya: "Hargorejo"
                        val selectedIndex = kalurahanList.indexOf(serverValue)
                        if (selectedIndex >= 0) {
                            kalurahanSpinner.setSelection(selectedIndex) // Pilih nilai sesuai data server
                        } else {
                            // Jika nilai dari server tidak ditemukan dalam array
                            Toast.makeText(requireContext(), "Data kalurahan tidak valid: $serverValue", Toast.LENGTH_SHORT).show()
                        }



                        // Update Button actions
                        Log.d("Updating Buttons", "Checking image paths")
                        // Menonaktifkan tombol jika gambar tersedia
                        fotoRumahTampakDepan.text =
                            if (data.rumah_tampak_depan_image_path != null) {
                                fotoRumahTampakDepan.isEnabled = false
                                "Gambar Tersedia"
                            } else {
                                fotoRumahTampakDepan.isEnabled = true
                                "Upload Gambar"
                            }

                        fotoRumahTampakBelakang.text =
                            if (data.rumah_tampak_belakang_image_path != null) {
                                fotoRumahTampakBelakang.isEnabled = false
                                "Gambar Tersedia"
                            } else {
                                fotoRumahTampakBelakang.isEnabled = true
                                "Upload Gambar"
                            }

                        fotoRumahTampakDalam.text =
                            if (data.rumah_tampak_dalam_image_path != null) {
                                fotoRumahTampakDalam.isEnabled = false
                                "Gambar Tersedia"
                            } else {
                                fotoRumahTampakDalam.isEnabled = true
                                "Upload Gambar"
                            }

                        fotoKtp.text =
                            if (data.ktp_image_path != null) {
                                fotoKtp.isEnabled = false
                                "Gambar Tersedia"
                            } else {
                                fotoKtp.isEnabled = true
                                "Upload Gambar"
                            }

                        fotoKk.text =
                            if (data.kk_image_path != null) {
                                fotoKk.isEnabled = false
                                "Gambar Tersedia"
                            } else {
                                fotoKk.isEnabled = true
                                "Upload Gambar"
                            }


// Menampilkan tombol Update atau Simpan
                        // Menampilkan tombol Update atau Simpan berdasarkan data saja
                        if (data.nama_lengkap.isNotEmpty() && data.nik.isNotEmpty()) {
                            teks_peringatan_mode_update.text = "Perhatian: Harap isi semua data saat memperbarui. Data yang sudah Anda masukkan sebelumnya otomatis terisi. Ubah hanya bagian yang perlu diperbarui. "
                            // Menampilkan tombol Update dan menyembunyikan tombol Simpan
                            updateButton.visibility = View.VISIBLE
                            submitButton.visibility = View.GONE
                        } else {
                            // Jika data kosong, tampilkan tombol Simpan dan sembunyikan tombol Update
                            Log.d("Buttons Visibility", "Displaying Submit Button")
                            submitButton.visibility = View.VISIBLE
                            updateButton.visibility = View.GONE
                        }







                        // Fungsi untuk mengecek apakah session data tersedia atau tidak




                        val imageBaseUrl = "http://192.168.46.100:5000/users/bpnt/uploads/"

// Rumah Tampak Depan
// Rumah Tampak Depan
                        data.rumah_tampak_depan_image_path?.let {
                            val imageUrl = "$imageBaseUrl$it"
                            Glide.with(requireContext()).load(imageUrl).into(thumbRumahTampakDepan)

                            thumbRumahTampakDepan.setOnClickListener {
                                showImageDialog(imageUrl)
                            }

                            thumbRumahTampakDepan.setOnLongClickListener {
                                downloadImage(imageUrl)
                                true
                            }

                            deleteFotoRumahTampakDepan.setOnClickListener {
                                deleteImageBPNTAsinkronCall(thumbRumahTampakDepan, imageUrl, "rumah_tampak_depan")
                            }

                            // Menyimpan hanya path gambar (tanpa baseUrl)
                            imageUrlsSimpan["rumah_tampak_depan"] = it

                            // Menampilkan tombol delete jika ada gambar
                            deleteFotoRumahTampakDepan.visibility = View.VISIBLE
                        } ?: run {
                            // Menyembunyikan tombol delete jika tidak ada gambar
                            deleteFotoRumahTampakDepan.visibility = View.GONE
                        }

// Rumah Tampak Belakang
                        data.rumah_tampak_belakang_image_path?.let {
                            val imageUrl = "$imageBaseUrl$it"
                            Glide.with(requireContext()).load(imageUrl).into(thumbRumahTampakBelakang)

                            thumbRumahTampakBelakang.setOnClickListener {
                                showImageDialog(imageUrl)
                            }

                            thumbRumahTampakBelakang.setOnLongClickListener {
                                downloadImage(imageUrl)
                                true
                            }

                            deleteFotoRumahTampakBelakang.setOnClickListener {
                                deleteImageBPNTAsinkronCall(thumbRumahTampakBelakang, imageUrl, "rumah_tampak_belakang")
                            }

                            // Menyimpan hanya path gambar (tanpa baseUrl)
                            imageUrlsSimpan["rumah_tampak_belakang"] = it

                            // Menampilkan tombol delete jika ada gambar
                            deleteFotoRumahTampakBelakang.visibility = View.VISIBLE
                        } ?: run {
                            // Menyembunyikan tombol delete jika tidak ada gambar
                            deleteFotoRumahTampakBelakang.visibility = View.GONE
                        }

// Rumah Tampak Dalam
                        data.rumah_tampak_dalam_image_path?.let {
                            val imageUrl = "$imageBaseUrl$it"
                            Glide.with(requireContext()).load(imageUrl).into(thumbRumahTampakDalam)

                            thumbRumahTampakDalam.setOnClickListener {
                                showImageDialog(imageUrl)
                            }

                            thumbRumahTampakDalam.setOnLongClickListener {
                                downloadImage(imageUrl)
                                true
                            }

                            deleteFotoRumahTampakDalam.setOnClickListener {
                                deleteImageBPNTAsinkronCall(thumbRumahTampakDalam, imageUrl, "rumah_tampak_dalam")
                            }

                            // Menyimpan hanya path gambar (tanpa baseUrl)
                            imageUrlsSimpan["rumah_tampak_dalam"] = it

                            // Menampilkan tombol delete jika ada gambar
                            deleteFotoRumahTampakDalam.visibility = View.VISIBLE
                        } ?: run {
                            // Menyembunyikan tombol delete jika tidak ada gambar
                            deleteFotoRumahTampakDalam.visibility = View.GONE
                        }

// KTP
                        data.ktp_image_path?.let {
                            val imageUrl = "$imageBaseUrl$it"
                            Glide.with(requireContext()).load(imageUrl).into(thumbKtp)

                            thumbKtp.setOnClickListener {
                                showImageDialog(imageUrl)
                            }

                            thumbKtp.setOnLongClickListener {
                                downloadImage(imageUrl)
                                true
                            }

                            deleteFotoKtp.setOnClickListener {
                                deleteImageBPNTAsinkronCall(thumbKtp, imageUrl, "ktp")
                            }

                            // Menyimpan hanya path gambar (tanpa baseUrl)
                            imageUrlsSimpan["ktp"] = it

                            // Menampilkan tombol delete jika ada gambar
                            deleteFotoKtp.visibility = View.VISIBLE
                        } ?: run {
                            // Menyembunyikan tombol delete jika tidak ada gambar
                            deleteFotoKtp.visibility = View.GONE
                        }

// KK
                        data.kk_image_path?.let {
                            val imageUrl = "$imageBaseUrl$it"
                            Glide.with(requireContext()).load(imageUrl).into(thumbKk)

                            thumbKk.setOnClickListener {
                                showImageDialog(imageUrl)
                            }

                            thumbKk.setOnLongClickListener {
                                downloadImage(imageUrl)
                                true
                            }

                            deleteFotoKk.setOnClickListener {
                                deleteImageBPNTAsinkronCall(thumbKk, imageUrl, "kk")
                            }

                            // Menyimpan hanya path gambar (tanpa baseUrl)
                            imageUrlsSimpan["kk"] = it

                            // Menampilkan tombol delete jika ada gambar
                            deleteFotoKk.visibility = View.VISIBLE
                        } ?: run {
                            // Menyembunyikan tombol delete jika tidak ada gambar
                            deleteFotoKk.visibility = View.GONE
                        }


                        statusTextView.text = data.status
                        komentarTextView.text = data.komentar
                        Log.d("TAG", "Status: ${data.status}")
                        Log.d("TAG", "Komentar: ${data.komentar}")

// Mengubah warna teks menjadi hitam
                        statusTextView.setTextColor(Color.BLACK)
                        komentarTextView.setTextColor(Color.BLACK)


                    }
                } else {
                    Log.d("Response Error", "Failed to load data from server")
                    Toast.makeText(requireContext(), "", Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<ApiResponseBPNT
                    >, t: Throwable) {
                Log.e("onFailure", "Error occurred: ${t.message}")
                Toast.makeText(requireContext(), "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }











    private fun populateSessionData() {
        val sessionManager = SessionManager(requireContext())
        val userSession = sessionManager.getUserSession()

        val nikFromSession = userSession["nik"]
        if (nikFromSession.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Session tidak valid, silakan login kembali", Toast.LENGTH_SHORT).show()
        } else {
            nikInput.setText(nikFromSession)
        }
    }

    private fun setupImageUploadButtons() {
        fotoRumahTampakDepan.setOnClickListener { openGallery("depan") }
        fotoRumahTampakBelakang.setOnClickListener { openGallery("belakang") }
        fotoRumahTampakDalam.setOnClickListener { openGallery("dalam") }
        fotoKk.setOnClickListener { openGallery("kk") }
        fotoKtp.setOnClickListener { openGallery("ktp") }
    }




    private fun openGallery(imageType: String) {
        selectedImageType = imageType
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Cek jika requestCode dan resultCode sesuai dengan yang diharapkan
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            // Pastikan data yang dikembalikan tidak null
            val uri = data?.data
            uri?.let {
                // Tentukan aksi berdasarkan tipe gambar yang dipilih
                when (selectedImageType) {
                    "depan" -> {
                        fotoRumahTampakDepanUri = it
                        loadImageIntoView(it, thumbRumahTampakDepan)
                    }
                    "belakang" -> {
                        fotoRumahTampakBelakangUri = it
                        loadImageIntoView(it, thumbRumahTampakBelakang)
                    }
                    "dalam" -> {
                        fotoRumahTampakDalamUri = it
                        loadImageIntoView(it, thumbRumahTampakDalam)
                    }
                    "kk" -> {
                        fotoKkUri = it
                        loadImageIntoView(it, thumbKk)
                    }
                    "ktp" -> {
                        fotoKtpUri = it
                        loadImageIntoView(it, thumbKtp)
                    }
                    else -> {
                        // Handle case where no image type is selected
                    }
                }
            } ?: run {
                // Handle case where no image URI is returned
                Log.e("onActivityResult", "Failed to get image URI")
            }
        }
    }

    // Function to load image into ImageView using Glide
    private fun loadImageIntoView(uri: Uri, imageView: ImageView) {
        // Cek jika URI adalah URL dari server
        val imageUrl = uri.toString()

        // Jika ini adalah URL gambar dari server
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            Glide.with(requireContext())
                .load(imageUrl)  // URL gambar dari server
                .into(imageView)
        } else {
            // Jika ini adalah URI gambar lokal
            Glide.with(requireContext())
                .load(uri)  // URI lokal gambar
                .into(imageView)
        }
    }


    private fun setupSpinner() {
        val provinsiList = arrayOf("Daerah Istimewa Yogyakarta")
        val kabupatenList = arrayOf("Kulon Progo")
        val kapanewonList = arrayOf("Kokap")
        val kalurahanList = arrayOf("Hargorejo", "Hargomulyo", "Hargotirto", "Hargowilis", "Kalirejo")

        // Membuat adapter dan menambahkan padding pada setiap item spinner
        val provinsiAdapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, provinsiList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view as TextView
                // Menambahkan padding pada tampilan item dan mengubah warna teks menjadi hitam
                view.setPadding(20, 20, 20, 20) // Padding atas, kiri, kanan, bawah
                textView.setTextColor(Color.BLACK) // Set warna teks menjadi hitam
                return view
            }
        }
        val kabupatenAdapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, kabupatenList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view as TextView
                // Menambahkan padding pada tampilan item dan mengubah warna teks menjadi hitam
                view.setPadding(20, 20, 20, 20) // Padding atas, kiri, kanan, bawah
                textView.setTextColor(Color.BLACK) // Set warna teks menjadi hitam
                return view
            }
        }
        val kapanewonAdapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, kapanewonList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view as TextView
                // Menambahkan padding pada tampilan item dan mengubah warna teks menjadi hitam
                view.setPadding(20, 20, 20, 20) // Padding atas, kiri, kanan, bawah
                textView.setTextColor(Color.BLACK) // Set warna teks menjadi hitam
                return view
            }
        }
        val kalurahanAdapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, kalurahanList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view as TextView
                // Menambahkan padding pada tampilan item dan mengubah warna teks menjadi hitam
                view.setPadding(20, 20, 20, 20) // Padding atas, kiri, kanan, bawah
                textView.setTextColor(Color.BLACK) // Set warna teks menjadi hitam
                return view
            }
        }

        // Menentukan resource untuk dropdown view
        provinsiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        kabupatenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        kapanewonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        kalurahanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Menetapkan adapter pada spinner
        provinsiSpinner.adapter = provinsiAdapter
        kabupatenSpinner.adapter = kabupatenAdapter
        kapanewonSpinner.adapter = kapanewonAdapter
        kalurahanSpinner.adapter = kalurahanAdapter
    }



    private fun getSelectedSpinnerValues(): Map<String, String>? {
        val provinsi = provinsiSpinner.selectedItem.toString()
        val kabupaten = kabupatenSpinner.selectedItem.toString()
        val kapanewon = kapanewonSpinner.selectedItem.toString()
        val kalurahan = kalurahanSpinner.selectedItem.toString()

        if (provinsi == "Pilih Provinsi" || kabupaten == "Pilih Kabupaten" || kapanewon == "Pilih Kapanewon" || kalurahan == "Pilih Kalurahan") {
            Toast.makeText(requireContext(), "Semua field spinner harus dipilih!", Toast.LENGTH_SHORT).show()
            return null
        }

        return mapOf(
            "provinsi" to provinsi,
            "kabupaten" to kabupaten,
            "kapanewon" to kapanewon,
            "kalurahan" to kalurahan
        )
    }



    private fun validateForm(): Boolean {
        var valid = true

        if (namaLengkapInput.text.isEmpty()) {
            namaLengkapInput.error = "Nama lengkap wajib diisi"
            valid = false
        }
        if (alamatInput.text.isEmpty()) {
            alamatInput.error = "Alamat wajib diisi"
            valid = false
        }
        if (nikInput.text.isEmpty() || nikInput.text.length != 16) {
            nikInput.error = "NIK harus terdiri dari 16 digit"
            valid = false
        }
        if (noKkInput.text.isEmpty() || noKkInput.text.length != 16) {
            noKkInput.error = "Nomor KK harus terdiri dari 16 digit"
            valid = false
        }
        if (kebutuhanpangan.text.isEmpty()) {
            kebutuhanpangan.error = "Wajib diisi"
            valid = false
        }
        if (alasanusulan.text.isEmpty() || alasanusulan.text.split(" ").size < 3) {
            alasanusulan.error = "Wajib diisi minimal 3 kata"
            valid = false
        }

        if (fotoRumahTampakDepanUri == null || fotoRumahTampakBelakangUri == null || fotoRumahTampakDalamUri == null || fotoKkUri == null || fotoKtpUri == null) {
            Toast.makeText(requireContext(), "Semua foto wajib diupload", Toast.LENGTH_SHORT).show()
            valid = false
        }
        return valid
    }


    private fun validateFormUpdate(): Boolean {
        var valid = true

        if (namaLengkapInput.text.isEmpty()) {
            namaLengkapInput.error = "Nama lengkap wajib diisi"
            valid = false
        }
        if (alamatInput.text.isEmpty()) {
            alamatInput.error = "Alamat wajib diisi"
            valid = false
        }
        if (nikInput.text.isEmpty() || nikInput.text.length != 16) {
            nikInput.error = "NIK harus terdiri dari 16 digit"
            valid = false
        }
        if (noKkInput.text.isEmpty() || noKkInput.text.length != 16) {
            noKkInput.error = "Nomor KK harus terdiri dari 16 digit"
            valid = false
        }
        if (kebutuhanpangan.text.isEmpty()) {
            kebutuhanpangan.error = "Wajib diisi"
            valid = false
        }
        if (alasanusulan.text.isEmpty() || alasanusulan.text.split(" ").size < 3) {
            alasanusulan.error = "Wajib diisi minimal 3 kata"
            valid = false
        }


        return valid
    }


    private fun submitDataToServer(spinnerValues: Map<String, String>) {
        // Ambil nilai dari EditText
        val namaLengkap = RequestBody.create("text/plain".toMediaTypeOrNull(), namaLengkapInput.text.toString())
        val alamat = RequestBody.create("text/plain".toMediaTypeOrNull(), alamatInput.text.toString())
        val nik = RequestBody.create("text/plain".toMediaTypeOrNull(), nikInput.text.toString())
        val noKk = RequestBody.create("text/plain".toMediaTypeOrNull(), noKkInput.text.toString())
        val kebutuhanpangan = RequestBody.create("text/plain".toMediaTypeOrNull(), kebutuhanpangan.text.toString())
        val alasanusulan = RequestBody.create("text/plain".toMediaTypeOrNull(), alasanusulan.text.toString())

        // Membuat request untuk username
        val sessionManager = SessionManager(requireContext())
        val username = sessionManager.getUserSession()["username"] ?: "unknown" // Pastikan username tidak null
        val usernameRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), username)

        // Ambil nilai spinner dari spinnerValues
        val provinsi = spinnerValues["provinsi"] ?: ""
        val kabupaten = spinnerValues["kabupaten"] ?: ""
        val kapanewon = spinnerValues["kapanewon"] ?: ""
        val kalurahan = spinnerValues["kalurahan"] ?: ""

        // Membuat requestBody untuk spinner values
        val provinsiRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), provinsi)
        val kabupatenRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), kabupaten)
        val kapanewonRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), kapanewon)
        val kalurahanRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), kalurahan)

        // Log values for debugging
        Log.d("BPNT", "Provinsi: $provinsi, Kabupaten: $kabupaten, Kapanewon: $kapanewon, Kalurahan: $kalurahan, Username: $username")

        // Cek apakah foto telah diupload
        val rumahTampakDepanImagePart = fotoRumahTampakDepanUri?.let { uri ->
            createImagePart("rumah_tampak_depan_image", uri)
        }
        val rumahTampakBelakangImagePart = fotoRumahTampakBelakangUri?.let { uri ->
            createImagePart("rumah_tampak_belakang_image", uri)
        }
        val rumahTampakDalamImagePart = fotoRumahTampakDalamUri?.let { uri ->
            createImagePart("rumah_tampak_dalam_image", uri)
        }
        val ktpImagePart = fotoKtpUri?.let { uri ->
            createImagePart("ktp_image", uri)
        }
        val kkImagePart = fotoKkUri?.let { uri ->
            createImagePart("kk_image", uri)
        }

        // Pastikan semua foto sudah dipilih
        if (rumahTampakDepanImagePart == null || rumahTampakBelakangImagePart == null || rumahTampakDalamImagePart == null || ktpImagePart == null || kkImagePart == null) {
            Toast.makeText(requireContext(), "Semua foto wajib diupload", Toast.LENGTH_SHORT).show()
            return
        }

        // Retrofit API call dengan menambahkan username dan nilai spinner
        val call = RetrofitClient.apiServiceBPNT.submitBpntForm(
            namaLengkap, alamat, nik, noKk, kebutuhanpangan, alasanusulan,
            usernameRequest,  // Menambahkan username dalam request
            provinsiRequest, kabupatenRequest, kapanewonRequest, kalurahanRequest,
            rumahTampakDepanImagePart,
            rumahTampakDalamImagePart,
            rumahTampakBelakangImagePart,
            ktpImagePart,
            kkImagePart
        )

        call.enqueue(object : Callback<ApiResponseBPNT> {
            override fun onResponse(call: Call<ApiResponseBPNT>, response: Response<ApiResponseBPNT>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Data berhasil dikirim", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal mengirim data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponseBPNT>, t: Throwable) {
                Toast.makeText(requireContext(), "Terjadi kesalahan ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d("BPNT", t.message.toString())
            }
        })
    }



    private fun updateDataToServer(spinnerValues: Map<String, String>) {
        // Ambil nilai dari EditText
        val namaLengkap = RequestBody.create("text/plain".toMediaTypeOrNull(), namaLengkapInput.text.toString())
        val alamat = RequestBody.create("text/plain".toMediaTypeOrNull(), alamatInput.text.toString())
        val nik = RequestBody.create("text/plain".toMediaTypeOrNull(), nikInput.text.toString())
        val noKk = RequestBody.create("text/plain".toMediaTypeOrNull(), noKkInput.text.toString())
        val kebutuhanpangan = RequestBody.create("text/plain".toMediaTypeOrNull(), kebutuhanpangan.text.toString())
        val alasanusulan = RequestBody.create("text/plain".toMediaTypeOrNull(), alasanusulan.text.toString())

        // Ambil username dari session
        val sessionManager = SessionManager(requireContext())
        val username = sessionManager.getUserSession()["username"] ?: "unknown"
        val usernameRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), username)

        // Ambil nilai spinner
        val provinsi = spinnerValues["provinsi"] ?: ""
        val kabupaten = spinnerValues["kabupaten"] ?: ""
        val kapanewon = spinnerValues["kapanewon"] ?: ""
        val kalurahan = spinnerValues["kalurahan"] ?: ""

        // Buat requestBody untuk spinner values
        val provinsiRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), provinsi)
        val kabupatenRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), kabupaten)
        val kapanewonRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), kapanewon)
        val kalurahanRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), kalurahan)

        // Log untuk debugging
        Log.d("BPNT", "Provinsi: $provinsi, Kabupaten: $kabupaten, Kapanewon: $kapanewon, Kalurahan: $kalurahan, Username: $username")

        // Cek apakah gambar baru diunggah, jika tidak null maka kirim
        val rumahTampakDepanImagePart = fotoRumahTampakDepanUri?.let { uri ->
            createImagePart("rumah_tampak_depan_image", uri).also {
                Log.d("ImageParts", "Rumah Tampak Depan ImagePart: $it")
            } // Gambar baru
        } ?: MultipartBody.Part.createFormData("rumah_tampak_depan_image", "").also {
            Log.d("ImageParts", "Rumah Tampak Depan ImagePart (Empty): $it")
        }

        val rumahTampakBelakangImagePart = fotoRumahTampakBelakangUri?.let { uri ->
            createImagePart("rumah_tampak_belakang_image", uri).also {
                Log.d("ImageParts", "Rumah Tampak Belakang ImagePart: $it")
            }
        } ?: MultipartBody.Part.createFormData("rumah_tampak_belakang_image", "").also {
            Log.d("ImageParts", "Rumah Tampak Belakang ImagePart (Empty): $it")
        }

        val rumahTampakDalamImagePart = fotoRumahTampakDalamUri?.let { uri ->
            createImagePart("rumah_tampak_dalam_image", uri).also {
                Log.d("ImageParts", "Rumah Tampak Dalam ImagePart: $it")
            }
        } ?: MultipartBody.Part.createFormData("rumah_tampak_dalam_image", "").also {
            Log.d("ImageParts", "Rumah Tampak Dalam ImagePart (Empty): $it")
        }

        val ktpImagePart = fotoKtpUri?.let { uri ->
            createImagePart("ktp_image", uri).also {
                Log.d("ImageParts", "KTP ImagePart: $it")
            }
        } ?: MultipartBody.Part.createFormData("ktp_image", "").also {
            Log.d("ImageParts", "KTP ImagePart (Empty): $it")
        }

        val kkImagePart = fotoKkUri?.let { uri ->
            createImagePart("kk_image", uri).also {
                Log.d("ImageParts", "KK ImagePart: $it")
            }
        } ?: MultipartBody.Part.createFormData("kk_image", "").also {
            Log.d("ImageParts", "KK ImagePart (Empty): $it")
        }

// Cetak total jumlah part gambar
        val totalImageParts = listOf(
            rumahTampakDepanImagePart,
            rumahTampakBelakangImagePart,
            rumahTampakDalamImagePart,
            ktpImagePart,
            kkImagePart
        ).size

        Log.d("ImageParts", "Total ImageParts: $totalImageParts")
        Log.d("ISINYA APA??",imageUrlsSimpan.toString())

        val imageKeys = listOf("rumah_tampak_depan", "rumah_tampak_belakang", "rumah_tampak_dalam", "ktp", "kk")
        val isAllImagesUploaded = imageKeys.all { imageUrlsSimpan[it]?.isNotEmpty() == true }

        if (!isAllImagesUploaded) {
            Toast.makeText(requireContext(), "Semua foto wajib diupload", Toast.LENGTH_SHORT).show()
        }

        // Retrofit API call dengan metode PUT untuk update
        val call = RetrofitClient.apiServiceBPNT.UpdateBPNTForm(
            namaLengkap, alamat, nik, noKk, kebutuhanpangan, alasanusulan,
            usernameRequest,
            provinsiRequest, kabupatenRequest, kapanewonRequest, kalurahanRequest,
            rumahTampakDepanImagePart, // Kosong jika tidak diunggah ulang
            rumahTampakDalamImagePart,
            rumahTampakBelakangImagePart,
            ktpImagePart,
            kkImagePart
        )

        call.enqueue(object : Callback<ApiResponseBPNT> {
            override fun onResponse(call: Call<ApiResponseBPNT>, response: Response<ApiResponseBPNT>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal memperbarui data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponseBPNT>, t: Throwable) {
                Toast.makeText(requireContext(), "Terjadi kesalahan ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d("BPNT", t.message.toString())
            }
        })
    }









    private fun createImagePart(name: String, uri: Uri): MultipartBody.Part {
        val file = File(getRealPathFromURI(uri))
        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
        return MultipartBody.Part.createFormData(name, file.name, requestFile)
    }

    fun getRealPathFromURI(uri: Uri): String {
        // Ambil informasi username dari session (misalnya)
        val sessionManager = SessionManager(requireContext())
        val username = sessionManager.getUserSession()["username"] ?: "unknown"
        // Ambil timestamp saat ini untuk nama unik
        val timestamp = System.currentTimeMillis()
        // Ambil nama file asli dari URI
        val fileName = getFileNameFromURI(uri)
        // Gabungkan timestamp, username, dan nama file untuk menghasilkan path yang diinginkan
        val newFileName = "${timestamp}-${username}-${fileName}"

        // Tentukan lokasi penyimpanan di direktori cache atau tempat penyimpanan lainnya
        val storageDirectory = requireContext().cacheDir
        val tempFile = File(storageDirectory, newFileName)

        // Salin file dari URI ke file sementara
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(tempFile)

        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        // Kembalikan path file yang baru
        return tempFile.absolutePath
    }

    fun getFileNameFromURI(uri: Uri): String {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
        val fileName = cursor?.getString(columnIndex ?: 0) ?: "untitled"
        cursor?.close()
        return fileName
    }

    // Fungsi untuk menampilkan gambar dalam modal

    private fun showImageDialog(imageUrl: String) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_image_view)

        // Mengatur ukuran dialog agar sesuai dengan layar
        val layoutParams = dialog.window?.attributes
        layoutParams?.width = (resources.displayMetrics.widthPixels * 0.9).toInt() // Set width to 90% of screen width
        layoutParams?.height = (resources.displayMetrics.heightPixels * 0.7).toInt() // Set height to 70% of screen height
        dialog.window?.attributes = layoutParams

        val imageView: SubsamplingScaleImageView = dialog.findViewById(R.id.imageView)

        // Memastikan URL gambar yang valid
        Log.d("Image URL", "URL to load: $imageUrl")

        val imageLoader = ImageLoader(requireContext())
        val request = ImageRequest.Builder(requireContext())
            .data(imageUrl)
            .target(object : coil.target.Target {
                override fun onStart(placeholder: Drawable?) {
                    // Menangani ketika gambar mulai dimuat
                }

                override fun onSuccess(result: Drawable) {
                    // Menangani ketika gambar berhasil dimuat
                    val bitmap = (result as BitmapDrawable).bitmap

                    // Setel gambar di SubsamplingScaleImageView
                    imageView.setImage(ImageSource.bitmap(bitmap))

                    // Pilih scale type yang sesuai
                    imageView.setScaleAndCenter(1.0f, PointF(0f, 0f))
                    imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE) // Menjaga gambar tetap proporsional
                }

                override fun onError(error: Drawable?) {
                    // Menangani kesalahan pemuatan gambar
                }
            })
            .build()

        imageLoader.enqueue(request)

        val closeButton: Button = dialog.findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            Log.d("CloseDialog", "Close button clicked")
            closeDialog(dialog) // Panggil closeDialog dengan view sebagai 'it'
        }


        val downloadButton: Button = dialog.findViewById(R.id.downloadButton)
        downloadButton.setOnClickListener {
            downloadImage(imageUrl) // Panggil fungsi downloadImage dengan URL gambar
        }


        dialog.show()
    }


    private fun downloadImage(imageUrl: String) {
        // Periksa versi Android
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Android 10+ (API 29 ke atas)
            downloadImageForAndroid10Plus(imageUrl)
        } else {
            // Android di bawah 10
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Minta izin untuk menyimpan ke penyimpanan eksternal
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            } else {
                // Jika izin sudah diberikan
                downloadImageForBelowAndroid10(imageUrl)
            }
        }
    }

    // Fungsi untuk Android 10+ (API 29 ke atas)
    private fun downloadImageForAndroid10Plus(imageUrl: String) {
        val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(imageUrl))
            .setTitle("Gambar BPNT")
            .setDescription("Sedang mengunduh gambar BPNT")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "bpnt_image.jpg") // Direkomendasikan

        downloadManager.enqueue(request)
        Toast.makeText(requireContext(), "Gambar sedang diunduh", Toast.LENGTH_SHORT).show()
    }

    // Fungsi untuk Android di bawah 10
    private fun downloadImageForBelowAndroid10(imageUrl: String) {
        val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(imageUrl))
            .setTitle("Gambar BPNT")
            .setDescription("Sedang mengunduh gambar BPNT")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "bpnt_image.jpg")

        downloadManager.enqueue(request)
        Toast.makeText(requireContext(), "Gambar sedang diunduh", Toast.LENGTH_SHORT).show()
    }

    // Tangani hasil permintaan izin untuk Android di bawah 10
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Izin diberikan, lanjutkan proses unduhan
            Toast.makeText(requireContext(), "Izin diberikan, coba unduh ulang gambar.", Toast.LENGTH_SHORT).show()
        } else {
            // Izin ditolak
            Toast.makeText(requireContext(), "Izin diperlukan untuk menyimpan gambar.", Toast.LENGTH_SHORT).show()
        }
    }


    // Fungsi untuk menutup dialog
    fun closeDialog(dialog: Dialog) {
        dialog.dismiss() // Menutup dialog secara langsung
    }

    private fun deleteImageBPNTAsinkronCall(imageView: ImageView, imagePath: String?, imagePathKey: String) {
        // Menghapus gambar dari ImageView
        imageView.setImageResource(0)

        // Mengubah nilai path gambar menjadi null
        when (imagePathKey) {
            "rumah_tampak_depan" -> fotoRumahTampakDepanUri = null
            "rumah_tampak_belakang" -> fotoRumahTampakBelakangUri = null
            "rumah_tampak_dalam" -> fotoRumahTampakDalamUri = null
            "kk" -> fotoKkUri = null
            "ktp" -> fotoKtpUri = null
        }

        // Menyembunyikan tombol delete segera setelah gambar dihapus
        when (imagePathKey) {
            "rumah_tampak_depan" -> deleteFotoRumahTampakDepan.visibility = View.GONE
            "rumah_tampak_belakang" -> deleteFotoRumahTampakBelakang.visibility = View.GONE
            "rumah_tampak_dalam" -> deleteFotoRumahTampakDalam.visibility = View.GONE
            "ktp" -> deleteFotoKtp.visibility = View.GONE
            "kk" -> deleteFotoKk.visibility = View.GONE
        }

        // Jika ada path gambar (imagePath) yang tidak null, kirimkan untuk dihapus dari server
        if (!imagePath.isNullOrEmpty()) {
            val sessionManager = SessionManager(requireContext())
            val username = sessionManager.getUserSession()["username"] ?: return

            // Panggil API untuk menghapus gambar dari server
            deleteImageBPNTAsinkron(username, imagePath) { success ->
                if (success) {
                    // Hanya reload data setelah gambar berhasil dihapus di server
                    fetchDataFromServer()
                } else {
                    // Jika penghapusan gagal, tampilkan pesan error
                    Toast.makeText(context, "Gagal menghapus gambar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Fungsi untuk menghapus gambar dari server (menggunakan Retrofit dengan callback)
    private fun deleteImageBPNTAsinkron(username: String, imagePath: String, callback: (Boolean) -> Unit) {
        val call = RetrofitClient.apiServiceBPNT.deleteImage(username, imagePath)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // Menangani respons sukses
                    Toast.makeText(context, "Gambar berhasil dihapus", Toast.LENGTH_SHORT).show()
                    callback(true)  // Menandakan penghapusan berhasil
                } else {
                    // Menangani respons gagal
                    Toast.makeText(context, "Gagal menghapus gambar", Toast.LENGTH_SHORT).show()
                    callback(false)  // Menandakan penghapusan gagal
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Menangani kesalahan jika gagal memanggil API
                Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
                callback(false)  // Menandakan penghapusan gagal
            }
        })
    }







}
