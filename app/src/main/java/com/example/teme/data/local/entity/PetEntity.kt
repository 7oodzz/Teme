package com.example.teme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet_table")
data class PetEntity(
    @PrimaryKey val id: Int = 1, // Only one pet
    val name: String,
    val level: Int,
    val currentExp: Int,
    val coins: Int,
    val energy: Int
)
