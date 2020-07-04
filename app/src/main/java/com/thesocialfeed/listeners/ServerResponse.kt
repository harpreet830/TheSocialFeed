package com.thesocialfeed.listeners

interface ServerResponse {

    fun onSuccess(requestCode : Int, response: String)
    fun onError(requestCode: Int)
}