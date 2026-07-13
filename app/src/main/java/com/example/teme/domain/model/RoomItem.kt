package com.example.teme.domain.model

data class RoomItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val isUnlocked: Boolean = false,
    val type: ItemType
)

enum class ItemType {
    PLANT, RUG, LAMP, POSTER
}

// Pre-defined shop items available
val SHOP_ITEMS = listOf(
    RoomItem(id = "item_plant_1", name = "Tiny Cactus", description = "A cute little cactus.", price = 50, type = ItemType.PLANT),
    RoomItem(id = "item_rug_1", name = "Cozy Rug", description = "A soft, pastel rug.", price = 100, type = ItemType.RUG),
    RoomItem(id = "item_lamp_1", name = "Desk Lamp", description = "A warm, glowing lamp.", price = 150, type = ItemType.LAMP),
    RoomItem(id = "item_poster_1", name = "Retro Poster", description = "An 8-bit landscape poster.", price = 80, type = ItemType.POSTER)
)
