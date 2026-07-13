package com.example.teme.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teme.domain.model.Pet
import com.example.teme.domain.model.PetState
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
    private val focusDurationMinutes = 25

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
                _uiState.update { it.copy(unlockedItems = items) }
            }
        }
    }

    fun toggleTimer() {
        if (_uiState.value.isTimerRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _uiState.update { it.copy(isTimerRunning = true, currentPetState = PetState.FOCUSING) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.timerRemainingSeconds > 0 && _uiState.value.isTimerRunning) {
                delay(1000L)
                _uiState.update { it.copy(timerRemainingSeconds = it.timerRemainingSeconds - 1) }
            }
            if (_uiState.value.timerRemainingSeconds == 0) {
                completeSession()
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isTimerRunning = false, currentPetState = PetState.ASLEEP) }
        
        // Energy penalty on pause/give up
        viewModelScope.launch {
            val pet = _uiState.value.pet
            val newEnergy = (pet.energy - 10).coerceAtLeast(0)
            repository.savePet(pet.copy(energy = newEnergy))
        }
    }

    fun giveUp() {
        timerJob?.cancel()
        _uiState.update { 
            it.copy(
                isTimerRunning = false, 
                timerRemainingSeconds = focusDurationMinutes * 60,
                currentPetState = PetState.ASLEEP
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
                currentPetState = PetState.IDLE,
                timerRemainingSeconds = focusDurationMinutes * 60
            ) 
        }

        viewModelScope.launch {
            val result = calculateExpUseCase(_uiState.value.pet, focusDurationMinutes)
            repository.savePet(result.updatedPet)

            if (result.didLevelUp) {
                _uiState.update { it.copy(showLevelUpCelebration = true) }
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
            }
        }
    }
}
