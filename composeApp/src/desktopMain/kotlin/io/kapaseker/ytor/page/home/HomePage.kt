package io.kapaseker.ytor.page.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.kapaseker.ytor.LocalController
import io.kapaseker.ytor.nav.SettingNav
import io.kapaseker.ytor.page.home.biz.HomeViewModel
import io.kapaseker.ytor.resource.PaddingMedium
import io.kapaseker.ytor.resource.PagePadding
import io.kapaseker.ytor.resource.inString
import io.kapaseker.ytor.widget.AppRoundFilledIconButton
import io.kapaseker.ytor.widget.Page
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ytor.composeapp.generated.resources.Res
import ytor.composeapp.generated.resources.download
import ytor.composeapp.generated.resources.save
import ytor.composeapp.generated.resources.setting

@Composable
fun HomePage(
    entry: NavBackStackEntry,
    vm: HomeViewModel = viewModel { HomeViewModel() },
    scope: CoroutineScope = rememberCoroutineScope(),
) {

    val controller = LocalController.current
    var dir by remember { mutableStateOf("") }

    fun chooseFileSaveDir() {
        scope.launch(Dispatchers.IO) {
            val saveDir = FileKit.openDirectoryPicker()
            dir = saveDir?.file?.absolutePath.orEmpty()
        }
    }

    Page {

        Column {

            Row {

                Spacer(modifier = Modifier.weight(1f))

                AppRoundFilledIconButton(
                    icon = Res.drawable.setting,
                    contentDescription = Res.string.setting.inString(),
                ) {
                    controller.navigate(SettingNav)
                }
            }

            var input by remember { mutableStateOf("") }

            Row(
                modifier = Modifier.padding(top = PagePadding).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PaddingMedium),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = input, onValueChange = {
                        input = it
                    }
                )

                AppRoundFilledIconButton(icon = Res.drawable.save) {
                    chooseFileSaveDir()
                }

                AppRoundFilledIconButton(
                    icon = Res.drawable.download
                ) {
                    vm.download(input, dir)
                }
            }
        }
    }
}