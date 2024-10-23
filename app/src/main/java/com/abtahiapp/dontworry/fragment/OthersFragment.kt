package com.abtahiapp.dontworry.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.abtahiapp.dontworry.utils.QuoteResponse
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.utils.RetrofitClient
import com.abtahiapp.dontworry.activity.PlacesActivity
import com.abtahiapp.dontworry.activity.SocialSpaceActivity
import com.abtahiapp.dontworry.activity.WeatherActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Observer
import com.abtahiapp.dontworry.room.QuoteDatabase
import com.abtahiapp.dontworry.room.QuoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OthersFragment : Fragment() {

    private lateinit var quoteTextView: TextView
    private lateinit var authorTextView: TextView
    private lateinit var quoteDatabase: QuoteDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_others, container, false)
        quoteTextView = view.findViewById(R.id.quoteTextView)
        authorTextView = view.findViewById(R.id.authorTextView)

        val cardView1 = view.findViewById<CardView>(R.id.cardView1)
        val cardView2 = view.findViewById<CardView>(R.id.cardView2)
        val cardView3 = view.findViewById<CardView>(R.id.cardView3)
        val cardView4 = view.findViewById<CardView>(R.id.cardView4)

        quoteDatabase = QuoteDatabase.getDatabase(requireContext())

        cardView1.setOnClickListener {
            if (isOnline()) {
                fetchAndDisplayQuote()
            } else {
                Toast.makeText(requireContext(), "You are offline. Can't fetch new quotes.", Toast.LENGTH_SHORT).show()
            }
        }

        cardView2.setOnClickListener {
            if(isOnline()){
                val intent = Intent(activity, PlacesActivity::class.java)
                startActivity(intent)
                activity?.overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim)
            } else{
                Toast.makeText(requireContext(), "You are offline!", Toast.LENGTH_SHORT).show()
            }
        }

        cardView3.setOnClickListener {
            if(isOnline()){
                val intent = Intent(activity, WeatherActivity::class.java)
                startActivity(intent)
                activity?.overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim)
            } else{
                Toast.makeText(requireContext(), "You are offline!", Toast.LENGTH_SHORT).show()
            }
        }

        cardView4.setOnClickListener {
            if(isOnline()){
                val intent = Intent(activity, SocialSpaceActivity::class.java)
                startActivity(intent)
                activity?.overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim)
            } else{
                Toast.makeText(requireContext(), "You are offline!", Toast.LENGTH_SHORT).show()
            }
        }

        if (!isOnline()) {
            loadLastSavedQuote()
        } else {
            fetchAndDisplayQuote()
        }

        return view
    }

    private fun fetchAndDisplayQuote() {
        RetrofitClient.quotesInstance.getRandomQuote().enqueue(object : Callback<List<QuoteResponse>> {
            override fun onResponse(call: Call<List<QuoteResponse>>, response: Response<List<QuoteResponse>>) {
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val quoteResponse = response.body()?.get(0)
                    if (quoteResponse != null) {
                        val quoteText = "\"${quoteResponse.quote}\""
                        val authorText = "- ${quoteResponse.author}"
                        quoteTextView.text = quoteText
                        authorTextView.text = authorText

                        lifecycleScope.launch(Dispatchers.IO) {
                            quoteDatabase.quoteDao().insertQuote(
                                QuoteEntity(quote = quoteResponse.quote, author = quoteResponse.author)
                            )
                        }
                    }
                } else {
                    loadLastSavedQuote()
                    Toast.makeText(requireContext(), "Failed to fetch quote\nError: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<QuoteResponse>>, t: Throwable) {
                loadLastSavedQuote()
                Toast.makeText(requireContext(), "Failed to fetch quote", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadLastSavedQuote() {
        quoteDatabase.quoteDao().getLastQuote().observe(viewLifecycleOwner, Observer { quoteEntity ->
            if (quoteEntity != null) {
                quoteTextView.text = "\"${quoteEntity.quote}\""
                authorTextView.text = "- ${quoteEntity.author}"
            } else {
                Toast.makeText(requireContext(), "No offline data available", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isOnline(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}