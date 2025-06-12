package io.kapaseker.ytor

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.kapaseker.ytor.nav.Home
import io.kapaseker.ytor.page.home.HomePage
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    // Creates the NavController
    val navController = rememberNavController()

    // Creates the NavHost with the navigation graph consisting of supplied destinations
    NavHost(navController = navController, startDestination = Home) {
        composable<Home> { HomePage(  ) }
    }
}