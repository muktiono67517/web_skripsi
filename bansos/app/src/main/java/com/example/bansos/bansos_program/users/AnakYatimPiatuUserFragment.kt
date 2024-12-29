package com.example.bansos.bansos_program.users


import ApiResponseAnakYatimPiatu
import ApiResponseDisabilitas
import SessionManager
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("Deprecation")
class AnakYatimPiatuUserFragment : Fragment() {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var  teks_peringatan_mode_update: TextView
    private lateinit var namaAnakInput: EditText
    private lateinit var alamatInput: EditText
    private lateinit var nikInput: EditText
    private lateinit var noKkInput: EditText

    private lateinit var provinsiSpinner: Spinner
    private lateinit var kabupatenSpinner: Spinner
    private lateinit var kapanewonSpinner: Spinner
    private lateinit var kalurahanSpinner: Spinner
    private lateinit var kondisiOrangTuaSpinner: Spinner
    private lateinit var tempatLahir: EditText
    private lateinit var tanggalLahir: EditText
    private lateinit var umur: EditText
    private lateinit var pendidikanSekarang: EditText

    private lateinit var fotoKk: Button
    private lateinit var fotoKTPWaliPenganggungJawab: Button
    private lateinit var submitButton: Button
    private lateinit var updateButton: Button

    private lateinit var thumbKk: ImageView
    private lateinit var thumbKTPWaliPenganggungJawab: ImageView
    private lateinit var deleteFotoKk: ImageButton
    private lateinit var deleteKTPWaliPenganggungJawab: ImageButton

