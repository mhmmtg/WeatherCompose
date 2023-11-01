package net.mguler.weathercompose.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.mguler.weathercompose.network.Geo
import net.mguler.weathercompose.network.GeoAPI

class GeoViewModel(app: Application): AndroidViewModel(app) {
    private val _geoData = MutableLiveData<Geo>()
    val geoData: LiveData<Geo> get() = _geoData

    fun getGeoData(name: String, b: Boolean) {
        viewModelScope.launch {
            if (b) {
                try {
                    val result = GeoAPI.geoService.getGeoData(name)
                    _geoData.value = result.body()
                }
                catch (e: Exception) {
                    println(e.localizedMessage)
                }
            }
            else {
                _geoData.value = Geo()
            }

        }
    }
}