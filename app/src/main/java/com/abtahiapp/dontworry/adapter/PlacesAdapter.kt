package com.abtahiapp.dontworry.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.Place
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.activity.MapActivity
import com.bumptech.glide.Glide

class PlacesAdapter(
    private val context: Context,
    private var places: MutableList<Place>
) : RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.placeName.text = place.name

        if (place.imageUrl.isNotEmpty()) {
            val imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=${place.imageUrl}&key=YOUR_GOOGLE_API_KEY"
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.defaultnews)
                .into(holder.placeImage)
        } else {
            holder.placeImage.setImageResource(R.drawable.defaultnews)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MapActivity::class.java).apply {
                putExtra("place_name", place.name)
                putExtra("latitude", place.latitude)
                putExtra("longitude", place.longitude)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return places.size
    }

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeName: TextView = itemView.findViewById(R.id.place_name)
        val placeImage: ImageView = itemView.findViewById(R.id.place_image)
    }

    fun updatePlaces(newPlaces: List<Place>) {
        places.clear()
        places.addAll(newPlaces)
        notifyDataSetChanged()
    }
}
