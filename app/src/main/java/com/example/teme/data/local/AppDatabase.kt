package com.example.teme.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.teme.data.local.dao.PetDao
import com.example.teme.data.local.dao.RoomItemDao
import com.example.teme.data.local.entity.PetEntity
import com.example.teme.data.local.entity.RoomItemEntity

@Database(entities = [PetEntity::class, RoomItemEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun roomItemDao(): RoomItemDao
}
