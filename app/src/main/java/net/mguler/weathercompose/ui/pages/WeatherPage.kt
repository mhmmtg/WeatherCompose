package net.mguler.weathercompose.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import net.mguler.weathercompose.R
import net.mguler.weathercompose.data.Utils
import net.mguler.weathercompose.data.Utils.weatherImg
import net.mguler.weathercompose.data.WeatherStore
import net.mguler.weathercompose.model.NextHours
import net.mguler.weathercompose.network.Hourly
import net.mguler.weathercompose.ui.theme.Typography
import net.mguler.weathercompose.viewmodel.WeatherViewModel
import net.mguler.weathercompose.viewmodel.WeatherViewModel.WeatherStatus.DONE
import net.mguler.weathercompose.viewmodel.WeatherViewModel.WeatherStatus.ERROR
import net.mguler.weathercompose.viewmodel.WeatherViewModel.WeatherStatus.LOADING
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun WeatherPage(navController: NavController) {
    val weatherVM: WeatherViewModel = viewModel()
    //val weather by weatherVM.weatherData.observeAsState()

    val uiState by weatherVM.uiState.collectAsState()
    val status by weatherVM.status.collectAsState()

    val context = LocalContext.current
    val locationData by WeatherStore(context).read.collectAsState(initial = null)
    locationData?.let { weatherVM.getWeatherData(it) }

    //Whole page
    Column(modifier = Modifier.padding(top = 32.dp), verticalArrangement = SpaceBetween) {
        //Spacer(modifier = Modifier.height(16.dp))

        val rowModifier = Modifier
            .padding(horizontal = 32.dp)
            .clickable { navController.navigate("edit") }

        val maxWidth = Modifier.fillMaxWidth()

        //Location
        Row(rowModifier) {
            Icon(
                imageVector = Icons.Rounded.Place,
                contentDescription = "",
                modifier = Modifier.size(32.dp),
            )

            BodyText(text = locationData?.name ?: "Loading", size = 24.sp)
            Spacer(modifier = Modifier.weight(1f))

            Icon(
                painter = painterResource(id = R.drawable.ic_edit),
                contentDescription = "",
                modifier = Modifier.size(32.dp),
            )
        }


        //Middle bar
        Row(modifier = maxWidth) {
            when(status) {
                LOADING -> StatusText(text = "Loading..")
                ERROR -> StatusText(text = "No connection!")
                DONE -> {
                    Column(modifier = maxWidth, horizontalAlignment = CenterHorizontally) {
                        MainImage(uiState.currentWeather?.weathercode)
                        Text(
                            text = Utils.weatherMap[uiState.currentWeather?.weathercode]!!,
                            style = Typography.bodyLarge,
                            fontSize = 24.sp
                        )
                        Divider()


                        Row {
                            val degree = uiState.currentWeather?.temperature?.toInt().toString()
                            //degree?.let { degreeText = it.toString() }

                            BodyText(text = degree, 100.sp)
                            Text(text = "o", fontSize = 34.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(verticalAlignment = CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_wind),
                                contentDescription = ""
                            )
                            BodyText("0.0km/h", 16.sp)
                            Divider()

                            Icon(
                                painter = painterResource(id = R.drawable.ic_humidity),
                                contentDescription = ""
                            )
                            val precipitation = "${uiState.hourly?.precipitationProbability?.get(LocalTime.now().hour)?.toString()}%"
                            BodyText(precipitation ,16.sp)
                        }
                        Divider()
                    }
                }
            }
        }

        //Lazy row wrapper
        Row(Modifier.padding(8.dp, 16.dp)) { HourlyCard(uiState.hourly) }

    }

}


@Composable
fun BodyText(text: String, size: TextUnit) {
    Text(
        text = text,
        fontSize = size,
        style = Typography.bodyLarge
    )
}

@Composable
fun StatusText(text: String) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        fontSize = 34.sp,
        style = Typography.bodyLarge,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun MainImage(weathercode: Int?) {
    Image(
        painter = painterResource(id = weatherImg[weathercode] ?: weatherImg[-1]!!),
        contentDescription = "",
        modifier = Modifier.size(150.dp, 150.dp),
        //contentScale = ContentScale.Crop
    )
}

@Composable
fun HourlyCard(hourlyData: Hourly?) {
    val nextHoursList = ArrayList<NextHours>()
    hourlyData?.let { hourly ->
        for (i in 0..23) {
            val nextTemp = hourly.temperature2m?.get(i)

            val weatherCodeHourly = hourly.weathercode?.get(i)
            val nextIcon = weatherImg[weatherCodeHourly]

            val nextHour = hourly.time?.get(i)
            val nextHourTime = LocalDateTime.parse(nextHour).toLocalTime().toString()

            val nextHours = NextHours(nextTemp, nextHourTime, nextIcon)
            nextHoursList.add(nextHours)
        }
    }


    val listState = rememberLazyListState()
    LazyRow(state = listState) {
        items(nextHoursList) { CardItem(it) }
    }

    val toScroll = LocalTime.now().hour
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(key1 = true) {
        coroutineScope.launch {
            listState.scrollToItem(toScroll)
        }
    }
}

@Composable
private fun CardItem(hour: NextHours) {
    ElevatedCard(
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.row_bg)
        ),
        modifier = Modifier.size(width = 120.dp, height = 180.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    )
    {
        Column(
            verticalArrangement = SpaceBetween,
            horizontalAlignment = CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            BodyText(text = hour.nextHours!!, 16.sp)
            Image(painter = painterResource(id = hour.icon!!), contentDescription = "")
            Row {
                BodyText(hour.temperature?.toInt().toString(), 34.sp)
                Text(text = "o", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    Divider()

    //Sadece bir özellik dğiştirmek için -philip
    //LocalTextStyle.current.copy(textAlign = TextAlign.Right)
}


@Composable
fun Divider() {
    Spacer(Modifier.size(8.dp))
}