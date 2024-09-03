package com.abtahiapp.dontworry.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.RetrofitClient
import com.abtahiapp.dontworry.WeatherResponse
import com.abtahiapp.dontworry.adapter.WeatherAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.content.pm.PackageManager
import android.Manifest
import android.view.View
import android.widget.ProgressBar
import com.abtahiapp.dontworry.BuildConfig
import com.abtahiapp.dontworry.CurrentWeatherResponse

class WeatherActivity : AppCompatActivity() {

    private lateinit var weatherAdapter: WeatherAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var suggestionTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var weatherRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        weatherRecyclerView= findViewById(R.id.weather_recycler_view)
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
                fetchCurrentWeather(location.latitude, location.longitude)
                fetchWeatherForecast(location.latitude, location.longitude)
            } else {
                fetchWeatherForecast(22.3475, 91.8123) //Chittagong
                fetchCurrentWeather(22.3475, 91.8123)
            }
        }.addOnFailureListener {
            fetchWeatherForecast(22.3475, 91.8123) //Chittagong
            fetchCurrentWeather(22.3475, 91.8123)
        }
    }

    private fun fetchWeatherForecast(lat: Double, lon: Double) {
        val apiKey = BuildConfig.OPEN_WEATHER_API_KEY

        RetrofitClient.weatherInstance.getWeatherForecast(lat = lat, lon = lon, apiKey = apiKey)
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    if (response.isSuccessful) {
                        val weatherList = response.body()?.list ?: emptyList()
                        weatherAdapter.updateWeather(weatherList)
                        showLoading(false)
                    } else {
                        showLoading(false)
                        Toast.makeText(this@WeatherActivity, "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(this@WeatherActivity, "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchCurrentWeather(lat: Double, lon: Double) {
        val apiKey = BuildConfig.OPEN_WEATHER_API_KEY

        RetrofitClient.currentWeatherInstance.getCurrentWeather(lat = lat, lon = lon, apiKey = apiKey)
            .enqueue(object : Callback<CurrentWeatherResponse> {
                override fun onResponse(call: Call<CurrentWeatherResponse>, response: Response<CurrentWeatherResponse>) {
                    if (response.isSuccessful) {
                        val currentWeather = response.body()?.weather?.firstOrNull()
                        currentWeather?.let {
                            showWeatherSuggestion(it.main)
                            Toast.makeText(this@WeatherActivity, "Current Weather: ${it.main}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@WeatherActivity, "Failed to fetch current weather data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CurrentWeatherResponse>, t: Throwable) {
                    Toast.makeText(this@WeatherActivity, "Failed to fetch current weather data", Toast.LENGTH_SHORT).show()
                }
            })
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
            fetchWeatherForecast(22.3475, 91.8123) //Chittagong
        }
    }
}