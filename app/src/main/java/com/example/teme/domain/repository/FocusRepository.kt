package com.example.teme.domain.repository

import com.example.teme.domain.model.Pet
import com.example.teme.domain.model.RoomItem
import kotlinx.coroutines.flow.Flow

interface FocusRepository {
    fun getPet(): Flow<Pet?>
    suspend fun savePet(pet: Pet)
    
    fun getUnlockedItems(): Flow<List<RoomItem>>
    suspend fun unlockItem(itemId: String)
    suspend fun toggleItemActiveState(itemId: String, isActive: Boolean)
}
