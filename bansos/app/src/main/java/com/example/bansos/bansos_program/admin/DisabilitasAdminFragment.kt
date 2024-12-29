package com.example.bansos.bansos_program.admin


import ApiResponseDisabilitasAdmin
import SessionManager
import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText

import android.widget.ImageView
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.ImageLoader
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.example.bansos.R
import com.example.bansos.bansos_program.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DisabilitasAdminFragment : Fragment() {

    private var lastSelectedKalurahan: String? = null



    private lateinit var namaLengkap: TextView
    private lateinit var alamat: TextView
    private lateinit var noKK: TextView
    private lateinit var nik: TextView
    private lateinit var tanggalUsulan: TextView
    private lateinit var statusSpinner: Spinner
    private lateinit var komentar: EditText

    // Mengganti nama variabel gambar dengan 'thumb'
    private lateinit var thumbKk: ImageView
    private lateinit var thumbKtp: ImageView

    private lateinit var komentarInput: EditText

    private val PICK_IMAGE_REQUEST = 1

    private var selectedImageType: String? = null

    private var fotoKkUri: Uri? = null
    private var fotoKtpUri: Uri? = null

    private var kalurahan: String? = null
    private  lateinit var btnExport:Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.disabilitas_admin, container, false)


        // Data untuk Spinner Kalurahan
        val kalurahanList = arrayOf("Hargorejo", "Hargomulyo", "Hargotiro", "Hargowilis", "Kalirejo")

        // Membuat adapter untuk Spinner
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

        // Set drop-down view resource untuk Spinner
        kalurahanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Menetapkan adapter ke Spinner
        val spinnerKalurahan = view.findViewById<Spinner>(R.id.kalurahanSelect)
        spinnerKalurahan.adapter = kalurahanAdapter

        // Menangani item selection pada Spinner
        spinnerKalurahan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Mendapatkan item yang dipilih
                val selectedKalurahan = parentView.getItemAtPosition(position).toString()

                // Log untuk melihat posisi dan kalurahan yang dipilih
                Log.d("SpinnerKalurahan", "Item dipilih pada posisi: $position, Kalurahan: $selectedKalurahan")

                // Pastikan fragment hanya dimuat jika kalurahan yang dipilih berbeda dari yang terakhir dipilih
                if (selectedKalurahan != lastSelectedKalurahan) {
                    // Log untuk membandingkan kalurahan yang dipilih dengan yang terakhir dipilih
                    Log.d("SpinnerKalurahan", "Kalurahan yang dipilih berbeda dengan kalurahan terakhir yang dipilih.")

                    // Update kalurahan yang terakhir dipilih
                    lastSelectedKalurahan = selectedKalurahan

                    // Mengirimkan kalurahan yang dipilih ke fragment berikutnya (jika diperlukan)
                    Log.d("SpinnerKalurahan", "Mengirim kalurahan: $lastSelectedKalurahan ke fragment berikutnya.")
                    fetchDataFromServer(lastSelectedKalurahan!!)
                } else {
                    // Log jika kalurahan yang dipilih sama dengan kalurahan terakhir
                    Log.d("SpinnerKalurahan", "Kalurahan yang dipilih sama dengan kalurahan terakhir yang dipilih. Tidak mengirim data.")
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Log untuk menangani kondisi tidak ada item yang dipilih
                Log.d("SpinnerKalurahan", "Tidak ada kalurahan yang dipilih.")
            }
        }

        // SwipeRefreshLayout untuk memuat ulang data
        val swipeRefreshLayout: SwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            // Memuat ulang data dari server (sesuai kebutuhan)
            if (lastSelectedKalurahan != null) {
                // Contoh panggilan fungsi menggunakan kalurahan yang dipilih
                fetchDataFromServer(lastSelectedKalurahan!!)
            }
            swipeRefreshLayout.isRefreshing = false // Hentikan animasi refresh
        }

        // Menyiapkan elemen-elemen UI lainnya
        initializeUIElements(view)
        btnExport.setOnClickListener {
            // Membuat daftar kalurahan dan ID-nya
            val kalurahanList = arrayOf(
                "Hargorejo", "Hargomulyo", "Hargotiro", "Hargowilis", "Kalirejo"
            )

            val kalurahanIdMap = mapOf(
                "Hargorejo" to 1,
                "Hargomulyo" to 2,
                "Hargotiro" to 3,
                "Hargowilis" to 4,
                "Kalirejo" to 5
            )

            // Membuat dialog pemilihan kalurahan
            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Pilih Kalurahan")
            builder.setItems(kalurahanList) { _, which ->
                // Mendapatkan nama kalurahan yang dipilih
                val selectedKalurahan = kalurahanList[which]

                // Mengambil ID kalurahan dari map
                val selectedKalurahanId = kalurahanIdMap[selectedKalurahan]

                // Log untuk memastikan ID kalurahan yang dipilih
                Log.d("KalurahanSelection", "Kalurahan yang dipilih: $selectedKalurahan, ID: $selectedKalurahanId")

                // Jika ID kalurahan ditemukan
                if (selectedKalurahanId != null) {
                    // Tentukan URL untuk mengunduh file Excel sesuai dengan ID kalurahan
                    val fileUrl = "http://192.168.46.100:5000/disabilitasadminmobiletoexcel/$selectedKalurahanId"

                    // Log URL yang akan digunakan untuk mengunduh file
                    Log.d("KalurahanSelection", "URL untuk mengunduh: $fileUrl")

                    // Membuka file Excel menggunakan browser atau aplikasi pengelola file
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(fileUrl)
                    startActivity(intent)

                } else {
                    // Menampilkan pesan jika kalurahan belum dipilih atau tidak ditemukan di map
                    Toast.makeText(requireContext(), "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
                    Log.e("KalurahanSelection", "Terjadi kesalahan, ID kalurahan tidak ditemukan.")
                }
            }

            // Menampilkan dialog
            builder.show()
        }
        setupSpinner()

        // Memuat data session pengguna
        populateSessionData()

        return view
    }



        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Panggil fetchDataFromServer tanpa parameter
        val selectedStatus = arguments?.getString("selectedStatus") // Pastikan key sesuai

