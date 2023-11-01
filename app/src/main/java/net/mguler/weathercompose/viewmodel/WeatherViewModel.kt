package net.mguler.weathercompose.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.mguler.weathercompose.data.Utils
import net.mguler.weathercompose.data.WeatherStore
import net.mguler.weathercompose.model.City
import net.mguler.weathercompose.network.CurrentWeather
import net.mguler.weathercompose.network.Weather
import net.mguler.weathercompose.network.WeatherAPI

class WeatherViewModel(app: Application) : AndroidViewModel(app) {

    enum class WeatherStatus { LOADING, ERROR, DONE }

    private val _status = MutableStateFlow(WeatherStatus.LOADING)
    val status: StateFlow<WeatherStatus> = _status

    private val _uiState = MutableStateFlow(Weather())
    val uiState: StateFlow<Weather> = _uiState.asStateFlow()

    init {
        _uiState.value = Weather(cityName = "Loading..",
            currentWeather = CurrentWeather(temperature = 0.0, windspeed = 0.0, weathercode = -1)
        )
        //val city = getSelectedCity(app)
        //getWeatherData(city)
    }

    fun getWeatherData(city: City) {
        viewModelScope.launch {
            //_status.value = WeatherStatus.LOADING
            try {
                val result = WeatherAPI.weatherService.getNewWeatherData(city.lat, city.lng)
                result.body()?.let { weather ->
                    _uiState.value = weather
                    _uiState.update { it.copy(cityName = city.name) }
                }
                _status.value = WeatherStatus.DONE
                //_weatherData.value?.cityName = city.cityName
            } catch (e: Exception) {
                _status.value = WeatherStatus.ERROR
                println(e.localizedMessage)
                //_weatherData.value = listOf()
            }
        }
    }

    private fun getSelectedCity(app: Application): City {
        val sp = app.getSharedPreferences(Utils.PREFS, Context.MODE_PRIVATE)

        val spSelectedCity = sp.getString("location", "Globe:0:0")!!
        val listSelectedCity = spSelectedCity.split(":")

        val cityName = listSelectedCity[0]
        val cityLat = listSelectedCity[1].toDouble()
        val cityLng = listSelectedCity[2].toDouble()
        return City(cityName, cityLat, cityLng)
    }

}