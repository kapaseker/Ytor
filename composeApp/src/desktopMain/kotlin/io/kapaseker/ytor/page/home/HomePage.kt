package io.kapaseker.ytor.page.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.kapaseker.ytor.LocalController
import io.kapaseker.ytor.nav.SettingNav
import io.kapaseker.ytor.page.home.biz.HomeViewModel
import io.kapaseker.ytor.resource.PaddingMedium
import io.kapaseker.ytor.resource.PagePadding
import io.kapaseker.ytor.resource.inPainter
import io.kapaseker.ytor.resource.inString
import io.kapaseker.ytor.widget.AppRoundFilledIconButton
import io.kapaseker.ytor.widget.AppToggleIconButton
import io.kapaseker.ytor.widget.AppToggleIconButtonSmall
import io.kapaseker.ytor.widget.Page
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ytor.composeapp.generated.resources.Res
import ytor.composeapp.generated.resources.download
import ytor.composeapp.generated.resources.download_destination_hint
import ytor.composeapp.generated.resources.download_destination_label
import ytor.composeapp.generated.resources.download_link_hint
import ytor.composeapp.generated.resources.download_link_label
import ytor.composeapp.generated.resources.history
import ytor.composeapp.generated.resources.save
import ytor.composeapp.generated.resources.setting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    entry: NavBackStackEntry,
    vm: HomeViewModel = viewModel { HomeViewModel() },
    scope: CoroutineScope = rememberCoroutineScope(),
) {

    val destinationHistory by vm.destinationHistory.collectAsState()

    val controller = LocalController.current
    var dir by remember { mutableStateOf("") }

    var showDestinationHistory by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var input by remember { mutableStateOf("") }

    val snackHostState = remember {
        SnackbarHostState()
    }

    fun showDestinationHistory() {
        showDestinationHistory = true
    }

    fun chooseFileSaveDir() {
        scope.launch(Dispatchers.IO) {
            val saveDir = FileKit.openDirectoryPicker()
            dir = saveDir?.file?.absolutePath.orEmpty()
        }
    }

    fun startDownload() {
        scope.launch {
            if (input.isEmpty()) {

            }else if (dir.isEmpty()) {

            }else {
                vm.download(input, dir)
                input = ""
                snackHostState.showSnackbar("good")
            }
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

            Row(
                modifier = Modifier.padding(top = PagePadding).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PaddingMedium),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = input,
                    singleLine = true,
                    onValueChange = {
                        input = it
                    },
                    label = {
                        Text(
                            text = Res.string.download_link_label.inString(),
                        )
                    },
                    placeholder = {
                        Text(
                            text = Res.string.download_link_hint.inString(),
                        )
                    }
                )

                AppRoundFilledIconButton(
                    icon = Res.drawable.download
                ) {
                    startDownload()
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PaddingMedium),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Box(
                    modifier = Modifier.weight(1f),
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = dir,
                        readOnly = true,
                        singleLine = true,
                        onValueChange = {

                        },

                        label = {
                            Text(
                                text = Res.string.download_destination_label.inString(),
                            )
                        },

                        placeholder = {
                            Text(
                                text = Res.string.download_destination_hint.inString(),
                            )
                        }
                    )

                    AppToggleIconButton(
                        modifier = Modifier.align(Alignment.CenterEnd)
                            .padding(top = 8.dp, end = 4.dp),
                        checked = showDestinationHistory,
                        icon = Res.drawable.history,
                        contentDescription = null,
                    ) {
                        showDestinationHistory()
                    }
                }


                AppRoundFilledIconButton(icon = Res.drawable.save) {
                    chooseFileSaveDir()
                }
            }


            if (showDestinationHistory) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showDestinationHistory = false
                    },
                    sheetState = sheetState
                ) {
                    // Sheet content
                    destinationHistory.forEach {
                        Text(it)
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
        ) {
            Snackbar(snackbarData = it)
        }
    }
}