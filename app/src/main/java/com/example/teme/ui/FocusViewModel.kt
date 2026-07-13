package com.example.teme.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teme.domain.model.Pet
import com.example.teme.domain.model.PetState
import com.example.teme.domain.model.SHOP_ITEMS
import com.example.teme.domain.repository.FocusRepository
import com.example.teme.domain.usecase.CalculateExpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val repository: FocusRepository,
    private val calculateExpUseCase: CalculateExpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    private val petDialogues = listOf(
        "You're doing great!",
        "*comfy sigh*",
        "Keep going, adventurer!",
        "Almost there!",
        "Stay focused!",
        "Let's level up together!"
    )

    init {
        viewModelScope.launch {
            repository.getPet().collectLatest { pet ->
                if (pet == null) {
                    repository.savePet(Pet())
                } else {
                    _uiState.update { it.copy(pet = pet) }
                }
            }
        }

        viewModelScope.launch {
            repository.getUnlockedItems().collectLatest { items ->
                val actuallyUnlocked = items.filter { it.isUnlocked }
                if (actuallyUnlocked.isEmpty()) {
                    // Give the user 2 starting items automatically
                    repository.unlockItem(SHOP_ITEMS[0].id)
                    repository.unlockItem(SHOP_ITEMS[1].id)
                } else {
                    _uiState.update { it.copy(unlockedItems = items) }
                }
            }
        }
    }

    fun updateTimerSettings(focusMins: Int) {
        _uiState.update { 
            it.copy(
                focusDurationMinutes = focusMins,
                timerRemainingSeconds = focusMins * 60,
                showTimerSettings = false
            )
        }
    }

    fun toggleTimerSettings() {
        _uiState.update { it.copy(showTimerSettings = !it.showTimerSettings) }
    }

    fun toggleTimer() {
        if (_uiState.value.isTimerRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _uiState.update { 
            it.copy(
                isTimerRunning = true, 
                currentPetState = PetState.FOCUSING,
                currentDialogue = "Focus mode engaged!"
            )
        }
        
        timerJob = viewModelScope.launch {
            while (_uiState.value.timerRemainingSeconds > 0 && _uiState.value.isTimerRunning) {
                delay(1000L)
                _uiState.update { it.copy(timerRemainingSeconds = it.timerRemainingSeconds - 1) }
            }
            if (_uiState.value.timerRemainingSeconds <= 0 && _uiState.value.isTimerRunning) {
                completeSession()
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { 
            it.copy(
                isTimerRunning = false, 
                currentPetState = PetState.ASLEEP,
                currentDialogue = "*sleeping*"
            )
        }
        
        // Energy penalty on pause during focus
        viewModelScope.launch {
            val pet = _uiState.value.pet
            val newEnergy = (pet.energy - 5).coerceAtLeast(0)
            repository.savePet(pet.copy(energy = newEnergy))
        }
    }

    fun giveUp() {
        timerJob?.cancel()
        _uiState.update { 
            it.copy(
                isTimerRunning = false, 
                timerRemainingSeconds = it.focusDurationMinutes * 60,
                currentPetState = PetState.ASLEEP,
                currentDialogue = "We can try again later."
            )
        }
        
        viewModelScope.launch {
            val pet = _uiState.value.pet
            val newEnergy = (pet.energy - 20).coerceAtLeast(0)
            repository.savePet(pet.copy(energy = newEnergy))
        }
    }

    private fun completeSession() {
        _uiState.update { 
            it.copy(
                isTimerRunning = false,
                timerRemainingSeconds = it.focusDurationMinutes * 60,
                currentPetState = PetState.IDLE,
                currentDialogue = "Great job! Session complete."
            ) 
        }

        viewModelScope.launch {
            val result = calculateExpUseCase(_uiState.value.pet, _uiState.value.focusDurationMinutes)
            repository.savePet(result.updatedPet)
            
            // Show floating indicators
            showFloatingMessage("+${result.earnedCoins} Coins", true)
            delay(500)
            showFloatingMessage("+${_uiState.value.focusDurationMinutes * 10} XP", true)

            if (result.didLevelUp) {
                _uiState.update { it.copy(showLevelUpCelebration = true) }
                showFloatingMessage("LEVEL UP!", true)
            }
        }
    }

    fun interactWithPet() {
        val dialogue = petDialogues.random()
        _uiState.update { it.copy(currentDialogue = dialogue) }
        
        // Clear dialogue after a few seconds
        viewModelScope.launch {
            delay(4000)
            if (_uiState.value.currentDialogue == dialogue) {
                _uiState.update { it.copy(currentDialogue = null) }
            }
        }
    }

    private fun showFloatingMessage(text: String, isPositive: Boolean) {
        val msg = FloatingMessage(text = text, isPositive = isPositive)
        _uiState.update { it.copy(floatingMessages = it.floatingMessages + msg) }
        
        viewModelScope.launch {
            delay(2000) // Message visible duration
            _uiState.update { 
                it.copy(floatingMessages = it.floatingMessages.filter { m -> m.id != msg.id })
            }
        }
    }

    fun dismissLevelUp() {
        _uiState.update { it.copy(showLevelUpCelebration = false) }
    }

    fun toggleAudio() {
        _uiState.update { it.copy(isAudioPlaying = !it.isAudioPlaying) }
    }

    fun toggleShop() {
        _uiState.update { it.copy(showShop = !it.showShop) }
    }

    fun buyItem(itemId: String, price: Int) {
        val currentCoins = _uiState.value.pet.coins
        if (currentCoins >= price) {
            viewModelScope.launch {
                val updatedPet = _uiState.value.pet.copy(coins = currentCoins - price)
                repository.savePet(updatedPet)
                repository.unlockItem(itemId)
                showFloatingMessage("-$price Coins", false)
            }
        } else {
            _uiState.update { it.copy(currentDialogue = "Not enough coins...") }
        }
    }

    fun toggleItemState(itemId: String, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleItemActiveState(itemId, isActive)
        }
    }
}