//        fetchDataFromServer(selectedStatus.toString())
    }



    private fun initializeUIElements(view: View) {
        // Binding views
        namaLengkap = view.findViewById(R.id.dataNamaLengkap)
        alamat = view.findViewById(R.id.dataAlamat)
        noKK = view.findViewById(R.id.dataNoKK)
        komentar = view.findViewById(R.id.dataKomentar)
        nik = view.findViewById(R.id.dataNIK)
        tanggalUsulan = view.findViewById(R.id.dataTanggalUsulan)
        statusSpinner = view.findViewById(R.id.dataStatusSpinner)
        thumbKk = view.findViewById(R.id.dataFotoKartuKeluarga)
        thumbKtp = view.findViewById(R.id.dataFotoKTP)
        komentarInput = view.findViewById(R.id.dataKomentar)
        btnExport=view.findViewById(R.id.btnExport)
    }


    private fun fetchDataFromServer(kalurahanParameter: String) {
        val call = RetrofitClient.apiServiceDisabilitasAdmin.getdisabititasDataAdmin(kalurahanParameter)
        call.enqueue(object : Callback<ApiResponseDisabilitasAdmin> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<ApiResponseDisabilitasAdmin>, response: Response<ApiResponseDisabilitasAdmin>) {
                Log.d("onResponse", "Response received from server")
                val tableLayout = view?.findViewById<TableLayout>(R.id.TableLayout)

                // Bersihkan TableLayout sebelum menambahkan data baru
                tableLayout?.removeAllViews()

                if (response.isSuccessful && response.body() != null) {
                    val dataList = response.body()?.data

                    if (dataList.isNullOrEmpty()) {
                        // Tampilkan baris kosong jika data tidak ada
                        val tableRow = createEmptyTableRow()
                        tableLayout?.addView(tableRow)
                        Toast.makeText(requireContext(), "Tidak ada data ditemukan untuk kalurahan tersebut", Toast.LENGTH_SHORT).show()
                    } else {
                        // Tambahkan header tabel
                        val headerRow = createTableHeaderRow()
                        tableLayout?.addView(headerRow)

                        // Tambahkan baris data
                        for (data in dataList) {
                            val tableRow = TableRow(requireContext())
                            tableRow.setPadding(8, 8, 8, 8)

                            // Kolom data
                            tableRow.addView(createTextView(data.nama_lengkap ?: "Data Kosong"))
                            tableRow.addView(createTextView(data.alamat ?: "Data Kosong"))
                            tableRow.addView(createTextView(data.no_kk ?: "Data Kosong"))
                            tableRow.addView(createTextView(data.nik ?: "Data Kosong"))
                            tableRow.addView(createTextView(data.tingkat_disabilitas ?: "Data Kosong"))

                            // Format tanggal
                            val formattedTanggal = formatTanggal(data.tanggal_usulan ?: "Data Kosong")
                            tableRow.addView(createTextView(formattedTanggal))

                            // Gambar
                            addImageToTableRow(tableRow, data.ktp_image_path ?: "Data Kosong")
                            addImageToTableRow(tableRow, data.kk_image_path ?: "Data Kosong")

                            // Spinner Status
                            tableRow.addView(createStatusSpinner(data.status, data.username.toString()))

                            // EditText Komentar dengan debounce
                            val komentarInput = createEditView(data.komentar ?: "Data Kosong")
                            komentarInput.addTextChangedListener(object : TextWatcher {
                                private var debounceHandler: Handler? = null
                                private val debounceDelay: Long = 1000 // 1 detik debounce

                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                    debounceHandler?.removeCallbacksAndMessages(null)
                                    debounceHandler = Handler(Looper.getMainLooper()).apply {
                                        postDelayed({
                                            val komentar = s.toString()
                                            if (komentar.isNotEmpty()) {
                                                sendKomentarToServer(data.username.toString(), komentar)
                                            }
                                        }, debounceDelay)
                                    }
                                }

                                override fun afterTextChanged(s: Editable?) {}
                            })
                            tableRow.addView(komentarInput)

                            // Tambahkan baris ke TableLayout
                            tableLayout?.addView(tableRow)
                        }
                    }
                } else {
                    showErrorTableLayout("Gagal memuat data disabilitas")
                }
            }

            override fun onFailure(call: Call<ApiResponseDisabilitasAdmin>, t: Throwable) {
                Log.e("onFailure", "Error occurred: ${t.message}")
                showErrorTableLayout("Terjadi kesalahan: ${t.message}")
            }
        })
    }

    private fun createEmptyTableRow(): TableRow {
        val tableRow = TableRow(requireContext())
        val headers = listOf(
            "Nama Lengkap", "Alamat", "Nomor KK", "NIK", "Tingkat Disabilitas",
            "Tanggal Usulan", "Foto KTP", "Foto Kartu Keluarga", "Status", "Komentar"
        )
        headers.forEach { headerText -> tableRow.addView(createTextView(headerText)) }
        return tableRow
    }

    private fun createTableHeaderRow(): TableRow {
        val headerRow = TableRow(requireContext())
        val headers = listOf(
            "Nama Lengkap", "Alamat", "Nomor KK", "NIK", "Tingkat Disabilitas",
            "Tanggal Usulan", "Foto KTP", "Foto Kartu Keluarga", "Status", "Komentar"
        )
        headers.forEach { headerText -> headerRow.addView(createHeaderTextView(headerText)) }
        return headerRow
    }

    private fun showErrorTableLayout(message: String) {
        val tableLayout = view?.findViewById<TableLayout>(R.id.TableLayout)
        tableLayout?.removeAllViews()
        val emptyRow = createEmptyTableRow()
        tableLayout?.addView(emptyRow)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }





    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatTanggal(tanggal: String): String {
        return try {
            // Parse tanggal dalam format ISO (2024-12-11T08:13:51.000Z)
            val instant = Instant.parse(tanggal)

            // Format menjadi HH:mm:ss dd-MM-yyyy
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy")
                .withZone(ZoneId.systemDefault())

            formatter.format(instant)
        } catch (e: Exception) {
            "Data Kosong"
        }
    }
    // Fungsi untuk membuat TextView
    private fun createTextView(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            setPadding(8, 8, 8, 8)
            gravity = Gravity.CENTER
            setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white)) // Latar belakang putih
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black)) // Teks hitam
        }
    }

    private fun createEditView(value: String): EditText {
        val editText = EditText(requireContext())
        editText.setText(value) // Set initial value

        // Jika ada data (nilai tidak kosong), buat EditText bisa diedit
        if (value.isEmpty()) {
            // Jika kosong, biarkan tidak bisa diedit
            editText.isFocusable = true
            editText.isFocusableInTouchMode = true
        } else {
            // Jika ada data, biarkan bisa diedit
            editText.isFocusable = true
            editText.isFocusableInTouchMode = true
        }

        // Menggunakan latar belakang sesuai tema atau warna tabel
        editText.setBackgroundColor(Color.WHITE) // Atau menggunakan `Color.TRANSPARENT` jika kamu menginginkan latar belakang transparan

        // Mengatur warna teks menjadi hitam
        editText.setTextColor(Color.BLACK)

        editText.setPadding(8, 8, 8, 8) // Menambahkan padding agar lebih rapi
        return editText
    }



    // Fungsi untuk membuat Header TextView
    private fun createHeaderTextView(headerText: String): TextView {
        return TextView(requireContext()).apply {
            text = headerText
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD) // Membuat teks tebal
            setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white)) // Latar belakang putih
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black)) // Teks hitam
        }
    }

    private fun createStatusSpinner(currentStatus: String?, username: String): Spinner {
        val spinner = Spinner(requireContext())
        val statusOptions = listOf(
            "Belum Diverifikasi Admin",
            "Sudah Diverifikasi Admin dan akan Diproses ke Muskal",
            "Disetujui di Musyawarah Kalurahan",
            "Ditolak di Musyawarah Kalurahan"
        ) // Sesuaikan dengan status yang relevan

        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statusOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinner.adapter = adapter

        // Ubah latar belakang Spinner menjadi putih dan teks menjadi hitam
        spinner.background = ContextCompat.getDrawable(requireContext(), android.R.color.white)

        // Set status default
        val selectedIndex = statusOptions.indexOf(currentStatus ?: "Belum Diverifikasi Admin")
        if (selectedIndex >= 0) {
            spinner.setSelection(selectedIndex)
        }

        // Event listener untuk status yang dipilih
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Ubah warna teks item yang dipilih menjadi hitam
                (view as? TextView)?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                val selectedStatus = statusOptions[position]
                Log.d("Spinner", "Status selected: $selectedStatus")

                // Mengirim status yang dipilih ke server dengan username
                sendStatusToServer(username, selectedStatus)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Tidak ada aksi jika tidak ada yang dipilih
            }
        }

        return spinner
    }




    // Mengirim status ke server
