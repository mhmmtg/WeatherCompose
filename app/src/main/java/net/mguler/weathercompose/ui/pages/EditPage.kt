package net.mguler.weathercompose.ui.pages

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons.Rounded
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mguler.weathercompose.data.WeatherStore
import net.mguler.weathercompose.model.City
import net.mguler.weathercompose.network.ResultsItem
import net.mguler.weathercompose.ui.theme.Typography
import net.mguler.weathercompose.ui.theme.mainText
import net.mguler.weathercompose.ui.theme.statusBar
import net.mguler.weathercompose.viewmodel.GeoViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EditPage(navController: NavHostController) {
    val cityText = remember { mutableStateOf("") }

    val vm: GeoViewModel = viewModel()
    val cities by vm.geoData.observeAsState()

    val colModifier = Modifier
        .fillMaxSize()
        .padding(16.dp)

    Column(modifier = colModifier, verticalArrangement = SpaceBetween) {

        //Search bar
        Row {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = CenterHorizontally) {
                CityTextField(cityText, vm)
                Spacer(modifier = Modifier.size(8.dp))

                LazyColumn {
                    cities?.results?.let { list ->
                        if (list.isNotEmpty()) {
                            items(list) { SearchRow(it, navController) }
                        }
                    }
                }
            }
        }

        //Find me button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            content = { FindMe() }
        )
    }
}


@Composable
fun CityTextField(cityText: MutableState<String>, vm: GeoViewModel) {
    OutlinedTextField(
        value = cityText.value,
        label = { Text(text = "Select Location") },
        onValueChange = {
            val lengthBool = it.length > 2
            cityText.value = it
            vm.getGeoData(it, lengthBool)
        },
        leadingIcon = { Icon(imageVector = Rounded.Place, contentDescription = "") },
        trailingIcon = { Icon(imageVector = Rounded.Clear, contentDescription = "") }
    )
}

@Composable
fun SearchRow(item: ResultsItem?, navController: NavController) { //??????????? host
    val context = LocalContext.current
    val city = City(item?.name!!, item.latitude!!, item.longitude!!)

    val rowModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 32.dp)
        .clickable {
            saveLocation(context, city)
            navController.navigate("weather")
        }

    Box(modifier = rowModifier) {
        Text(
            text = "${item.name}, ${item.admin1 ?: ""}",
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}

@Composable
fun FindMe() {
    val context = LocalContext.current
    // TODO: get location
    val city = City("Globe", 0.0, 0.0)

    ExtendedFloatingActionButton(
        onClick = { saveLocation(context, city) },
        icon = { Icon(Rounded.Place, "") },
        text = { Text(text = "Find Me!", color = Color.White, style = Typography.titleMedium) },
        containerColor = statusBar,
        contentColor = Color.White
    )
}


private fun saveLocation(context: Context, city: City) {
    val store = WeatherStore(context)
    CoroutineScope(Dispatchers.IO).launch {
        store.save(context, city)
    }
}