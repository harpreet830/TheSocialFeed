package com.thesocialfeed.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thesocialfeed.listeners.ServerResponse
import com.thesocialfeed.repository.FeedRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class FeedViewModel : ViewModel() {

    val GET_DATA_SETS = 100
    val onDataFeedResponse = MutableLiveData<JSONObject>()
    val onRetrofitError = MutableLiveData<Int>()


    fun getDataFeed(pageNo: Int,requestCode: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            FeedRepository.getDataFeedsFromMocky(
                serverResponse = object : ServerResponse {
                    override fun onSuccess(requestCode: Int, response: String) {
                        onDataFeedResponse.postValue(JSONObject(response))
                    }

                    override fun onError(requestCode: Int) {
                        onRetrofitError.postValue(requestCode)
                    }
                },
                requestCode = requestCode
           , pageNo = pageNo )
        }
    }

}