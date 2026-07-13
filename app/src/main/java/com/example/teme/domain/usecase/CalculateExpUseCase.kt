package com.example.teme.domain.usecase

import com.example.teme.domain.model.Pet
import javax.inject.Inject

class CalculateExpUseCase @Inject constructor() {
    
    data class Result(
        val updatedPet: Pet,
        val didLevelUp: Boolean,
        val earnedCoins: Int
    )

    operator fun invoke(pet: Pet, focusDurationMinutes: Int): Result {
        // Base formula: 10 XP per minute of focus
        val earnedExp = focusDurationMinutes * 10
        
        // Base formula: 2 coins per minute of focus
        val earnedCoins = focusDurationMinutes * 2

        var newExp = pet.currentExp + earnedExp
        var newLevel = pet.level
        var newCoins = pet.coins + earnedCoins
        var didLevelUp = false

        var maxExpForNext = newLevel * 100

        while (newExp >= maxExpForNext) {
            newExp -= maxExpForNext
            newLevel++
            didLevelUp = true
            maxExpForNext = newLevel * 100
        }

        val updatedPet = pet.copy(
            level = newLevel,
            currentExp = newExp,
            coins = newCoins
        )

        return Result(
            updatedPet = updatedPet,
            didLevelUp = didLevelUp,
            earnedCoins = earnedCoins
        )
    }
}
