package com.example.beetles.network

import retrofit2.http.GET
import retrofit2.http.Query

interface CbrApi {
    @GET("xml_metall.asp")
    suspend fun getMetals(
        @Query("date_req1") from: String,
        @Query("date_req2") to: String
    ): MetallResponse
}
