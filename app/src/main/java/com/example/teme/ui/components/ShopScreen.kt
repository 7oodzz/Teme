package com.example.teme.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.teme.domain.model.RoomItem
import com.example.teme.domain.model.SHOP_ITEMS

@Composable
fun ShopScreen(
    currentCoins: Int,
    unlockedItems: List<RoomItem>,
    onBuyItem: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        text = "Shop",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close Shop")
                    }
                }
                
                Text(
                    text = "Coins: $currentCoins",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val mergedItems = SHOP_ITEMS.map { shopItem ->
                        val isUnlocked = unlockedItems.any { it.id == shopItem.id }
                        shopItem.copy(isUnlocked = isUnlocked)
                    }

                    items(mergedItems) { item ->
                        ShopItemRow(
                            item = item,
                            canAfford = currentCoins >= item.price,
                            onBuyClick = { onBuyItem(item.id, item.price) }
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
    onBuyClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, fontWeight = FontWeight.Bold)
            Text(text = item.description, style = MaterialTheme.typography.bodySmall)
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        if (item.isUnlocked) {
            Text(text = "Owned", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        } else {
            Button(
                onClick = onBuyClick,
                enabled = canAfford
            ) {
                Text(text = "${item.price} Coins")
            }
        }
    }
}
