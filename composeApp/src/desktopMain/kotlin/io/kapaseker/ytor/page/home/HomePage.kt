package io.kapaseker.ytor.page.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import io.kapaseker.ytor.LocalController
import io.kapaseker.ytor.nav.SettingNav
import io.kapaseker.ytor.resource.PagePadding
import io.kapaseker.ytor.resource.inString
import io.kapaseker.ytor.widget.AppFilledIconButton
import io.kapaseker.ytor.widget.Page
import ytor.composeapp.generated.resources.Res
import ytor.composeapp.generated.resources.app_name
import ytor.composeapp.generated.resources.setting

@Composable
fun HomePage(
    entry: NavBackStackEntry,
) {

    val controller = LocalController.current

    Page {
        AppFilledIconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            icon = Res.drawable.setting,
            contentDescription = Res.string.setting.inString(),
        ) {
            controller.navigate(SettingNav)
        }

        Text(text = "Hello ${Res.string.app_name.inString()}", modifier = Modifier.align(Alignment.Center))
    }
}