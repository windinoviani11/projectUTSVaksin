package com.example.utsvaksinv2.data.pendaftar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "pendaftar")
data class Pendaftar(
    @ColumnInfo(name = "nik") var nik: Long = 0,
    @ColumnInfo(name = "foto_pendaftar") var foto_pendaftar: String ="",
    @ColumnInfo(name = "nama_pendaftar") var nama_pendaftar: String ="",
    @ColumnInfo(name = "umur") var umur: Int = 0,
    @ColumnInfo(name = "jenis_kelamin") var jenis_kelamin: String ="",
    @ColumnInfo(name = "penyakit") var penyakit: String ="",
): Serializable {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}

