package com.abtahiapp.dontworry.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.QuoteResponse
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.RetrofitClient
import com.abtahiapp.dontworry.WeatherResponse
import com.abtahiapp.dontworry.adapter.WeatherAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherQuoteFragment : Fragment() {

    private lateinit var weatherAdapter: WeatherAdapter
    private lateinit var quoteTextView: TextView
    private lateinit var authorTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weather_quote, container, false)
        val weatherRecyclerView: RecyclerView = view.findViewById(R.id.weather_recycler_view)
        quoteTextView = view.findViewById(R.id.quoteTextView)
        authorTextView = view.findViewById(R.id.authorTextView)

        weatherRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        weatherAdapter = WeatherAdapter(requireContext(), mutableListOf())
        weatherRecyclerView.adapter = weatherAdapter

        fetchWeatherForecast()
        fetchAndDisplayQuote()

        return view
    }

        private fun fetchWeatherForecast() {
        val chittagongLat = 22.3475
        val chittagongLon = 91.8123
        val apiKey = "80034495d58fc3db348f75354755f6e6"

        RetrofitClient.weatherInstance.getWeatherForecast(lat = chittagongLat, lon = chittagongLon, apiKey = apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherList = response.body()?.list ?: emptyList()
                    weatherAdapter.updateWeather(weatherList)
                } else {
                }
            }
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchAndDisplayQuote() {
        RetrofitClient.quotesInstance.getRandomQuote().enqueue(object : Callback<List<QuoteResponse>> {
            override fun onResponse(call: Call<List<QuoteResponse>>, response: Response<List<QuoteResponse>>) {
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val quoteResponse = response.body()?.get(0)
                    if (quoteResponse != null) {
                        quoteTextView.text = "\"${quoteResponse.quote}\""
                        authorTextView.text = "- ${quoteResponse.author}"
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch quote\nError: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<QuoteResponse>>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to fetch quote", Toast.LENGTH_SHORT).show()
            }
        })
    }
}