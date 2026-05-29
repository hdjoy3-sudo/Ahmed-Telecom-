package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locked_months")
data class LockedMonthEntity(
    @PrimaryKey val monthName: String
)
