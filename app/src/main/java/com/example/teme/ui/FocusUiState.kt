package com.example.teme.ui

import com.example.teme.domain.model.Pet
import com.example.teme.domain.model.PetState
import com.example.teme.domain.model.RoomItem

data class FocusUiState(
    val pet: Pet = Pet(),
    val unlockedItems: List<RoomItem> = emptyList(),
    val timerRemainingSeconds: Int = 25 * 60,
    val isTimerRunning: Boolean = false,
    val currentPetState: PetState = PetState.IDLE,
    val showLevelUpCelebration: Boolean = false,
    val isAudioPlaying: Boolean = false,
    val showShop: Boolean = false
) {
    val timerFormatted: String
        get() {
            val minutes = timerRemainingSeconds / 60
            val seconds = timerRemainingSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}
