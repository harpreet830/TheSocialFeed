package com.thesocialfeed.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "feed_item")
data class FeedItemModel(
    @ColumnInfo(name = "id") var id: String?,
    @ColumnInfo(name = "thumbnail_image") var thumbnail_image: String,
    @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB)
    var image: ByteArray,
    @ColumnInfo(name = "event_name") var event_name: String,
    @PrimaryKey @ColumnInfo(name = "event_date") var event_date: Long,
    @ColumnInfo(name = "views") var views: Int,
    @ColumnInfo(name = "likes") var likes: Int,
    @ColumnInfo(name = "shares") var shares: Int)