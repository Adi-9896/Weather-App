package com.example.weather

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface apiinterface {

    @GET("weather")
    fun getWeather(
        @Query("lat") latitude: String,
        @Query("lon") lomngitude: String,
        @Query("appid") apikey: String
    ): Call<Modal>

    @GET("weather")
    fun getcityWeather(
        @Query("q") city: String, @Query("appid") apikey: String
    ): Call<Modal>
}