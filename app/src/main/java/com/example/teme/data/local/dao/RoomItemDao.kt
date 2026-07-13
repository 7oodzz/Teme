package com.example.teme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.teme.data.local.entity.RoomItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomItemDao {
    @Query("SELECT * FROM unlocked_items_table")
    fun getUnlockedItems(): Flow<List<RoomItemEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockItem(item: RoomItemEntity)

    @Query("UPDATE unlocked_items_table SET isActive = :isActive WHERE itemId = :itemId")
    suspend fun updateItemActiveState(itemId: String, isActive: Boolean)
}
