package com.dldmswo1209.calculator.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.dldmswo1209.calculator.model.History

// room 에 연결된 Dao
// History Entity 를 어떻게 사용할 것인지...(가져오기, 추가, 삭제...)
@Dao
interface HistoryDao{
    @Query("SELECT * FROM history")
    fun getAll(): List<History>

    @Insert
    fun insertHistory(history: History)

    @Query("Delete From history")
    fun deleteAll()

}