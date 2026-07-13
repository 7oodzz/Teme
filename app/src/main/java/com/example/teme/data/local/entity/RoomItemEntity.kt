package com.example.teme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlocked_items_table")
data class RoomItemEntity(
    @PrimaryKey val itemId: String,
    val isActive: Boolean = true // Default to true when newly unlocked
)
