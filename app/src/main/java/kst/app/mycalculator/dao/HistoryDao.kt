package kst.app.mycalculator.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kst.app.mycalculator.model.History

@Dao
interface HistoryDao{

    @Query(value = "SELECT * FROM history")
    fun getAll() : List<History>

    @Insert
    fun insertHistory(history: History)

    @Query(value = "DELETE FROM history")
    fun deleteAll()

/*    @Delete
    fun delete(history: History)

    @Query(value = "SELECT * FROM history WHERE result LIKE :result LIMIT 1")
    fun  findByResult(result: String)*/
}