    private  lateinit var statusTextView:TextView
    private  lateinit var komentarTextView:TextView


    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageType: String? = null
    private var fotoKkUri: Uri? = null
    private var fotoKTPWaliPenganggungJawabUri: Uri? = null
    private val imageUrlsSimpan = mutableMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.anak_yatim_piatu_user, container, false)

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
        // SwipeRefreshLayout untuk refresh data
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        // Teks peringatan mode update
        teks_peringatan_mode_update = view.findViewById(R.id.teks_peringatan_mode_update)

        // Input fields
        namaAnakInput = view.findViewById(R.id.nama_anak)
        alamatInput = view.findViewById(R.id.alamat_text)
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

        nikInput = view.findViewById(R.id.nik_input_)
        noKkInput = view.findViewById(R.id.no_kk_input)

        // Spinners untuk lokasi
        provinsiSpinner = view.findViewById(R.id.provinsi_spinner)
        kabupatenSpinner = view.findViewById(R.id.kabupaten_spinner)
        kapanewonSpinner = view.findViewById(R.id.kapanewon_spinner)
        kalurahanSpinner = view.findViewById(R.id.kalurahan_spinner)

        // Spinner untuk kondisi orang tua
        kondisiOrangTuaSpinner = view.findViewById(R.id.kondisi_orang_tua_spinner)

        // Input fields untuk informasi tambahan
        tempatLahir = view.findViewById(R.id.tempat_lahir)
        tempatLahir.hint="Contoh:Yogyakarta"
        tempatLahir.setHintTextColor(Color.GRAY)

        tanggalLahir = view.findViewById(R.id.tanggal_lahir)


        tanggalLahir.setOnClickListener{
            showDatePickerDialog(tanggalLahir)

        }



        umur = view.findViewById(R.id.umur)
        pendidikanSekarang = view.findViewById(R.id.pendidikan_sekarang)

        // Tombol untuk unggah foto KK dan KTP
        fotoKk = view.findViewById(R.id.upload_foto_kk)
        fotoKTPWaliPenganggungJawab = view.findViewById(R.id.upload_foto_ktp_wali_penganggung_jawab)

        // Tombol untuk submit dan update
        submitButton = view.findViewById(R.id.submit_button)
        updateButton = view.findViewById(R.id.update_button)

        // Thumbnail ImageViews untuk menampilkan gambar KK dan KTP
        thumbKk = view.findViewById(R.id.thumb_foto_kk)
        thumbKTPWaliPenganggungJawab = view.findViewById(R.id.thumb_foto_ktp)

        statusTextView=view.findViewById(R.id.status_bantuan_yatim)
        komentarTextView=view.findViewById(R.id.pesan_admin_bantuan_yatim)

        // Tombol untuk menghapus gambar KK dan KTP
        deleteFotoKk = view.findViewById(R.id.delete_foto_kk)
        deleteKTPWaliPenganggungJawab = view.findViewById(R.id.delete_foto_ktp)
    }



    // Fungsi untuk menampilkan DatePickerDialog
    private fun showDatePickerDialog(tanggalLahir: EditText) {
        // Mendapatkan tanggal saat ini
        val calendar = Calendar.getInstance()
        val tahun = calendar.get(Calendar.YEAR)
        val bulan = calendar.get(Calendar.MONTH)
        val hari = calendar.get(Calendar.DAY_OF_MONTH)

        // Membuat DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                // Mengatur format tanggal
                calendar.set(year, monthOfYear, dayOfMonth)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val selectedDate = sdf.format(calendar.time)

                // Set tanggal yang dipilih ke EditText
                tanggalLahir.setText(selectedDate)
            },
            tahun, bulan, hari
        )

        // Menambahkan pembatas agar tanggal tidak lebih dari hari ini
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }






    private fun fetchDataFromServer() {
        val sessionManager = SessionManager(requireContext())
        val nik = sessionManager.getUserSession()["nik"] ?: return
        Log.d("NIK ANDA SESI", nik)

        // Memanggil API untuk mendapatkan data berdasarkan NIK
        val call = RetrofitClient.apiServiceAnakYatimPiatu.getAnakYatimPiatuData(nik)
        call.enqueue(object : Callback<ApiResponseAnakYatimPiatu> {
            override fun onResponse(call: Call<ApiResponseAnakYatimPiatu>, response: Response<ApiResponseAnakYatimPiatu>) {
                Log.d("onResponse", "Response received from server")

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()?.data
                    Log.d("Data Received", "Data from server: $data")

                    if (data != null) {
                        // Update EditText fields
                        Log.d("Updating EditTexts", "Setting EditText values")

                        namaAnakInput.setText(data.nama_anak)
                        alamatInput.setText(data.alamat)
                        nikInput.setText(data.nik)
                        noKkInput.setText(data.no_kk)

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




                        val kondisiOrangTuaList = arrayOf("Yatim", "Piatu", "Yatim Piatu")
                        val kondisiOrangTuaAdapter = object : ArrayAdapter<String>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                           kondisiOrangTuaList
                        ) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = super.getView(position, convertView, parent)
                                val textView = view.findViewById<TextView>(android.R.id.text1)
                                textView.setTextColor(Color.BLACK) // Set warna teks menjadi hitam
                                return view
                            }
                        }
                        kondisiOrangTuaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        kondisiOrangTuaSpinner.adapter = kondisiOrangTuaAdapter
                        val serverValueKondisiOrangTua = data.kondisi_orangtua
                        val selectedIndexx = kondisiOrangTuaList.indexOf(serverValueKondisiOrangTua)
                        if (selectedIndexx >= 0) {
                           kondisiOrangTuaSpinner.setSelection(selectedIndexx) // Pilih nilai sesuai data server
                        } else {
                            // Jika nilai dari server tidak ditemukan dalam array
                            Toast.makeText(requireContext(), "Data Kondisi Orang Tua tidak valid: $serverValueKondisiOrangTua", Toast.LENGTH_SHORT).show()
                        }


                        val tempatTanggalLahir = data.tempat_tanggal_lahir

// Memisahkan tempat dan tanggal
                        val parts = tempatTanggalLahir?.split(", ")
                        val tempatlahir = parts?.get(0) // "Kulon Progo"
                       val tanggallahir = if (parts?.size ?: 0 > 1) {
                            // Mengubah format tanggal dari "DD-MM-YYYY" menjadi "DD/MM/YYYY"
                            val dateParts = parts?.get(1)?.split("-")
                            if (dateParts?.size == 3) {
                                "${dateParts[0]}/${dateParts[1]}/${dateParts[2]}"
                            } else {
                                ""
                            }
                        } else {
                            ""
                        }


// Sekarang tempatLahir dan tanggalLahir memiliki nilai yang terpisah
                        println("Tempat Lahir: $tempatLahir")
                        println("Tanggal Lahir: $tanggalLahir")

// Pastikan Anda menggunakan input field yang sesuai
                        tempatLahir.setText(tempatlahir.toString()) // Update EditText untuk tempat lahir
                        tanggalLahir.setText(tanggallahir) // Update EditText untuk tanggal lahir



                        umur.setText(data.umur.toString())
                        pendidikanSekarang.setText(data.pendidikan_sekarang.toString())

                        // Menampilkan foto KTP dan KK jika ada
                        fotoKTPWaliPenganggungJawab.text = if (data.ktp_wali_penanggungjawab_image_path != null) {
                            fotoKTPWaliPenganggungJawab.isEnabled = false
                            "Gambar Tersedia"
                        } else {
                            fotoKTPWaliPenganggungJawab.isEnabled = true
                            "Upload Gambar"
                        }

                        fotoKk.text = if (data.kk_image_path != null) {
                            fotoKk.isEnabled = false
                            "Gambar Tersedia"
                        } else {
                            fotoKk.isEnabled = true
                            "Upload Gambar"
                        }

                        // Menampilkan tombol Update atau Simpan
                        if (data.nama_anak.isNotEmpty() && data.nik.isNotEmpty()) {
                            teks_peringatan_mode_update.text = "Perhatian: Harap isi semua data saat memperbarui. Data yang sudah Anda masukkan sebelumnya otomatis terisi. Ubah hanya bagian yang perlu diperbarui."
                            updateButton.visibility = View.VISIBLE
                            submitButton.visibility = View.GONE
                        } else {
                            submitButton.visibility = View.VISIBLE
                            updateButton.visibility = View.GONE
                        }

                        val imageBaseUrl = "http://192.168.46.100:5000/users/anak_yatim_piatu/uploads/"

                        // Handle KTP Image
                        data.ktp_wali_penanggungjawab_image_path?.let {
                            val imageUrl = "$imageBaseUrl$it"
                            Glide.with(requireContext()).load(imageUrl).into(thumbKTPWaliPenganggungJawab)

                            thumbKTPWaliPenganggungJawab.setOnClickListener {
                                showImageDialog(imageUrl)
                            }

                            thumbKTPWaliPenganggungJawab.setOnLongClickListener {
                                downloadImage(imageUrl)
                                true
                            }

                            deleteKTPWaliPenganggungJawab.setOnClickListener {
                                deleteImageAnakYatimPiatuAsinkronCall(thumbKTPWaliPenganggungJawab, imageUrl, "ktp")
                            }
                            imageUrlsSimpan["ktp"] = it
                            deleteKTPWaliPenganggungJawab.visibility = View.VISIBLE
                        } ?: run {
                            deleteKTPWaliPenganggungJawab.visibility = View.GONE
                        }

                        // Handle KK Image
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
                                deleteImageAnakYatimPiatuAsinkronCall(thumbKk, imageUrl, "kk")
                            }
                            imageUrlsSimpan["kk"] = it
                            deleteFotoKk.visibility = View.VISIBLE
                        } ?: run {
                            deleteFotoKk.visibility = View.GONE
                        }

                        // Menyembunyikan SwipeRefreshLayout setelah data selesai dimuat
                        swipeRefreshLayout.isRefreshing = false
                    }
                    if (data != null) {
                        statusTextView.text = data.status
                    }
                    if (data != null) {
                        komentarTextView.text = data.komentar
                    }
                    if (data != null) {
                        Log.d("TAG", "Status: ${data.status}")
                    }
                    if (data != null) {
                        Log.d("TAG", "Komentar: ${data.komentar}")
                    }
