package com.dldmswo1209.calculator

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dldmswo1209.calculator.dao.HistoryDao
import com.dldmswo1209.calculator.model.History

// db 생성
@Database(entities = [History::class], version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun historyDao(): HistoryDao
}