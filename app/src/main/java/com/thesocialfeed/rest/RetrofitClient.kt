package com.thesocialfeed.rest

 import co.khati.oyedriverpartner.rest.ApiInterface
 import com.thesocialfeed.BuildConfig
 import com.thesocialfeed.BuildConfig.BASE_URL
 import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * Created by Harpreet on 2019-10-17.
 */
 object RetrofitClient {

    val restapiInterface: ApiInterface by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient().build())
            .build()

        // Create Retrofit client
        return@lazy retrofit.create(ApiInterface::class.java)
    }

    private fun httpClient(): OkHttpClient.Builder {
        val httpClient = OkHttpClient.Builder()
        httpClient.connectTimeout(10, TimeUnit.SECONDS)
        httpClient.readTimeout(10, TimeUnit.SECONDS)
        httpClient.writeTimeout(10, TimeUnit.SECONDS)
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        httpClient.interceptors().add(interceptor)
        return httpClient
    }

}