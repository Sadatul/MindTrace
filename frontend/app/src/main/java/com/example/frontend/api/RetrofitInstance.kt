package com.example.frontend.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val dementiaAPI: DementiaAPI by lazy {
        Retrofit.Builder()
            .baseUrl("https://mindtrace.pinklifeline.xyz/")
            //.baseUrl("http://10.0.2.2:8080") // For local testing with Android emulator
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DementiaAPI::class.java)
    }
}