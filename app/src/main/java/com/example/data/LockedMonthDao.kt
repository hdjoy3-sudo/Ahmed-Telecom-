package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LockedMonthDao {
    @Query("SELECT * FROM locked_months")
    fun getAllLockedMonths(): Flow<List<LockedMonthEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun lockMonth(month: LockedMonthEntity)

    @Delete
    suspend fun unlockMonth(month: LockedMonthEntity)
}
