package com.example.weather

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Retrofitinstance {
    val retrofit  = Retrofit.Builder().baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create()).build()


     val json  =  retrofit.create(apiinterface::class.java)
}