// Mengirim status ke server
    private fun sendStatusToServer(username: String, status: String) {
        val statusUrl = "http://192.168.46.100:5000/disabilitasadminmobile/$username/status"
        Log.d("SendStatus", "URL: $statusUrl")

        // Membuat form body (bukan JSON)
        val statusBody = FormBody.Builder()
            .add("status", status)
            .build()

        // Membuat request dengan method PUT
        val statusRequest = Request.Builder()
            .url(statusUrl)
            .put(statusBody)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        val client = OkHttpClient()

        // Menjalankan request dengan coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(statusRequest).execute()

                // Memastikan request berhasil
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("SendStatus", "Response: $responseBody")

                    // Mengambil pesan dari body respons server
                    val jsonResponse = JSONObject(responseBody)
                    val message = jsonResponse.getString("message")

                    // Menampilkan toast di thread utama
                    withContext(Dispatchers.Main) {
                        showToast(message)
                    }
                } else {
                    // Menangani error respons dari server
                    withContext(Dispatchers.Main) {
                        showToast("Error: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                // Menangani error seperti kegagalan koneksi
                withContext(Dispatchers.Main) {
                    showToast("Gagal menghubungi server: ${e.message}")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Log.d("SendStatus", "Toast Message: $message")
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun sendKomentarToServer(username: String, komentar: String) {
        val komentarUrl = "http://192.168.46.100:5000/disabilitasadminmobile/$username/komentar"
        Log.d("SendKomentar", "URL: $komentarUrl")

        // Membuat form body (bukan JSON)
        val komentarBody = FormBody.Builder()
            .add("komentar", komentar)
            .build()

        // Membuat request dengan method PUT
        val komentarRequest = Request.Builder()
            .url(komentarUrl)
            .put(komentarBody)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        val client = OkHttpClient()

        // Menjalankan request dengan coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(komentarRequest).execute()

                // Memastikan request berhasil
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("SendKomentar", "Response: $responseBody")

                    // Mengambil pesan dari body respons server
                    val jsonResponse = JSONObject(responseBody)
                    val message = jsonResponse.getString("message")

                    // Menampilkan toast di thread utama
                    withContext(Dispatchers.Main) {
                        showToast(message)
                    }
                } else {
                    // Menangani error respons dari server
                    withContext(Dispatchers.Main) {
                        showToast("Error: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                // Menangani error seperti kegagalan koneksi
                withContext(Dispatchers.Main) {
                    showToast("Gagal menghubungi server: ${e.message}")
                }
            }
        }
    }











    private fun populateSessionData() {

        val sessionManager = SessionManager(requireContext())

        val userSession = sessionManager.getUserSession()

        val nikFromSession = userSession["nik"]

        if (nikFromSession.isNullOrEmpty()) {

            Toast.makeText(requireContext(), "Session tidak valid, silakan login kembali", Toast.LENGTH_SHORT).show()

        } else {

            nik.text = nikFromSession

        }

    }

    // Fungsi untuk menambahkan gambar ke dalam TableRow
    private fun addImageToTableRow(tableRow: TableRow, imagePath: String?) {
        val imageView = ImageView(requireContext())

        // Cek apakah path gambar tersedia
        if (!imagePath.isNullOrEmpty()) {
            val imageBaseUrl = "http://192.168.46.100:5000/users/disabilitas/uploads/"
            val imageUrl = "$imageBaseUrl$imagePath"

            // Muat gambar menggunakan Glide
            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.table_row_background) // Gambar placeholder (opsional)
                .error(R.drawable.table_row_background) // Gambar error (opsional)
                .into(imageView)

            // Set ukuran gambar di dalam TableRow
            val params = TableRow.LayoutParams(150, 150) // Sesuaikan ukuran gambar
            imageView.layoutParams = params

            // Tambahkan aksi klik untuk memperbesar gambar
            imageView.setOnClickListener {
                showImageDialog(imageUrl)
            }

            // Tambahkan aksi long klik untuk mengunduh gambar
            imageView.setOnLongClickListener {
                downloadImage(imageUrl)
                true
            }
        } else {
            // Jika path gambar kosong, gunakan placeholder
            imageView.setImageResource(R.drawable.ic_launcher_background)
            val params = TableRow.LayoutParams(150, 150)
            imageView.layoutParams = params
        }

        // Tambahkan ImageView ke TableRow
        tableRow.addView(imageView)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {

            val uri = data?.data

            uri?.let {

                when (selectedImageType) {



                    "kk" -> {

                        fotoKkUri = it

                        loadImageIntoView(it, thumbKk)

                    }

                    "ktp" -> {

                        fotoKtpUri = it

                        loadImageIntoView(it, thumbKtp)

                    }

                }

            } ?: run {

                Log.e("onActivityResult", "Failed to get image URI")

            }

        }

    }


    private fun loadImageIntoView(uri: Uri, imageView: ImageView) {

        val imageUrl = uri.toString()

        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {

            Glide.with(requireContext()).load(imageUrl).into(imageView)

        } else {

            Glide.with(requireContext()).load(uri).into(imageView)

        }

    }


    private fun setupSpinner() {

        val statusList = arrayOf(

            "Belum Diverifikasi Admin",

            "Sudah Diverifikasi Admin dan akan Diproses ke Muskal",

            "Disetujui di Musyawarah Kalurahan",

            "Ditolak di Musyawarah Kalurahan"

        )
        val statusAdapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, statusList) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

                val view = super.getView(position, convertView, parent)

                val textView = view as TextView

                view.setPadding(20, 20, 20, 20)

                textView.setTextColor(Color.BLACK)

                return view

            }

        }

        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        statusSpinner.adapter = statusAdapter
    }


    fun getRealPathFromURI(uri: Uri): String {

        val sessionManager = SessionManager(requireContext())

        val username = sessionManager.getUserSession()["username"] ?: "unknown"

        val timestamp = System.currentTimeMillis()

        val fileName = getFileNameFromURI(uri)

        val newFileName = "${timestamp}-${username}-${fileName}"

        val storageDirectory = requireContext().cacheDir

        val tempFile = File(storageDirectory, newFileName)

        val inputStream = requireContext().contentResolver.openInputStream(uri)

        val outputStream = FileOutputStream(tempFile)

        inputStream?.copyTo(outputStream)

        inputStream?.close()

        outputStream.close()

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


    private fun showImageDialog(imageUrl: String) {

        val dialog = Dialog(requireContext())

        dialog.setContentView(R.layout.dialog_image_view)

        val layoutParams = dialog.window?.attributes

        layoutParams?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()

        layoutParams?.height = (resources.displayMetrics.heightPixels * 0.7).toInt()

        dialog.window?.attributes = layoutParams

        val imageView: SubsamplingScaleImageView = dialog.findViewById(R.id.imageView)

        Log.d("Image URL", "URL to load: $imageUrl")

        val imageLoader = ImageLoader(requireContext())

        val request = ImageRequest.Builder(requireContext())

            .data(imageUrl)

            .target(object : coil.target.Target {

                override fun onStart(placeholder: Drawable?) {}

                override fun onSuccess(result: Drawable) {

                    val bitmap = (result as BitmapDrawable).bitmap

                    imageView.setImage(ImageSource.bitmap(bitmap))

                    imageView.setScaleAndCenter(1.0f, PointF(0f, 0f))

                    imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE)

                }

                override fun onError(error: Drawable?) {}

            })

            .build()

        imageLoader.enqueue(request)

        val closeButton: Button = dialog.findViewById(R.id.closeButton)

        closeButton.setOnClickListener { closeDialog(dialog) }

        val downloadButton: Button = dialog.findViewById(R.id.downloadButton)

        downloadButton.setOnClickListener { downloadImage(imageUrl) }

        dialog.show()

    }


    private fun downloadImage(imageUrl: String) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

            downloadImageForAndroid10Plus(imageUrl)

        } else {

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

            } else {

                downloadImageForBelowAndroid10(imageUrl)

            }

        }

    }


    private fun downloadImageForAndroid10Plus(imageUrl: String) {

        val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val request = DownloadManager.Request(Uri.parse(imageUrl))

            .setTitle("Gambar BPNT")

            .setDescription("Sedang mengunduh gambar BPNT")

            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "bpnt_image.jpg")

        downloadManager.enqueue(request)

        Toast.makeText(requireContext(), "Gambar sedang diunduh", Toast.LENGTH_SHORT).show()

    }


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


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(requireContext(), "Izin diberikan, coba unduh ulang gambar.", Toast.LENGTH_SHORT).show()

        } else {

            Toast.makeText(requireContext(), "Izin diperlukan untuk menyimpan gambar.", Toast.LENGTH_SHORT).show()

        }

    }


    private fun closeDialog(dialog: Dialog) {

        dialog.dismiss() // Menutup dialog secara langsung

    }







}
