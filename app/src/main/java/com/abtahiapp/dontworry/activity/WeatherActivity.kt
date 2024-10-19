package com.abtahiapp.dontworry.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.BuildConfig
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.adapter.WeatherAdapter
import com.abtahiapp.dontworry.utils.InfoBottomSheetDialog
import com.abtahiapp.dontworry.viewmodel.WeatherViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class WeatherActivity : AppCompatActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()
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

        val backButton: ImageButton = findViewById(R.id.back)
        backButton.setOnClickListener {
            onBackPressed()
        }

        val infoButton = findViewById<ImageButton>(R.id.infoButton)
        infoButton.setOnClickListener {

            val infoMessage = getString(R.string.weather_info_message).trimIndent()

            val infoBottomSheetDialog = InfoBottomSheetDialog(this, infoMessage)
            infoBottomSheetDialog.show()
        }

        weatherRecyclerView.layoutManager = GridLayoutManager(this, 2)
        weatherAdapter = WeatherAdapter(this, mutableListOf())
        weatherRecyclerView.adapter = weatherAdapter

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupObservers()
        getCurrentLocationAndFetchWeather()
    }

    private fun setupObservers() {
        weatherViewModel.weatherForecast.observe(this, Observer { weatherList ->
            weatherAdapter.updateWeather(weatherList)
        })

        weatherViewModel.currentWeather.observe(this, Observer { description ->
            showWeatherSuggestion(description)
        })

        weatherViewModel.isLoading.observe(this, Observer { isLoading ->
            showLoading(isLoading)
        })
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
                weatherViewModel.fetchWeatherData(location.latitude, location.longitude, BuildConfig.OPEN_WEATHER_API_KEY)
            } else {
                weatherViewModel.fetchWeatherData(22.3475, 91.8123, BuildConfig.OPEN_WEATHER_API_KEY) //Chittagong
            }
        }.addOnFailureListener {
            weatherViewModel.fetchWeatherData(22.3475, 91.8123, BuildConfig.OPEN_WEATHER_API_KEY) //Chittagong
        }
    }

    private fun showWeatherSuggestion(description: String) {
        val suggestion = when {
            description.contains("rain", ignoreCase = true) -> "Don't go out, it's raining."
            description.contains("overcast", ignoreCase = true) || description.contains("clouds", ignoreCase = true) -> "Don't forget to take umbrella."
            description.contains("sunny", ignoreCase = true) -> "Don't forget to use sunscreen."
            description.contains("haze", ignoreCase = true) || description.contains("mist", ignoreCase = true) -> "Stay Refreshed."
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
            weatherViewModel.fetchWeatherData(22.3475, 91.8123, BuildConfig.OPEN_WEATHER_API_KEY) //Chittagong
        }
    }
}