package com.abtahiapp.dontworry.mvc

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.BuildConfig
import com.abtahiapp.dontworry.utils.CustomSearchResponse
import com.abtahiapp.dontworry.utils.Place
//import com.abtahiapp.dontworry.PlacesResponse
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.utils.RetrofitClient
import com.abtahiapp.dontworry.adapter.PlacesAdapter
import retrofit2.Call
import retrofit2.Response

// MVC Design Pattern

class PlacesActivity : AppCompatActivity() {

    private lateinit var placesAdapter: PlacesAdapter
    private lateinit var placesRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places)

        progressBar = findViewById(R.id.progress_bar)
        placesRecyclerView = findViewById(R.id.places_recycler_view)
        placesRecyclerView.layoutManager = LinearLayoutManager(this)
        placesAdapter = PlacesAdapter(this, mutableListOf())
        placesRecyclerView.adapter = placesAdapter

        showLoading(true)
        fetchNearbyPlaces()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            placesRecyclerView.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            placesRecyclerView.visibility = View.VISIBLE
        }
    }

//    private fun fetchNearbyPlaces() {
//        val apiKey = Secret.GOOGLE_API_KEY
//        val location = "22.3475, 91.8123"
//        val radius = 1500
//
//        RetrofitClient.placesInstance.getNearbyPlaces(location, radius, apiKey = apiKey)
//            .enqueue(object : retrofit2.Callback<PlacesResponse> {
//                override fun onResponse(call: Call<PlacesResponse>, response: Response<PlacesResponse>) {
//                    if (response.isSuccessful) {
//                        val placesList = response.body()?.results?.map { placeResult ->
//                            Place(
//                                name = placeResult.name,
//                                imageUrl = placeResult.photos?.get(0)?.photo_reference ?: "",
//                                latitude = placeResult.geometry.location.lat,
//                                longitude = placeResult.geometry.location.lng
//                            )
//                        } ?: emptyList()
//
//                        placesAdapter.updatePlaces(placesList)
//                    } else {
//                        Log.e("PlacesActivity", "Failed to fetch places: ${response.message()}")
//                        Toast.makeText(this@PlacesActivity, "Failed to fetch places: ${response.message()}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<PlacesResponse>, t: Throwable) {
//                    Log.e("PlacesActivity", "Failed to fetch places", t)
//                    Toast.makeText(this@PlacesActivity, "Failed to fetch places: ${t.message}", Toast.LENGTH_SHORT).show()
//                }
//            })
//    }

    private fun fetchNearbyPlaces() {
        showLoading(true)
        val apiKey = BuildConfig.GOOGLE_API_KEY
        val cx = BuildConfig.CUSTOM_SEARCH_ENGINE_ID
        val query = "tourist attractions near Chittagong"

        RetrofitClient.customSearchInstance.getPlaces(query, cx, apiKey)
            .enqueue(object : retrofit2.Callback<CustomSearchResponse> {
                override fun onResponse(call: Call<CustomSearchResponse>, response: Response<CustomSearchResponse>) {
                    if (response.isSuccessful) {
                        val placesList = response.body()?.items?.map { searchItem ->
                            Place(
                                name = searchItem.title,
                                imageUrl = searchItem.pagemap?.cse_thumbnail?.get(0)?.src ?: ""
                            )
                        } ?: emptyList()

                        placesAdapter.updatePlaces(placesList)
                        showLoading(false)
                    } else {
                        showLoading(false)
                        Toast.makeText(this@PlacesActivity, "Failed to fetch places: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CustomSearchResponse>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(this@PlacesActivity, "Failed to fetch places: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}