package com.abtahiapp.dontworry.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.Place
import com.abtahiapp.dontworry.PlacesResponse
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.RetrofitClient
import com.abtahiapp.dontworry.Secret
import com.abtahiapp.dontworry.adapter.PlacesAdapter
import retrofit2.Call
import retrofit2.Response

class PlacesActivity : AppCompatActivity() {

    private lateinit var placesAdapter: PlacesAdapter
    private lateinit var placesRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places)

        placesRecyclerView = findViewById(R.id.places_recycler_view)
        placesRecyclerView.layoutManager = LinearLayoutManager(this)
        placesAdapter = PlacesAdapter(this, mutableListOf())
        placesRecyclerView.adapter = placesAdapter

        fetchNearbyPlaces()

    }

    private fun fetchNearbyPlaces() {
        val apiKey = Secret.GOOGLE_API_KEY
        val location = "22.3475, 91.8123"
        val radius = 1500

        RetrofitClient.placesInstance.getNearbyPlaces(location, radius, apiKey = apiKey)
            .enqueue(object : retrofit2.Callback<PlacesResponse> {
                override fun onResponse(call: Call<PlacesResponse>, response: Response<PlacesResponse>) {
                    if (response.isSuccessful) {
                        val placesList = response.body()?.results?.map { placeResult ->
                            Place(
                                name = placeResult.name,
                                imageUrl = placeResult.photos?.get(0)?.photo_reference ?: "",
                                latitude = placeResult.geometry.location.lat,
                                longitude = placeResult.geometry.location.lng
                            )
                        } ?: emptyList()

                        placesAdapter.updatePlaces(placesList)
                    } else {
                        Log.e("PlacesActivity", "Failed to fetch places: ${response.message()}")
                        Toast.makeText(this@PlacesActivity, "Failed to fetch places: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<PlacesResponse>, t: Throwable) {
                    Log.e("PlacesActivity", "Failed to fetch places", t)
                    Toast.makeText(this@PlacesActivity, "Failed to fetch places: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

}