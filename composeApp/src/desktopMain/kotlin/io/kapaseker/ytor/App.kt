package io.kapaseker.ytor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.kapaseker.ytor.nav.HomeNav
import io.kapaseker.ytor.nav.composablePage
import io.kapaseker.ytor.page.home.HomePage
import org.jetbrains.compose.ui.tooling.preview.Preview

val LocalController = staticCompositionLocalOf<NavHostController> { error("null controller") }

@Composable
@Preview
fun App() {
    // Creates the NavController
    val navController = rememberNavController()

    CompositionLocalProvider(
        LocalController provides navController
    ) {

        // Creates the NavHost with the navigation graph consisting of supplied destinations
        NavHost(navController = navController, startDestination = HomeNav) {
            composablePage<HomeNav> {
                HomePage(entry = it)
            }
        }
    }
}