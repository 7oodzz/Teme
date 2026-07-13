package com.example.teme.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.teme.data.local.dao.PetDao
import com.example.teme.data.local.dao.RoomItemDao
import com.example.teme.data.local.entity.PetEntity
import com.example.teme.data.local.entity.RoomItemEntity

@Database(entities = [PetEntity::class, RoomItemEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun roomItemDao(): RoomItemDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add the isActive column to unlocked_items_table, default to 1 (true)
                db.execSQL("ALTER TABLE unlocked_items_table ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
            }
        }
    }
}
