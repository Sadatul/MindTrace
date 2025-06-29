package com.example.frontend.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val dementiaAPI: DementiaAPI by lazy {
        Retrofit.Builder()
            .baseUrl("https://mindtrace.pinklifeline.xyz/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DementiaAPI::class.java)
    }
}