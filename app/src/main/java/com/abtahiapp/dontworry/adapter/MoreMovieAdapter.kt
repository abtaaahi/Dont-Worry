package com.abtahiapp.dontworry.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.Movie
import com.abtahiapp.dontworry.R
import com.bumptech.glide.Glide

class MoreMovieAdapter(
    private val context: Context,
    private var movies: MutableList<Movie>,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<MoreMovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.movie_thumbnail)
        val title: TextView = view.findViewById(R.id.movie_title)

        fun bind(movie: Movie) {
            title.text = movie.title
            val fullThumbnailUrl = "https://image.tmdb.org/t/p/w500" + movie.thumbnailUrl
            Glide.with(context).load(fullThumbnailUrl).into(thumbnail)
            itemView.setOnClickListener {
                onMovieClick(movie)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])

    }

    override fun getItemCount(): Int = movies.size

    fun updateMovies(newMovies: List<Movie>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }
}