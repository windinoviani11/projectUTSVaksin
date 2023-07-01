package com.example.utsvaksinv2.data.pendaftar

import androidx.room.*

@Dao
interface PendaftarDao {
    @Query("SELECT * FROM pendaftar WHERE nama_pendaftar LIKE :namaPendaftar")
    suspend fun searchPendaftar(namaPendaftar: String) :List<Pendaftar>
    @Insert
    suspend fun addPendaftar(pendaftar: Pendaftar)

    @Update(entity = Pendaftar::class)
    suspend fun updatePendaftar(pendaftar: Pendaftar)

    @Delete
    suspend fun deletePendaftar(pendaftar: Pendaftar)

    @Query("SELECT * FROM pendaftar ORDER BY id DESC")
    suspend fun getAllPendaftar(): List<Pendaftar>
}