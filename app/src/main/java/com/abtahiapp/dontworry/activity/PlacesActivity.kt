package com.abtahiapp.dontworry.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.BuildConfig
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.adapter.PlacesAdapter
import com.abtahiapp.dontworry.utils.InfoBottomSheetDialog
import com.abtahiapp.dontworry.viewmodel.PlacesViewModel

class PlacesActivity : AppCompatActivity() {

    private lateinit var placesAdapter: PlacesAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var placesRecyclerView: RecyclerView

    private val placesViewModel: PlacesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places)

        progressBar = findViewById(R.id.progress_bar)
        placesRecyclerView = findViewById(R.id.places_recycler_view)
        placesRecyclerView.layoutManager = LinearLayoutManager(this)
        placesAdapter = PlacesAdapter(this, mutableListOf())
        placesRecyclerView.adapter = placesAdapter

        val infoButton = findViewById<ImageButton>(R.id.infoButton)
        infoButton.setOnClickListener {

            val infoMessage = getString(R.string.places_activity_info_message).trimIndent()

            val infoBottomSheetDialog = InfoBottomSheetDialog(this, infoMessage)
            infoBottomSheetDialog.show()
        }

        observeViewModel()
        fetchPlaces()
    }

    private fun observeViewModel() {
        placesViewModel.places.observe(this, Observer { placesList ->
            placesAdapter.updatePlaces(placesList)
        })

        placesViewModel.isLoading.observe(this, Observer { isLoading ->
            showLoading(isLoading)
        })

        placesViewModel.errorMessage.observe(this, Observer { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun fetchPlaces() {
        val apiKey = BuildConfig.GOOGLE_API_KEY
        val cx = BuildConfig.CUSTOM_SEARCH_ENGINE_ID
        val query = "tourist places in bangladesh"
        placesViewModel.fetchNearbyPlaces(apiKey, cx, query)
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
}
