package com.example.utsvaksinv2.adapter

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.utsvaksinv2.R
import com.example.utsvaksinv2.data.pendaftar.Pendaftar
import com.example.utsvaksinv2.pendaftar.EditPendaftarActivity
import java.io.File

class PendaftarAdapter (private val pendaftarList: ArrayList<Pendaftar>) : RecyclerView.Adapter<PendaftarAdapter.PendaftarViewHolder>(){
    class PendaftarViewHolder (pendaftarItemView: View) : RecyclerView.ViewHolder(pendaftarItemView){
        val nama_pendaftar : TextView = pendaftarItemView.findViewById(R.id.TVLNamaPendaftar)
        val nik_pendaftar : TextView = pendaftarItemView.findViewById(R.id.TVLNik)
        val umur_pendaftar : TextView = pendaftarItemView.findViewById(R.id.TVLUmur)
        val jenis_kelamin : TextView = pendaftarItemView.findViewById(R.id.TVLJenisKelamin)
        val riwayat_penyakit : TextView = pendaftarItemView.findViewById(R.id.TVLPenyakit)

        val img_pendaftar : ImageView =itemView.findViewById(R.id.IMLFotoPendaftar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendaftarViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.pendaftar_list_layout, parent, false)
        return PendaftarViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return pendaftarList.size
    }

    override fun onBindViewHolder(holder: PendaftarViewHolder, position: Int) {
        val currentItem = pendaftarList[position]
        val foto_dir = currentItem.foto_pendaftar.toString()
        val imgFile = File("${Environment.getExternalStorageDirectory()}/${foto_dir}")
        val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)

        holder.img_pendaftar.setImageBitmap(myBitmap)
        holder.nama_pendaftar.text = currentItem.nama_pendaftar.toString()
        holder.nik_pendaftar.text = currentItem.nik.toString()
        holder.umur_pendaftar.text = currentItem.umur.toString()
        holder.jenis_kelamin.text = currentItem.jenis_kelamin.toString()
        holder.riwayat_penyakit.text = currentItem.penyakit.toString()

        holder.itemView.setOnClickListener{
            Log.e("test_rv", currentItem.nama_pendaftar.toString())
            Log.e("tess", "tess")
            val activity = it.context as AppCompatActivity
            activity.startActivity(Intent(activity, EditPendaftarActivity::class.java).apply {
                putExtra("nik_pendaftar", currentItem.nik.toString())
                putExtra("foto_pendaftar", currentItem.foto_pendaftar.toString())
                putExtra("nama_pendaftar", currentItem.nama_pendaftar.toString())
                putExtra("umur_pendaftar", currentItem.umur.toString())
                putExtra("Jenis_kelamin", currentItem.jenis_kelamin.toString())
                putExtra("riwayat_penyakit", currentItem.penyakit.toString())
                putExtra("id",currentItem.id.toString())
            })
        }
    }
}