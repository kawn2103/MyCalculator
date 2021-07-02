package kst.app.mycalculator

import androidx.room.Database
import androidx.room.RoomDatabase
import kst.app.mycalculator.dao.HistoryDao
import kst.app.mycalculator.model.History

@Database(entities = [History:: class], version = 1)
abstract  class AppDatabase: RoomDatabase(){
    abstract  fun historyDao(): HistoryDao
}