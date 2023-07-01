package com.example.utsvaksinv2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.utsvaksinv2.data.pendaftar.Pendaftar
import com.example.utsvaksinv2.data.pendaftar.PendaftarDao

@Database(entities = [Pendaftar::class], version = 1)
abstract class PendaftarVaksinDB : RoomDatabase(){
    abstract fun getPendaftarDao() : PendaftarDao

    companion object{
        @Volatile
        private var instance: PendaftarVaksinDB? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDataBase(context).also{
                instance = it
            }
        }

        private fun buildDataBase(context: Context) = Room.databaseBuilder(context.applicationContext,
            PendaftarVaksinDB::class.java,
            "pendaftar-db"
        ).build()
    }
}