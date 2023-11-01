package net.mguler.weathercompose.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.map
import net.mguler.weathercompose.model.City
import java.io.IOException

private const val SETTINGS = "net.weather.settings"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(SETTINGS)


class WeatherStore(context: Context) {
    private val locationKey = stringPreferencesKey("location")
    private val latKey = doublePreferencesKey("latitude")
    private val lngKey = doublePreferencesKey("longitude")

    suspend fun save(context: Context, city: City) {
        context.dataStore.edit {
            it[locationKey] = city.name
            it[latKey] = city.lat
            it[lngKey] = city.lng
        }
    }

    val read: Flow<City> = context.dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            }
            else { throw it }
        }
        .map {
        City(
            it[locationKey] ?: "",
            it[latKey] ?: 0.0,
            it[lngKey] ?: 0.0
        )
    }
}
