package com.thesocialfeed.model

data class FeedModel(val id:String, val thumbnail_image:String, val event_name:String,
                     val event_date:Long, val views:Int, val likes:Int, val shares:Int)