// Mengubah warna teks menjadi hitam
                    statusTextView.setTextColor(Color.BLACK)
                    komentarTextView.setTextColor(Color.BLACK)
                } else {
                    Log.d("Response Error", "Failed to load data from server")
                    Toast.makeText(requireContext(), "", Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
            }

            override fun onFailure(call: Call<ApiResponseAnakYatimPiatu>, t: Throwable) {
                Log.e("onFailure", "Error occurred: ${t.message}")
                Toast.makeText(requireContext(), "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
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
        fotoKk.setOnClickListener { openGallery("kk") }
        fotoKTPWaliPenganggungJawab.setOnClickListener { openGallery("ktp") }
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
                    "kk" -> {
                        fotoKkUri = it
                        loadImageIntoView(it, thumbKk)
                    }
                    "ktp" -> {
                        fotoKTPWaliPenganggungJawabUri = it
                        loadImageIntoView(it, thumbKTPWaliPenganggungJawab)
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
        val kondisiOrangTuaList = arrayOf("Yatim", "Piatu", "Yatim Piatu")

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

        val kondisiOrangTuaAdapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, kondisiOrangTuaList) {
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
        kondisiOrangTuaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Menetapkan adapter pada spinner
        provinsiSpinner.adapter = provinsiAdapter
        kabupatenSpinner.adapter = kabupatenAdapter
        kapanewonSpinner.adapter = kapanewonAdapter
        kalurahanSpinner.adapter = kalurahanAdapter
        kondisiOrangTuaSpinner.adapter=kondisiOrangTuaAdapter
    }



    private fun getSelectedSpinnerValues(): Map<String, String>? {
        // Mendapatkan nilai yang dipilih dari spinner
        val selectedValues = mapOf(
            "provinsi" to provinsiSpinner.selectedItem.toString(),
            "kabupaten" to kabupatenSpinner.selectedItem.toString(),
            "kapanewon" to kapanewonSpinner.selectedItem.toString(),
            "kalurahan" to kalurahanSpinner.selectedItem.toString(),
            "kondisi_orangtua" to kondisiOrangTuaSpinner.selectedItem.toString()
        )

        // Daftar pilihan default yang perlu dihindari
        val defaultValues = listOf("Pilih Provinsi", "Pilih Kabupaten", "Pilih Kapanewon", "Pilih Kalurahan")

        // Memeriksa apakah ada spinner yang belum dipilih dengan benar
        for ((key, value) in selectedValues) {
            if (value in defaultValues) {
                Toast.makeText(requireContext(), "Semua field spinner harus dipilih!", Toast.LENGTH_SHORT).show()
                return null
            }
        }
        return selectedValues
    }





    private fun validateForm(): Boolean {
        var valid = true

        // Memvalidasi EditText
        valid = valid && validateEditText(namaAnakInput,"Nama lengkap wajib diisi")
        valid = valid && validateEditText(alamatInput, "Alamat wajib diisi")
        valid = valid && validateEditText(nikInput, "NIK harus terdiri dari 16 digit", 16)
        valid = valid && validateEditText(noKkInput, "Nomor KK harus terdiri dari 16 digit", 16)

        // Memvalidasi Spinner
        valid = valid && validateSpinner(provinsiSpinner, "Pilih Provinsi")
        valid = valid && validateSpinner(kabupatenSpinner, "Pilih Kabupaten")
        valid = valid && validateSpinner(kapanewonSpinner, "Pilih Kapanewon")
        valid = valid && validateSpinner(kalurahanSpinner, "Pilih Kalurahan")
        valid = valid && validateSpinner(kondisiOrangTuaSpinner, "Pilih Kondisi Orang Tua Anak")

        // Memvalidasi Foto
        if (fotoKkUri == null || fotoKTPWaliPenganggungJawabUri == null) {
            Toast.makeText(requireContext(), "Semua foto wajib diupload", Toast.LENGTH_SHORT).show()
            valid = false
        }

        return valid
    }



    // Fungsi untuk memvalidasi EditText
    private fun validateEditText(editText: EditText, errorMessage: String, length: Int? = null): Boolean {
        if (editText.text.isEmpty() || (length != null && editText.text.length != length)) {
            editText.error = errorMessage
            return false
        }
        return true
    }

    // Fungsi untuk memvalidasi Spinner
    private fun validateSpinner(spinner: Spinner, defaultValue: String): Boolean {
        if (spinner.selectedItem.toString() == defaultValue) {
            Toast.makeText(requireContext(), "Semua field spinner harus dipilih!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }




    private fun validateFormUpdate(): Boolean {
        var valid = true

        // Memvalidasi EditText
        valid = valid && validateEditText(namaAnakInput, "Nama lengkap wajib diisi")
        valid = valid && validateEditText(alamatInput, "Alamat wajib diisi")
        valid = valid && validateEditText(nikInput, "NIK harus terdiri dari 16 digit", 16)
        valid = valid && validateEditText(noKkInput, "Nomor KK harus terdiri dari 16 digit", 16)

        return valid
    }



    private fun submitDataToServer(spinnerValues: Map<String, String>) {
        // Ambil nilai dari EditText
        val namaAnak = RequestBody.create("text/plain".toMediaTypeOrNull(), namaAnakInput.text.toString())
        val alamatText = RequestBody.create("text/plain".toMediaTypeOrNull(), alamatInput.text.toString())
        val nik = RequestBody.create("text/plain".toMediaTypeOrNull(), nikInput.text.toString())
        val noKk = RequestBody.create("text/plain".toMediaTypeOrNull(), noKkInput.text.toString())
        val tempat_lahir = tempatLahir.text.toString()
        val tanggal_lahir = tanggalLahir.text.toString()

        println("Tempat Lahir: $tempat_lahir, Tanggal Lahir: $tanggal_lahir")

        val tempatTanggalLahir = "$tempat_lahir, $tanggal_lahir"

        val tempatTanggalLahirRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), tempatTanggalLahir)

        println("Tempat tgl Lahir: $tempatTanggalLahir")

        println("Tempat dan Tanggal Lahir: $tempatTanggalLahir")

        val pendidikanSekarang = RequestBody.create("text/plain".toMediaTypeOrNull(), pendidikanSekarang.text.toString())


        // Membuat request untuk username
        val sessionManager = SessionManager(requireContext())
        val username = sessionManager.getUserSession()["username"] ?: "unknown"
        val usernameRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), username)

        // Ambil nilai spinner dari spinnerValues
        val provinsi = spinnerValues["provinsi"] ?: ""
        val kabupaten = spinnerValues["kabupaten"] ?: ""
        val kapanewon = spinnerValues["kapanewon"] ?: ""
        val kalurahan = spinnerValues["kalurahan"] ?: ""
        val kondisiOrangTua = spinnerValues["kondisi_orangtua"] ?: ""

        // Membuat requestBody untuk spinner values
        val provinsiRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), provinsi)
        val kabupatenRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), kabupaten)
        val kapanewonRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), kapanewon)
        val kalurahanRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), kalurahan)
        val kondisiOrangTuaRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), kondisiOrangTua)

        // Cek apakah foto telah diupload
        val ktpImagePart = fotoKTPWaliPenganggungJawabUri?.let { uri -> createImagePart("ktp_image", uri) }
        val kkImagePart = fotoKkUri?.let { uri -> createImagePart("kk_image", uri) }

        // Pastikan semua foto sudah dipilih
        if (ktpImagePart == null || kkImagePart == null) {
            Toast.makeText(requireContext(), "Semua foto wajib diupload", Toast.LENGTH_SHORT).show()
            return
        }

        // Retrofit API call dengan menambahkan username dan nilai spinner
        val call = RetrofitClient.apiServiceAnakYatimPiatu.submitAnakYatimPiatuForm(
            namaAnak,alamatText,provinsiRequest,kabupatenRequest,kapanewonRequest,kalurahanRequest,nik,noKk,kondisiOrangTuaRequest,kkImagePart,ktpImagePart,tempatTanggalLahirRequest,pendidikanSekarang,usernameRequest
        )

        call.enqueue(object : Callback<ApiResponseAnakYatimPiatu> {
            override fun onResponse(call: Call<ApiResponseAnakYatimPiatu>, response: Response<ApiResponseAnakYatimPiatu>) {
                if (response.isSuccessful) {
                    Log.d("EOR KENAPA",response.body().toString())
                    Toast.makeText(requireContext(), "Data berhasil dikirim", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal mengirim data", Toast.LENGTH_SHORT).show()
                    Log.d("EOR KENAPA",response.body().toString())
                }
            }

            override fun onFailure(call: Call<ApiResponseAnakYatimPiatu>, t: Throwable) {
                Toast.makeText(requireContext(), "Terjadi kesalahan ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d("Disabilitas", t.message.toString())
            }
        })
    }








    private fun updateDataToServer(spinnerValues: Map<String, String>) {
        // Ambil nilai dari EditText
        val namaAnak = RequestBody.create("text/plain".toMediaTypeOrNull(), namaAnakInput.text.toString())
        val alamatText = RequestBody.create("text/plain".toMediaTypeOrNull(), alamatInput.text.toString())
        val nik = RequestBody.create("text/plain".toMediaTypeOrNull(), nikInput.text.toString())
        val noKk = RequestBody.create("text/plain".toMediaTypeOrNull(), noKkInput.text.toString())
        val tempat_lahir = tempatLahir.text.toString()
        val tanggal_lahir = tanggalLahir.text.toString()

        println("Tempat Lahir: $tempat_lahir, Tanggal Lahir: $tanggal_lahir")

        val tempatTanggalLahir = "$tempat_lahir, $tanggal_lahir"

        val tempatTanggalLahirRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), tempatTanggalLahir)

        println("Tempat tgl Lahir: $tempatTanggalLahir")

        println("Tempat dan Tanggal Lahir: $tempatTanggalLahir")

        val pendidikanSekarang = RequestBody.create("text/plain".toMediaTypeOrNull(), pendidikanSekarang.text.toString())


        // Membuat request untuk username
        val sessionManager = SessionManager(requireContext())
        val username = sessionManager.getUserSession()["username"] ?: "unknown"
        val usernameRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), username)

        // Ambil nilai spinner dari spinnerValues
        val provinsi = spinnerValues["provinsi"] ?: ""
        val kabupaten = spinnerValues["kabupaten"] ?: ""
        val kapanewon = spinnerValues["kapanewon"] ?: ""
        val kalurahan = spinnerValues["kalurahan"] ?: ""
        val kondisiOrangTua = spinnerValues["kondisi_orangtua"] ?: ""

        // Membuat requestBody untuk spinner values

        // Membuat requestBody untuk spinner values
        val provinsiRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), provinsi)
        val kabupatenRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), kabupaten)
        val kapanewonRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), kapanewon)
        val kalurahanRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), kalurahan)
        val kondisiOrangTuaRequest = RequestBody.create("text/plain".toMediaTypeOrNull(), kondisiOrangTua)

        // Log untuk debugging


        // Membuat image parts untuk foto KTP dan KK
        val ktpImagePart = fotoKTPWaliPenganggungJawabUri?.let { uri ->
            Log.d("ImageParts", "URI KTP: $uri")
            // Memastikan path file yang digunakan
            val filePath = getRealPathFromURI(uri)
            Log.d("ImageParts", "Path file KTP: $filePath")

            // Cek apakah file tersedia
            val file = File(filePath)
            if (file.exists()) {
                Log.d("ImageParts", "File KTP ditemukan: ${file.name}")
            } else {
                Log.d("ImageParts", "File KTP tidak ditemukan.")
            }

            createImagePart("ktp_image", uri).also {
                Log.d("ImageParts", "KTP ImagePart berhasil dibuat: $it")
            }
        } ?: run {
            Log.d("ImageParts", "KTP ImagePart kosong, tidak ada file gambar.")
            MultipartBody.Part.createFormData("ktp_image", "").also {
                Log.d("ImageParts", "KTP ImagePart (Empty): $it")
            }
        }

        val kkImagePart = fotoKkUri?.let { uri ->
            Log.d("ImageParts", "URI KK: $uri")
            val filePath = getRealPathFromURI(uri)
            Log.d("ImageParts", "Path file KK: $filePath")

            val file = File(filePath)
            if (file.exists()) {
                Log.d("ImageParts", "File KK ditemukan: ${file.name}")
            } else {
                Log.d("ImageParts", "File KK tidak ditemukan.")
            }

            createImagePart("kk_image", uri).also {
                Log.d("ImageParts", "KK ImagePart berhasil dibuat: $it")
            }
        } ?: run {
            Log.d("ImageParts", "KK ImagePart kosong, tidak ada file gambar.")
            MultipartBody.Part.createFormData("kk_image", "").also {
                Log.d("ImageParts", "KK ImagePart (Empty): $it")
            }
        }

