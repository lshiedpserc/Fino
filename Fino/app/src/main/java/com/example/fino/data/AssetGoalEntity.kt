package com.example.fino.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asset_goal")
data class AssetGoalEntity(
    @PrimaryKey
    val id: Int = 1,
    val targetAmount: Long
)
