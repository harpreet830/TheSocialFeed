package com.thesocialfeed.repository

import com.google.gson.JsonElement
import com.thesocialfeed.listeners.ServerResponse
import com.thesocialfeed.rest.RetrofitClient
import retrofit2.Response
import retrofit2.awaitResponse

object FeedRepository {


    private var retrofitClient = RetrofitClient.restapiInterface

    suspend fun getDataFeedsFromMocky(
        serverResponse: ServerResponse,
        requestCode: Int, pageNo: Int
    ) {
        try {
            if (pageNo == 1) {
                val jsonResponse: Response<JsonElement> =
                    retrofitClient.getFirstFeedSet().awaitResponse()
                if (jsonResponse.isSuccessful) {
                    serverResponse.onSuccess(requestCode, jsonResponse.body().toString())
                } else {
                    serverResponse.onError(requestCode)
                }
            } else if (pageNo == 2) {
                val jsonResponse: Response<JsonElement> =
                    retrofitClient.getSecondFeedSet().awaitResponse()
                if (jsonResponse.isSuccessful) {
                    serverResponse.onSuccess(requestCode, jsonResponse.body().toString())
                } else {
                    serverResponse.onError(requestCode)
                }
            } else if (pageNo == 3) {
                val jsonResponse: Response<JsonElement> =
                    retrofitClient.getThirdFeedSet().awaitResponse()
                if (jsonResponse.isSuccessful) {
                    serverResponse.onSuccess(requestCode, jsonResponse.body().toString())
                } else {
                    serverResponse.onError(requestCode)
                }
            }
        } catch (e: Exception) {
            serverResponse.onError(requestCode)
        }
    }
}