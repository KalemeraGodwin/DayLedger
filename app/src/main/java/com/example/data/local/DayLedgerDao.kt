package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DayLedgerDao {

    @Query("SELECT * FROM hour_blocks WHERE date = :date ORDER BY hour ASC")
    fun getHourBlocksForDate(date: String): Flow<List<HourBlockEntity>>

    @Query("SELECT * FROM hour_blocks WHERE date = :date ORDER BY hour ASC")
    suspend fun getHourBlocksForDateOnce(date: String): List<HourBlockEntity>

    @Query("SELECT * FROM day_plans WHERE date = :date LIMIT 1")
    fun getDayPlan(date: String): Flow<DayPlanEntity?>

    @Query("SELECT * FROM day_plans WHERE date = :date LIMIT 1")
    suspend fun getDayPlanOnce(date: String): DayPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDayPlan(plan: DayPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourBlocks(blocks: List<HourBlockEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHourBlock(block: HourBlockEntity)

    @Update
    suspend fun updateHourBlock(block: HourBlockEntity)

    @Query("UPDATE hour_blocks SET isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE hour_blocks SET reviewLog = :reviewLog, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateReviewLog(id: Long, reviewLog: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM hour_blocks WHERE id = :id")
    suspend fun deleteHourBlock(id: Long)

    @Query("DELETE FROM hour_blocks WHERE date = :date")
    suspend fun deleteHourBlocksForDate(date: String)

    @Transaction
    suspend fun replaceBlocksForDate(date: String, blocks: List<HourBlockEntity>) {
        deleteHourBlocksForDate(date)
        insertHourBlocks(blocks)
    }

    @Query("SELECT * FROM hour_blocks WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC, hour ASC")
    fun getHourBlocksInRange(startDate: String, endDate: String): Flow<List<HourBlockEntity>>

    @Query("SELECT DISTINCT date FROM hour_blocks ORDER BY date DESC")
    fun getAllDatesWithBlocks(): Flow<List<String>>

    @Query("SELECT * FROM hour_blocks")
    suspend fun getAllHourBlocks(): List<HourBlockEntity>
}
