package com.example.teme.ui

import com.example.teme.domain.model.Pet
import com.example.teme.domain.model.PetState
import com.example.teme.domain.model.RoomItem
import java.util.UUID

enum class SessionType {
    FOCUS, BREAK
}

data class FloatingMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isPositive: Boolean = true
)

data class FocusUiState(
    val pet: Pet = Pet(),
    val unlockedItems: List<RoomItem> = emptyList(),
    
    // Timer configurations
    val focusDurationMinutes: Int = 25,
    val breakDurationMinutes: Int = 5,
    val timerRemainingSeconds: Int = 25 * 60,
    val isTimerRunning: Boolean = false,
    val sessionType: SessionType = SessionType.FOCUS,
    
    val currentPetState: PetState = PetState.IDLE,
    val currentDialogue: String? = null,
    
    val showLevelUpCelebration: Boolean = false,
    val isAudioPlaying: Boolean = false,
    val showShop: Boolean = false,
    val showTimerSettings: Boolean = false,
    
    val floatingMessages: List<FloatingMessage> = emptyList()
) {
    val timerFormatted: String
        get() {
            val minutes = timerRemainingSeconds / 60
            val seconds = timerRemainingSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
        
    val timerProgress: Float
        get() {
            val maxSeconds = if (sessionType == SessionType.FOCUS) focusDurationMinutes * 60 else breakDurationMinutes * 60
            if (maxSeconds == 0) return 0f
            return 1f - (timerRemainingSeconds.toFloat() / maxSeconds.toFloat())
        }
}
