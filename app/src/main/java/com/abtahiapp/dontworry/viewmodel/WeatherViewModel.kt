package com.abtahiapp.dontworry.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abtahiapp.dontworry.WeatherForecast
import com.abtahiapp.dontworry.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _weatherForecast = MutableLiveData<List<WeatherForecast>>()
    val weatherForecast: LiveData<List<WeatherForecast>> get() = _weatherForecast

    private val _currentWeather = MutableLiveData<String>()
    val currentWeather: LiveData<String> get() = _currentWeather

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun fetchWeatherData(lat: Double, lon: Double, apiKey: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val weatherForecastResponse = repository.getWeatherForecast(lat, lon, apiKey)
                val currentWeatherResponse = repository.getCurrentWeather(lat, lon, apiKey)

                _weatherForecast.value = weatherForecastResponse.list
                _currentWeather.value = currentWeatherResponse.weather.firstOrNull()?.main.orEmpty()

            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}
