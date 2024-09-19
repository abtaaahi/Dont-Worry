package com.abtahiapp.dontworry.repository

import com.abtahiapp.dontworry.utils.RetrofitClient
import com.abtahiapp.dontworry.utils.CurrentWeatherResponse
import com.abtahiapp.dontworry.utils.WeatherResponse

class WeatherRepository {

    private val weatherService = RetrofitClient.weatherInstance
    private val currentWeatherService = RetrofitClient.currentWeatherInstance

    suspend fun getWeatherForecast(lat: Double, lon: Double, apiKey: String): WeatherResponse {
        return weatherService.getWeatherForecast(lat, lon, apiKey = apiKey)
    }

    suspend fun getCurrentWeather(lat: Double, lon: Double, apiKey: String): CurrentWeatherResponse {
        return currentWeatherService.getCurrentWeather(lat, lon, apiKey = apiKey)
    }
}