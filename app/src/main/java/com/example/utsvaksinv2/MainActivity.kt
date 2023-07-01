package com.example.utsvaksinv2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utsvaksinv2.adapter.PendaftarAdapter
import com.example.utsvaksinv2.data.PendaftarVaksinDB
import com.example.utsvaksinv2.data.pendaftar.Pendaftar
import com.example.utsvaksinv2.databinding.ActivityMainBinding
import com.example.utsvaksinv2.pendaftar.AddPendaftarFragment
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val STORAGE_PERMISSION_CODE = 102
    private val TAG = "PERMISSION_TAG"

    lateinit var pendaftarRecyclerView: RecyclerView

    lateinit var pendaftarDB: PendaftarVaksinDB

    lateinit var pendaftarList: ArrayList<Pendaftar>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!checkPermission()) {
            requestPermission()
            Log.e("no permission", "no permission")
        }

        pendaftarDB = PendaftarVaksinDB(this@MainActivity)
        loadDataPendaftar()

        //Membuat Fungsi pada Button add Pasien yang ada di Main Activity
        binding.btnAddPendaftar.setOnClickListener {
            AddPendaftarFragment().show(supportFragmentManager, "newPendaftarTag")
        }

        swipeDelete()

        binding.txtSearchPendaftar.addTextChangedListener {
            val keyword: String = "%${binding.txtSearchPendaftar.text.toString()}%"
            if (keyword.count() > 2){
                searchDataPendaftar(keyword)
            }
            else{
                loadDataPendaftar()
            }
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

    fun loadDataPendaftar() {
        var layoutManager = LinearLayoutManager(this)
        pendaftarRecyclerView = binding.pendaftarListView
        pendaftarRecyclerView.layoutManager = layoutManager
        pendaftarRecyclerView.setHasFixedSize(true)

        lifecycleScope.launch {
            pendaftarList = pendaftarDB.getPendaftarDao().getAllPendaftar() as ArrayList<Pendaftar>
            Log.e("List Pendaftar", pendaftarDB.toString())
            pendaftarRecyclerView.adapter = PendaftarAdapter(pendaftarList)
        }
    }

    fun deletePendaftar(pendaftar: Pendaftar) {
        val builder =  AlertDialog.Builder(this@MainActivity)
        builder.setMessage("Apakah ${pendaftar.nama_pendaftar} ingin dihapus ?")
            .setCancelable(false)
            .setPositiveButton("Yes") {
                    dialog, id ->
                lifecycleScope.launch {
                    pendaftarDB.getPendaftarDao().deletePendaftar(pendaftar)
                    loadDataPendaftar()
                }

                val imagesDir = Environment.getExternalStoragePublicDirectory("")
                //Konversi dari dir string ke dir file
                val foto_delete = File(imagesDir, pendaftar.foto_pendaftar)

                if (foto_delete.exists()) {
                    // Foto ada di dalam galery
                    if (foto_delete.delete()){
                        //foto di delete
                        val toastDelete =  Toast.makeText(applicationContext, "file edit foto delete", Toast.LENGTH_LONG)
                        toastDelete.show()
                    }
                }
            }
            .setNegativeButton("No"){
                    dialog, id ->
                dialog.dismiss()
                loadDataPendaftar()
            }
        val alert = builder.create()
        alert.show()
    }

    fun swipeDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                lifecycleScope.launch{
                    pendaftarList = pendaftarDB.getPendaftarDao().getAllPendaftar() as ArrayList<Pendaftar>
                    Log.e("position swipe", pendaftarList[position].toString())
                    Log.e("position swipe", pendaftarList.size.toString())


                    deletePendaftar(pendaftarList[position])
                }
            }
        }).attachToRecyclerView(pendaftarRecyclerView)
    }

    fun searchDataPendaftar(keyword: String){
        var layoutManager = LinearLayoutManager(this)
        pendaftarRecyclerView = binding.pendaftarListView
        pendaftarRecyclerView.layoutManager = layoutManager
        pendaftarRecyclerView.setHasFixedSize(true)

        lifecycleScope.launch{
            pendaftarList = pendaftarDB.getPendaftarDao().searchPendaftar(keyword) as ArrayList<Pendaftar>
            Log.e("list pendaftar", pendaftarList.toString())
            pendaftarRecyclerView.adapter = PendaftarAdapter(pendaftarList)
        }
    }
}