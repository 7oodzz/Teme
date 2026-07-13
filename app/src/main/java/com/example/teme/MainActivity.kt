package com.example.teme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.teme.audio.ProceduralAudioPlayer
import com.example.teme.ui.FocusViewModel
import com.example.teme.ui.SessionType
import com.example.teme.ui.components.*
import com.example.teme.ui.theme.TemeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val audioPlayer = ProceduralAudioPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TemeTheme {
                MainScreen(audioPlayer)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stopPlaying()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    audioPlayer: ProceduralAudioPlayer,
    viewModel: FocusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    // Handle Audio state changes
    LaunchedEffect(uiState.isAudioPlaying) {
        if (uiState.isAudioPlaying) audioPlayer.startPlaying() else audioPlayer.stopPlaying()
    }

    // Shake effect for level up
    var shakeOffset by remember { mutableStateOf(0f) }
    LaunchedEffect(uiState.showLevelUpCelebration) {
        if (uiState.showLevelUpCelebration) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            for (i in 0..10) {
                shakeOffset = if (i % 2 == 0) 10f else -10f
                delay(50)
            }
            shakeOffset = 0f
        }
    }

    // Subtle haptics during timer
    LaunchedEffect(uiState.timerRemainingSeconds) {
        if (uiState.isTimerRunning && uiState.timerRemainingSeconds % 60 == 0) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) // rhythmic tick every minute
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFEFEBE9), // Soft Slate/Cream
        topBar = {
            TopAppBar(
                title = { 
                    Text("Teme", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFF4E342E)) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = { viewModel.toggleTimerSettings() }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF4E342E))
                    }
                    IconButton(onClick = { viewModel.toggleAudio() }) {
                        Icon(
                            imageVector = if (uiState.isAudioPlaying) Icons.Rounded.Close else Icons.Rounded.PlayArrow,
                            contentDescription = "Toggle Audio",
                            tint = Color(0xFF4E342E)
                        )
                    }
                    IconButton(onClick = { viewModel.toggleShop() }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Shop", tint = Color(0xFF4E342E))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .offset(x = shakeOffset.dp, y = shakeOffset.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top: Dynamic Game World Canvas (Campfire + Pet + Background)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    DynamicGameWorldCanvas(
                        petState = uiState.currentPetState,
                        unlockedItems = uiState.unlockedItems,
                        timerProgress = uiState.timerProgress,
                        onPetTap = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.interactWithPet() 
                        }
                    )
                    
                    // Floating Indicators over the canvas
                    uiState.floatingMessages.forEach { msg ->
                        FloatingIndicator(
                            text = msg.text,
                            isPositive = msg.isPositive,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Numeric Timer Overlay
                    Text(
                        text = uiState.timerFormatted,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    )
                    
                    Text(
                        text = if (uiState.sessionType == SessionType.FOCUS) "FOCUS" else "BREAK",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.sessionType == SessionType.FOCUS) Color(0xFFFFCC80) else Color(0xFF81D4FA),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 70.dp)
                    )
                }

                // Middle: Dialogue Box
                if (uiState.currentDialogue != null) {
                    TypewriterText(
                        text = uiState.currentDialogue!!,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(64.dp))
                }

                // Bottom: Controls and Stats (Retro Style)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD7CCC8)),
                    shape = RoundedCornerShape(0.dp) // blocky
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("LVL: ${uiState.pet.level}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFF4E342E))
                            Text("COIN: ${uiState.pet.coins}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFF4E342E))
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("EXP", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFF4E342E))
                        val expProgress = uiState.pet.currentExp.toFloat() / uiState.pet.maxExpForNextLevel.toFloat()
                        RetroStatBar(
                            progress = expProgress,
                            fillColor = Color(0xFF81C784)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("NRG", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFF4E342E))
                        val energyProgress = uiState.pet.energy.toFloat() / 100f
                        RetroStatBar(
                            progress = energyProgress,
                            fillColor = Color(0xFF64B5F6)
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleTimer() 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E342E)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.size(width = 120.dp, height = 50.dp)
                    ) {
                        Text(
                            text = if (uiState.isTimerRunning) "PAUSE" else "START", 
                            fontFamily = FontFamily.Monospace, 
                            color = Color.White
                        )
                    }

                    if (uiState.isTimerRunning) {
                        Button(
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.giveUp() 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.size(width = 120.dp, height = 50.dp)
                        ) {
                            Text("GIVE UP", fontFamily = FontFamily.Monospace, color = Color.White)
                        }
                    }
                }
            }

            // Overlays
            if (uiState.showTimerSettings) {
                TimerSettingsDialog(
                    initialFocus = uiState.focusDurationMinutes,
                    initialBreak = uiState.breakDurationMinutes,
                    onSave = { f, b -> viewModel.updateTimerSettings(f, b) },
                    onDismiss = { viewModel.toggleTimerSettings() }
                )
            }

            if (uiState.showShop) {
                ShopScreen(
                    currentCoins = uiState.pet.coins,
                    unlockedItems = uiState.unlockedItems,
                    onBuyItem = { id, price -> viewModel.buyItem(id, price) },
                    onToggleItem = { id, isActive -> viewModel.toggleItemState(id, isActive) },
                    onDismiss = { viewModel.toggleShop() }
                )
            }

            if (uiState.showLevelUpCelebration) {
                ConfettiParticleSystem()
                AlertDialog(
                    onDismissRequest = { viewModel.dismissLevelUp() },
                    containerColor = Color(0xFFD7CCC8), // Cozy background
                    shape = RoundedCornerShape(4.dp), // Retro shape
                    title = { Text("LEVEL UP!", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFF4E342E)) },
                    text = { Text("Your pet reached Level ${uiState.pet.level}!", fontFamily = FontFamily.Monospace, color = Color(0xFF4E342E)) },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.dismissLevelUp() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E342E)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("AWESOME", fontFamily = FontFamily.Monospace, color = Color.White)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TimerSettingsDialog(
    initialFocus: Int,
    initialBreak: Int,
    onSave: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var focusTime by remember { mutableStateOf(initialFocus.toFloat()) }
    var breakTime by remember { mutableStateOf(initialBreak.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFD7CCC8), // Cozy background
        shape = RoundedCornerShape(4.dp), // Retro shape
        title = { Text("Timer Setup", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFF4E342E)) },
        text = {
            Column {
                Text("Focus Duration: ${focusTime.toInt()} mins", fontFamily = FontFamily.Monospace, color = Color(0xFF4E342E))
                Slider(
                    value = focusTime,
                    onValueChange = { focusTime = it },
                    valueRange = 5f..120f,
                    steps = 23,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF4E342E), activeTrackColor = Color(0xFF8D6E63))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Break Duration: ${breakTime.toInt()} mins", fontFamily = FontFamily.Monospace, color = Color(0xFF4E342E))
                Slider(
                    value = breakTime,
                    onValueChange = { breakTime = it },
                    valueRange = 1f..30f,
                    steps = 29,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF4E342E), activeTrackColor = Color(0xFF8D6E63))
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(focusTime.toInt(), breakTime.toInt()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E342E)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("SAVE", fontFamily = FontFamily.Monospace, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", fontFamily = FontFamily.Monospace, color = Color(0xFF4E342E))
            }
        }
    )
}