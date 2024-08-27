package com.abtahiapp.dontworry.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.WeatherForecast
import com.bumptech.glide.Glide

class WeatherAdapter(private val context: Context, private var weatherList: List<WeatherForecast>) :
    RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {

    inner class WeatherViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.weather_date)
        val temp: TextView = view.findViewById(R.id.weather_temp)
        val description: TextView = view.findViewById(R.id.weather_description)
        val icon: ImageView = view.findViewById(R.id.weather_icon)

        fun bind(weather: WeatherForecast) {
            date.text = weather.dt_txt
            temp.text = "${weather.main.temp}°C"
            description.text = weather.weather[0].description
            val iconUrl = "https://openweathermap.org/img/wn/${weather.weather[0].icon}.png"
            Glide.with(context).load(iconUrl).into(icon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        holder.bind(weatherList[position])
    }

    override fun getItemCount(): Int = weatherList.size

    fun updateWeather(newWeatherList: List<WeatherForecast>) {
        weatherList = newWeatherList
        notifyDataSetChanged()
    }
}