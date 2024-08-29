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
import com.abtahiapp.dontworry.Secret
import com.abtahiapp.dontworry.WeatherResponse
import com.abtahiapp.dontworry.adapter.WeatherAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.content.pm.PackageManager
import android.Manifest

class WeatherActivity : AppCompatActivity() {

    private lateinit var weatherAdapter: WeatherAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var suggestionTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        val weatherRecyclerView: RecyclerView = findViewById(R.id.weather_recycler_view)
        suggestionTextView = findViewById(R.id.weather_suggestion)

        weatherRecyclerView.layoutManager = GridLayoutManager(this, 2)
        weatherAdapter = WeatherAdapter(this, mutableListOf())
        weatherRecyclerView.adapter = weatherAdapter

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocationAndFetchWeather()
    }

    private fun getCurrentLocationAndFetchWeather() {
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
                fetchWeatherForecast(location.latitude, location.longitude)
            } else {
                fetchWeatherForecast(22.3475, 91.8123) //Chittagong
            }
        }.addOnFailureListener {
            fetchWeatherForecast(22.3475, 91.8123) //Chittagong
        }
    }

    private fun fetchWeatherForecast(lat: Double, lon: Double) {
        val apiKey = Secret.OPEN_WEATHER_API_KEY

        RetrofitClient.weatherInstance.getWeatherForecast(lat = lat, lon = lon, apiKey = apiKey)
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    if (response.isSuccessful) {
                        val weatherList = response.body()?.list ?: emptyList()
                        weatherAdapter.updateWeather(weatherList)
                        if (weatherList.isNotEmpty()) {
                            val description = weatherList[0].weather[0].description
                            showWeatherSuggestion(description)
                        }
                    } else {
                        Toast.makeText(this@WeatherActivity, "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Toast.makeText(this@WeatherActivity, "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            fetchWeatherForecast(22.3475, 91.8123) //Chittagong
        }
    }
}