// Cetak total jumlah part gambar
        val totalImageParts = listOf(
            ktpImagePart,
            kkImagePart
        ).size

        Log.d("ImageParts", "Total ImageParts: $totalImageParts")
        Log.d("ImageParts", "Image URL Simpan: ${imageUrlsSimpan.toString()}")


        // Memastikan semua foto wajib di-upload
        val imageKeys = listOf("ktp", "kk")
        val isAllImagesUploaded = imageKeys.all { imageUrlsSimpan[it]?.isNotEmpty() == true }

//        if (!isAllImagesUploaded) {
//            Toast.makeText(requireContext(), "Semua foto wajib diupload", Toast.LENGTH_SHORT).show()
//            return // Menghentikan proses jika ada gambar yang belum diupload
//        }

        // Retrofit API call dengan metode PUT untuk update
        val call = RetrofitClient.apiServiceAnakYatimPiatu.updateAnakYatimPiatuForm(
            namaAnak,alamatText,provinsiRequest,kabupatenRequest,kapanewonRequest,kalurahanRequest,nik,noKk,kondisiOrangTuaRequest,kkImagePart,ktpImagePart,tempatTanggalLahirRequest,pendidikanSekarang,usernameRequest
        )

        // Mengirim data dan menangani response
        call.enqueue(object : Callback<ApiResponseAnakYatimPiatu> {
            override fun onResponse(call: Call<ApiResponseAnakYatimPiatu>, response: Response<ApiResponseAnakYatimPiatu>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal memperbarui data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponseAnakYatimPiatu>, t: Throwable) {
                Toast.makeText(requireContext(), "Terjadi kesalahan ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d("Disabilitas", t.message.toString())
            }
        })
    }






    private fun createImagePart(name: String, uri: Uri): MultipartBody.Part {
        val file = File(getRealPathFromURI(uri))
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
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
            .setTitle("Gambar Bantuan Disabilitas")
            .setDescription("Sedang mengunduh gambar Bantuan Disabilitas")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "disabilitas_image.jpg") // Direkomendasikan

        downloadManager.enqueue(request)
        Toast.makeText(requireContext(), "Gambar sedang diunduh", Toast.LENGTH_SHORT).show()
    }

    // Fungsi untuk Android di bawah 10
    private fun downloadImageForBelowAndroid10(imageUrl: String) {
        val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(imageUrl))
            .setTitle("Gambar")
            .setDescription("Sedang mengunduh gambar")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "disabilitas_image.jpg")

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

    private fun deleteImageAnakYatimPiatuAsinkronCall(imageView: ImageView, imagePath: String?, imagePathKey: String) {
        // Menghapus gambar dari ImageView
        imageView.setImageResource(0)

        // Mengubah nilai path gambar menjadi null
        when (imagePathKey) {
            "kk" -> fotoKkUri = null
            "ktp" -> fotoKTPWaliPenganggungJawabUri = null
        }

        // Menyembunyikan tombol delete segera setelah gambar dihapus
        when (imagePathKey) {
            "ktp" -> deleteKTPWaliPenganggungJawab.visibility = View.GONE
            "kk" -> deleteFotoKk.visibility = View.GONE
        }

        // Jika ada path gambar (imagePath) yang tidak null, kirimkan untuk dihapus dari server
        if (!imagePath.isNullOrEmpty()) {
            val sessionManager = SessionManager(requireContext())
            val username = sessionManager.getUserSession()["username"] ?: return

            // Panggil API untuk menghapus gambar dari server
            deleteImageAnakYatimPiatuAsinkron(username, imagePath) { success ->
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
    private fun deleteImageAnakYatimPiatuAsinkron(username: String, imagePath: String, callback: (Boolean) -> Unit) {
        val call = RetrofitClient.apiServiceAnakYatimPiatu.deleteAnakYatimPiatuImage(username, imagePath)
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
