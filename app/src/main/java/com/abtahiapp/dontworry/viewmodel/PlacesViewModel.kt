package com.abtahiapp.dontworry.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.abtahiapp.dontworry.CustomSearchResponse
import com.abtahiapp.dontworry.Place
import com.abtahiapp.dontworry.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlacesViewModel : ViewModel() {

    private val _places = MutableLiveData<List<Place>>()
    val places: LiveData<List<Place>> get() = _places

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun fetchNearbyPlaces(apiKey: String, cx: String, query: String) {
        _isLoading.value = true
        RetrofitClient.customSearchInstance.getPlaces(query, cx, apiKey)
            .enqueue(object : Callback<CustomSearchResponse> {
                override fun onResponse(
                    call: Call<CustomSearchResponse>,
                    response: Response<CustomSearchResponse>
                ) {
                    if (response.isSuccessful) {
                        _places.value = response.body()?.items?.map { searchItem ->
                            Place(
                                name = searchItem.title,
                                imageUrl = searchItem.pagemap?.cse_thumbnail?.get(0)?.src ?: ""
                            )
                        } ?: emptyList()
                        _isLoading.value = false
                    } else {
                        _isLoading.value = false
                        _errorMessage.value = "Failed to fetch places: ${response.message()}"
                    }
                }

                override fun onFailure(call: Call<CustomSearchResponse>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = "Failed to fetch places: ${t.message}"
                }
            })
    }
}
