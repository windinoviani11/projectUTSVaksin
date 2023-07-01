package com.example.utsvaksinv2.pendaftar

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.utsvaksinv2.MainActivity
import com.example.utsvaksinv2.data.PendaftarVaksinDB
import com.example.utsvaksinv2.data.pendaftar.Pendaftar
import com.example.utsvaksinv2.databinding.ActivityEditPendaftarBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class EditPendaftarActivity : AppCompatActivity()  {
    private var _binding: ActivityEditPendaftarBinding? = null
    private val binding get() = _binding!!

    private val REQ_CAM = 101
    private var dataGambar: Bitmap? = null
    private var old_foto_dir = ""
    private var new_foto_dir = ""

    private var id_pendaftar: Int = 0

    lateinit var pendaftarDB : PendaftarVaksinDB
    private val STORAGE_PERMISSION_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityEditPendaftarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pendaftarDB = PendaftarVaksinDB.invoke(this@EditPendaftarActivity)

        val intent = intent
        binding.TxtEditNamaPendaftar.setText(intent.getStringExtra("nama_pendaftar").toString())
        binding.TxtEditNik.setText(intent.getStringExtra("nik_pendaftar").toString())
        binding.TxtEditUmur.setText(intent.getStringExtra("umur_pendaftar").toString())
        binding.TxtEditJenisKelamin.setText(intent.getStringExtra("Jenis_kelamin").toString())
        binding.TxtEditPenyakit.setText(intent.getStringExtra("riwayat_penyakit").toString())

        id_pendaftar = intent.getStringExtra("id").toString().toInt()
        old_foto_dir = intent.getStringExtra("foto_pendaftar").toString()

        Log.e("foto_edit", old_foto_dir)

        val imgFile = File("${Environment.getExternalStorageDirectory()}/${old_foto_dir}")
        val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
        binding.BtnImgPendaftar.setImageBitmap(myBitmap)

        if(!checkPermission()){
            requestPermission()
            Log.e("no permission", "no permission")
        }

        binding.BtnImgPendaftar.setOnClickListener {
            openCamera()
        }

        binding.BtnEditPendaftar.setOnClickListener {
            editPendaftar()
        }
    }

    private fun checkPermission() : Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Environment.isExternalStorageManager()
        }
        else{
            val write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    //Method untuk request permission untuk access storage jika tidak diizinkan
    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                val  intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
            }
            catch (e: Exception){
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            }
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        }
    }

    fun saveMediaToStorage(bitmap: Bitmap):String {
        val filename = "${System.currentTimeMillis()}.jpg"

        var fos: OutputStream? = null

        var image_save = ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                fos = imageUri?.let{resolver.openOutputStream(it)}

                image_save = "${Environment.DIRECTORY_PICTURES}/${filename}"
            }
        }
        else {
            val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

            if(permission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
            }

            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)

            image_save = "${Environment.DIRECTORY_PICTURES}/${filename}"
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        return image_save
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQ_CAM && resultCode == RESULT_OK){
            dataGambar = data?.extras?.get("data") as Bitmap

            val image_save_uri: String = saveMediaToStorage(dataGambar!!)
            new_foto_dir = image_save_uri
            binding.BtnImgPendaftar.setImageBitmap(dataGambar)
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            this.packageManager?.let {
                intent?.resolveActivity(it).also {
                    startActivityForResult(intent, REQ_CAM)
                }
            }
        }
    }

    private fun editPendaftar() {
        val nama_pendaftar = binding.TxtEditNamaPendaftar.text.toString()
        val nik= binding.TxtEditNik.text.toString().toLong()
        val umur = binding.TxtEditUmur.text.toString().toInt()
        val jenis_kelamin = binding.TxtEditJenisKelamin.text.toString()
        val penyakit = binding.TxtEditPenyakit.text.toString()
        var foto_final_dir : String = old_foto_dir


        Log.e("edit_nama", nama_pendaftar)

        if(new_foto_dir != ""){
            Log.e("foto_baru", new_foto_dir)
            foto_final_dir = new_foto_dir
            val imagesDir = Environment.getExternalStoragePublicDirectory("")

            val old_foto_delete = File(imagesDir, old_foto_dir)

            if(old_foto_delete.exists()){
                if (old_foto_delete.delete()){
                    Log.e("foto final",foto_final_dir)
                }
            }
        }
        else {
            foto_final_dir = old_foto_dir
        }


        lifecycleScope.launch{
            val pendaftar = Pendaftar(nik, foto_final_dir, nama_pendaftar, umur, jenis_kelamin, penyakit)
            pendaftar.id = id_pendaftar
            pendaftarDB.getPendaftarDao().updatePendaftar(pendaftar)
        }

        val intentPendaftar = Intent(this, MainActivity::class.java)
        startActivity(intentPendaftar)
    }
}