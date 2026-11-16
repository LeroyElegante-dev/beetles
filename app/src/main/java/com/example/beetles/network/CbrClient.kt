package com.example.beetles.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.util.concurrent.TimeUnit

object CbrClient {
    private const val BASE_URL = "https://www.cbr.ru/scripts/"

    val api: CbrApi by lazy {
        val okHttp = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(SimpleXmlConverterFactory.createNonStrict())
            .build()
            .create(CbrApi::class.java)
    }
}
