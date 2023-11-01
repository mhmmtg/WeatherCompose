package net.mguler.weathercompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.mguler.weathercompose.ui.pages.EditPage
import net.mguler.weathercompose.ui.pages.WeatherPage
import net.mguler.weathercompose.ui.theme.WeatherComposeTheme

// TODO: icon colors

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorResource(R.color.main_bg)
                ) {
                    PageNavigation()
                }
            }
        }

    }
}

@Composable
fun PageNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "weather") {
        composable("weather") { WeatherPage(navController) }
        composable("edit") { EditPage(navController) }
    }
}



