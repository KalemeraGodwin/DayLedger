package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DayPlanEntity::class, HourBlockEntity::class],
    version = 3,
    exportSchema = false
)
abstract class DayLedgerDatabase : RoomDatabase() {

    abstract fun dayLedgerDao(): DayLedgerDao

    companion object {
        @Volatile
        private var INSTANCE: DayLedgerDatabase? = null

        fun getInstance(context: Context): DayLedgerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DayLedgerDatabase::class.java,
                    "day_ledger.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
