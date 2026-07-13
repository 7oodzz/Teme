package com.example.teme.data.repository

import com.example.teme.data.local.dao.PetDao
import com.example.teme.data.local.dao.RoomItemDao
import com.example.teme.data.local.entity.PetEntity
import com.example.teme.data.local.entity.RoomItemEntity
import com.example.teme.domain.model.Pet
import com.example.teme.domain.model.RoomItem
import com.example.teme.domain.model.SHOP_ITEMS
import com.example.teme.domain.repository.FocusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FocusRepositoryImpl @Inject constructor(
    private val petDao: PetDao,
    private val roomItemDao: RoomItemDao
) : FocusRepository {

    override fun getPet(): Flow<Pet?> {
        return petDao.getPet().map { entity ->
            entity?.let {
                Pet(
                    id = it.id,
                    name = it.name,
                    level = it.level,
                    currentExp = it.currentExp,
                    coins = it.coins,
                    energy = it.energy
                )
            }
        }
    }

    override suspend fun savePet(pet: Pet) {
        petDao.savePet(
            PetEntity(
                id = pet.id,
                name = pet.name,
                level = pet.level,
                currentExp = pet.currentExp,
                coins = pet.coins,
                energy = pet.energy
            )
        )
    }

    override fun getUnlockedItems(): Flow<List<RoomItem>> {
        return roomItemDao.getUnlockedItems().map { unlockedEntities ->
            val unlockedIds = unlockedEntities.map { it.itemId }.toSet()
            SHOP_ITEMS.map { item ->
                item.copy(isUnlocked = unlockedIds.contains(item.id))
            }
        }
    }

    override suspend fun unlockItem(itemId: String) {
        roomItemDao.unlockItem(RoomItemEntity(itemId))
    }
}
