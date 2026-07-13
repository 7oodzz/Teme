package com.example.teme.ui.components

import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.teme.domain.model.RoomItem

@Composable
fun ShopScreen(
    currentCoins: Int,
    unlockedItems: List<RoomItem>, // This list actually contains all items mapped properly by the Repository
    onBuyItem: (String, Int) -> Unit,
    onToggleItem: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    Dialog(onDismissRequest = {
        view.playSoundEffect(SoundEffectConstants.CLICK)
        onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(0.dp), // Retro blocky look
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD7CCC8)) // Cozy warm grey
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SHOP",
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E342E)
                    )
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onDismiss() 
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close Shop", tint = Color(0xFF4E342E))
                    }
                }
                
                Text(
                    text = "COINS: $currentCoins",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFE65100),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Use unlockedItems directly since the repo maps the full SHOP_ITEMS correctly
                    items(unlockedItems) { item ->
                        ShopItemRow(
                            item = item,
                            canAfford = currentCoins >= item.price,
                            onBuyClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                onBuyItem(item.id, item.price) 
                            },
                            onToggleClick = { isActive -> 
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                onToggleItem(item.id, isActive) 
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShopItemRow(
    item: RoomItem,
    canAfford: Boolean,
    onBuyClick: () -> Unit,
    onToggleClick: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFEBE9), RoundedCornerShape(4.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFF4E342E))
            Text(text = item.description, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, color = Color(0xFF5D4037))
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        if (item.isUnlocked) {
            Button(
                onClick = { onToggleClick(!item.isActive) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (item.isActive) Color(0xFF81C784) else Color(0xFF9E9E9E)
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (item.isActive) "EQUIPPED" else "EQUIP", 
                    fontFamily = FontFamily.Monospace,
                    color = if (item.isActive) Color(0xFF1B5E20) else Color.White
                )
            }
        } else {
            Button(
                onClick = onBuyClick,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E342E)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(text = "${item.price} G", fontFamily = FontFamily.Monospace, color = Color.White)
            }
        }
    }
}
