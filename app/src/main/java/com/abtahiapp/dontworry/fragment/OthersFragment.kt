package com.abtahiapp.dontworry.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.abtahiapp.dontworry.QuoteResponse
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.RetrofitClient
import com.abtahiapp.dontworry.activity.PlacesActivity
import com.abtahiapp.dontworry.activity.SocialSpaceActivity
import com.abtahiapp.dontworry.activity.WeatherActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OthersFragment : Fragment() {

    private lateinit var quoteTextView: TextView
    private lateinit var authorTextView: TextView

    private val TAG = "OthersFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        val view = inflater.inflate(R.layout.fragment_others, container, false)
        quoteTextView = view.findViewById(R.id.quoteTextView)
        authorTextView = view.findViewById(R.id.authorTextView)

        val cardView1 = view.findViewById<CardView>(R.id.cardView1)
        val cardView2 = view.findViewById<CardView>(R.id.cardView2)
        val cardView3 = view.findViewById<CardView>(R.id.cardView3)
        val cardView4 = view.findViewById<CardView>(R.id.cardView4)

        cardView1.setOnClickListener {
            fetchAndDisplayQuote()
        }

        cardView2.setOnClickListener {
            val intent = Intent(activity, PlacesActivity::class.java)
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim)
        }

        cardView3.setOnClickListener {
            val intent = Intent(activity, WeatherActivity::class.java)
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim)
        }

        cardView4.setOnClickListener {
            val intent = Intent(activity, SocialSpaceActivity::class.java)
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim)
        }

        fetchAndDisplayQuote()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
    }

    private fun fetchAndDisplayQuote() {
        Log.d(TAG, "Fetching random quote")
        RetrofitClient.quotesInstance.getRandomQuote().enqueue(object : Callback<List<QuoteResponse>> {
            override fun onResponse(call: Call<List<QuoteResponse>>, response: Response<List<QuoteResponse>>) {
                Log.d(TAG, "Quote fetched successfully, response: ${response.code()}")
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val quoteResponse = response.body()?.get(0)
                    if (quoteResponse != null) {
                        quoteTextView.text = "\"${quoteResponse.quote}\""
                        authorTextView.text = "- ${quoteResponse.author}"
                    }
                } else {
                    Log.e(TAG, "Failed to fetch quote, response code: ${response.code()}")
                    Toast.makeText(requireContext(), "Failed to fetch quote\nError: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<QuoteResponse>>, t: Throwable) {
                Log.e(TAG, "Failed to fetch quote, error: ${t.message}")
                Toast.makeText(requireContext(), "Failed to fetch quote", Toast.LENGTH_SHORT).show()
            }
        })
    }
}