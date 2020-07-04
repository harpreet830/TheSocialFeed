package com.thesocialfeed.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(FeedItemModel::class), version = 1, exportSchema = false)
abstract class DatabaseHelper : RoomDatabase() {

    abstract fun feedDao(): FeedDao

    companion object {
        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        fun getDatabase(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DatabaseHelper::class.java,
                    "socialfeed"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }


    }
}