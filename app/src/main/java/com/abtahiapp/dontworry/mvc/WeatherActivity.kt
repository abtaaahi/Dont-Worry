package com.abtahiapp.dontworry.mvc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.RetrofitClient
import com.abtahiapp.dontworry.adapter.WeatherAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.content.pm.PackageManager
import android.Manifest
import android.view.View
import android.widget.ProgressBar
import com.abtahiapp.dontworry.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/// MVC Design Pattern
class WeatherActivity : AppCompatActivity() {

    private lateinit var weatherAdapter: WeatherAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var suggestionTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var weatherRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        weatherRecyclerView = findViewById(R.id.weather_recycler_view)
        suggestionTextView = findViewById(R.id.weather_suggestion)
        progressBar = findViewById(R.id.progress_bar)

        weatherRecyclerView.layoutManager = GridLayoutManager(this, 2)
        weatherAdapter = WeatherAdapter(this, mutableListOf())
        weatherRecyclerView.adapter = weatherAdapter

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        showLoading(true)
        getCurrentLocationAndFetchWeather()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            weatherRecyclerView.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            weatherRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun getCurrentLocationAndFetchWeather() {
        showLoading(true)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                fetchWeatherData(location.latitude, location.longitude)
            } else {
                fetchWeatherData(22.3475, 91.8123) //Chittagong
            }
        }.addOnFailureListener {
            fetchWeatherData(22.3475, 91.8123) //Chittagong
        }
    }

    private fun fetchWeatherData(lat: Double, lon: Double) {
        val apiKey = BuildConfig.OPEN_WEATHER_API_KEY

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val weatherForecast = RetrofitClient.weatherInstance.getWeatherForecast(lat, lon, apiKey = apiKey)
                val currentWeather = RetrofitClient.currentWeatherInstance.getCurrentWeather(lat, lon, apiKey = apiKey)

                withContext(Dispatchers.Main) {
                    weatherAdapter.updateWeather(weatherForecast.list)
                    showWeatherSuggestion(currentWeather.weather.firstOrNull()?.main.orEmpty())
                    showLoading(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@WeatherActivity, "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showWeatherSuggestion(description: String) {
        val suggestion = when {
            description.contains("rain", ignoreCase = true) -> "Don't go out, it's raining."
            description.contains("overcast", ignoreCase = true) || description.contains("clouds", ignoreCase = true) -> "Don't forget to take umbrella."
            description.contains("sunny", ignoreCase = true) -> "Don't forget to use sunscreen."
            description.contains("haze", ignoreCase = true) || description.contains("mist", ignoreCase = true) -> "Wear some warm clothes."
            else -> "Stay safe and have a great day!"
        }
        suggestionTextView.text = suggestion
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationAndFetchWeather()
        } else {
            showLoading(false)
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            fetchWeatherData(22.3475, 91.8123) //Chittagong
        }
    }
}
