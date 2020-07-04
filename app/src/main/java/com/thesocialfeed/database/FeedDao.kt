package com.thesocialfeed.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
@Dao
interface FeedDao {

    @Query("SELECT * from feed_item")
    fun getAllFeedItems(): List<FeedItemModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookingAction: FeedItemModel)

}