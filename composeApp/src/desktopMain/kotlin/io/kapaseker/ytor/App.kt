package io.kapaseker.ytor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.kapaseker.ytor.nav.IndexNav
import io.kapaseker.ytor.nav.StartNav
import io.kapaseker.ytor.nav.SettingNav
import io.kapaseker.ytor.nav.composablePage
import io.kapaseker.ytor.page.index.IndexPage
import io.kapaseker.ytor.page.start.StartPage
import io.kapaseker.ytor.page.setting.SettingPage
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
        NavHost(navController = navController, startDestination = IndexNav) {
            composablePage<IndexNav> {
                IndexPage(entry = it)
            }
            composablePage<StartNav> {
                StartPage(entry = it)
            }
            composablePage<SettingNav> {
                SettingPage(entry = it)
            }
        }
    }
}