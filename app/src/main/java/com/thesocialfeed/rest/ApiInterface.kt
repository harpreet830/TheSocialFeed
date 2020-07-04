package co.khati.oyedriverpartner.rest

/**
 * Created by Harpreet on 2019-10-17.
 */

 import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

 @GET("59b3f0b0100000e30b236b7e")
 fun getFirstFeedSet(): Call<JsonElement>

 @GET("59ac28a9100000ce0bf9c236")
 fun getSecondFeedSet(): Call<JsonElement>

 @GET("59ac293b100000d60bf9c239")
 fun getThirdFeedSet(): Call<JsonElement>


}

