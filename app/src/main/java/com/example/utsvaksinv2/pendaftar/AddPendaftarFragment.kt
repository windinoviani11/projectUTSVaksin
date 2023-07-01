package com.example.utsvaksinv2.pendaftar

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.utsvaksinv2.MainActivity
import com.example.utsvaksinv2.data.PendaftarVaksinDB
import com.example.utsvaksinv2.data.pendaftar.Pendaftar
import com.example.utsvaksinv2.databinding.FragmentAddPendaftarBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class AddPendaftarFragment : BottomSheetDialogFragment(){
    private var _binding: FragmentAddPendaftarBinding? = null
    private val binding get() = _binding!!

    private val REQ_CAM = 100
    private var dataGambar: Bitmap? = null
    private var saved_image_url: String = ""

    private val STORAGE_PERMISSION_CODE = 102
    private val TAG = "PERMISSION_TAG"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddPendaftarBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun addPendaftar() {
        val nama_pendaftar = binding.TxtNamaPendaftar.text.toString()
        val nik = binding.TxtNik.text.toString().toLong()
        val jenis_kelamin = binding.TxtJenisKelamin.text.toString()
        val umur = binding.TxtUmur.text.toString().toInt()
        val riwayat_penyakit = binding.TxtPenyakit.text.toString()

        lifecycleScope.launch {
            val pendaftar = Pendaftar(nik, saved_image_url, nama_pendaftar, umur, jenis_kelamin, riwayat_penyakit)
            PendaftarVaksinDB(requireContext()).getPendaftarDao().addPendaftar(pendaftar)
        }
        dismiss()
    }

    fun saveMediaToStorage(bitmap: Bitmap):String {
        val filename = "${System.currentTimeMillis()}.jpg"

        var fos: OutputStream? = null

        var image_save = ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.contentResolver?.also { resolver ->
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
            val permission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)

            if(permission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
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
        if(requestCode == REQ_CAM && resultCode == AppCompatActivity.RESULT_OK){
            dataGambar = data?.extras?.get("data") as Bitmap

            val image_save_uri: String = saveMediaToStorage(dataGambar!!)
            binding.BtnImgPendaftar.setImageBitmap(dataGambar)
            saved_image_url = image_save_uri
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            this.activity?.packageManager?.let {
                intent?.resolveActivity(it).also {
                    startActivityForResult(intent, REQ_CAM)
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        (activity as MainActivity?)?.loadDataPendaftar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()

        binding.BtnImgPendaftar.setOnClickListener{
            openCamera()
        }
        binding.BtnAddPendaftar.setOnClickListener{
            if(saved_image_url != "") {
                addPendaftar()
            }
        }
    }

}