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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.teme.audio.ProceduralAudioPlayer
import com.example.teme.ui.FocusViewModel
import com.example.teme.ui.components.ConfettiParticleSystem
import com.example.teme.ui.components.PetCanvas
import com.example.teme.ui.components.ShopScreen
import com.example.teme.ui.components.TimerDisplay
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

    // Handle Audio state changes
    LaunchedEffect(uiState.isAudioPlaying) {
        if (uiState.isAudioPlaying) {
            audioPlayer.startPlaying()
        } else {
            audioPlayer.stopPlaying()
        }
    }

    // Shake effect for level up
    var shakeOffset by remember { mutableStateOf(0f) }
    LaunchedEffect(uiState.showLevelUpCelebration) {
        if (uiState.showLevelUpCelebration) {
            for (i in 0..10) {
                shakeOffset = if (i % 2 == 0) 10f else -10f
                delay(50)
            }
            shakeOffset = 0f
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFFFF8E1), // Cozy Cream background
        topBar = {
            TopAppBar(
                title = { Text("Focus Tamagotchi", fontWeight = FontWeight.Bold, color = Color(0xFF5D4037)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = { viewModel.toggleAudio() }) {
                        Icon(
                            imageVector = if (uiState.isAudioPlaying) Icons.Rounded.Close else Icons.Rounded.PlayArrow,
                            contentDescription = "Toggle Audio",
                            tint = Color(0xFF5D4037)
                        )
                    }
                    IconButton(onClick = { viewModel.toggleShop() }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Shop", tint = Color(0xFF5D4037))
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
                // Top: Room Canvas (Weight 1)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                ) {
                    PetCanvas(
                        petState = uiState.currentPetState,
                        unlockedItems = uiState.unlockedItems
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Middle: Timer
                val maxTimer = 25 * 60f
                TimerDisplay(
                    formattedTime = uiState.timerFormatted,
                    progress = 1f - (uiState.timerRemainingSeconds / maxTimer)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Bottom: Controls and Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { viewModel.toggleTimer() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAED581)),
                        modifier = Modifier.size(width = 120.dp, height = 50.dp)
                    ) {
                        Text(if (uiState.isTimerRunning) "Pause" else "Start", color = Color(0xFF33691E))
                    }

                    if (uiState.isTimerRunning) {
                        Button(
                            onClick = { viewModel.giveUp() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                            modifier = Modifier.size(width = 120.dp, height = 50.dp)
                        ) {
                            Text("Give Up", color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Status Bar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCC80))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Level: ${uiState.pet.level}", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            Text("Coins: ${uiState.pet.coins}", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val expProgress = uiState.pet.currentExp.toFloat() / uiState.pet.maxExpForNextLevel.toFloat()
                        LinearProgressIndicator(
                            progress = { expProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Color(0xFF4CAF50),
                            trackColor = Color(0xFFFFF3E0)
                        )
                        Text(
                            text = "Energy: ${uiState.pet.energy}/100",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Overlays
            if (uiState.showShop) {
                ShopScreen(
                    currentCoins = uiState.pet.coins,
                    unlockedItems = uiState.unlockedItems,
                    onBuyItem = { id, price -> viewModel.buyItem(id, price) },
                    onDismiss = { viewModel.toggleShop() }
                )
            }

            if (uiState.showLevelUpCelebration) {
                ConfettiParticleSystem()
                AlertDialog(
                    onDismissRequest = { viewModel.dismissLevelUp() },
                    title = { Text("Level Up!") },
                    text = { Text("Your pet reached Level ${uiState.pet.level}!") },
                    confirmButton = {
                        Button(onClick = { viewModel.dismissLevelUp() }) {
                            Text("Awesome")
                        }
                    }
                )
            }
        }
    }
}