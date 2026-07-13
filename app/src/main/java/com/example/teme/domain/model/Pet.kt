package com.example.teme.domain.model

data class Pet(
    val id: Int = 1, // Single pet
    val name: String = "Froggo",
    val level: Int = 1,
    val currentExp: Int = 0,
    val coins: Int = 0,
    val energy: Int = 100
) {
    val maxExpForNextLevel: Int
        get() = level * 100 // XP needed = Level * 100
}

enum class PetState {
    IDLE,
    FOCUSING,
    ASLEEP
}
