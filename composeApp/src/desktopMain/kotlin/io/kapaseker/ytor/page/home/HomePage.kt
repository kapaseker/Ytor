package io.kapaseker.ytor.page.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.kapaseker.ytor.LocalController
import io.kapaseker.ytor.nav.SettingNav
import io.kapaseker.ytor.page.home.biz.HomeViewModel
import io.kapaseker.ytor.resource.*
import io.kapaseker.ytor.util.isValidHttpUrl
import io.kapaseker.ytor.widget.AppIconButton
import io.kapaseker.ytor.widget.AppToggleIconButton
import io.kapaseker.ytor.widget.IconButtonStyle
import io.kapaseker.ytor.widget.Page
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ytor.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    entry: NavBackStackEntry,
    vm: HomeViewModel = viewModel { HomeViewModel() },
    scope: CoroutineScope = rememberCoroutineScope(),
) {

    val destinationHistory by vm.destinationHistory.collectAsState()
    val downloadState by vm.downloadState.collectAsState()

    // 调试：打印下载状态
    LaunchedEffect(downloadState) {
        println("=== Download State ===")
        println("Status: ${downloadState.status}")
        println("Progress: ${downloadState.progress}%")
        println("Speed: ${downloadState.speed}")
        println("ETA: ${downloadState.eta}")
        println("Total Size: ${downloadState.totalSize}")
        println("Error: ${downloadState.errorMessage}")
        println("======================")
    }

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

    fun hideDestinationHistory() {
        showDestinationHistory = false
    }

    fun chooseFileSaveDir() {
        scope.launch(Dispatchers.IO) {
            val saveDir = FileKit.openDirectoryPicker()
            dir = saveDir?.file?.absolutePath.orEmpty()
        }
    }

    fun chooseHistory(history: String) {
        dir = history
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                hideDestinationHistory()
            }
        }
    }

    fun deleteHistory(history: String) {
        vm.deleteHistory(history)
    }

    fun startDownload() {
        scope.launch {
            if (input.isEmpty()) {
                snackHostState.showSnackbar(Res.string.download_link_empty.getString())
            } else if (dir.isEmpty()) {
                snackHostState.showSnackbar(Res.string.download_destination_empty.getString())
            } else if(!input.isValidHttpUrl()) {
                snackHostState.showSnackbar(Res.string.download_link_not_url.getString())
            } else {
                vm.download(input, dir)
                input = ""
            }
        }
    }

    Page {

        Column {

            Row {

                Spacer(modifier = Modifier.weight(1f))

                AppIconButton(
                    icon = Res.drawable.setting,
                    style = IconButtonStyle.Filled,
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

                AppIconButton(
                    icon = Res.drawable.download,
                    style = IconButtonStyle.Filled,
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


                AppIconButton(
                    icon = Res.drawable.save,
                    style = IconButtonStyle.Filled,
                ) {
                    chooseFileSaveDir()
                }
            }


            if (showDestinationHistory) {
                ModalBottomSheet(
                    onDismissRequest = {
                        hideDestinationHistory()
                    },
                    sheetState = sheetState,
                ) {

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(destinationHistory, key = { a, b -> b }) { index, value ->
                            val showDivider by remember {
                                derivedStateOf {
                                    index != destinationHistory.lastIndex
                                }
                            }
                            Box(
                                modifier = Modifier.height(SingleLineListItemHeight).fillMaxWidth()
                                    .clickable {
                                        chooseHistory(value)
                                    }
                            ) {
                                Text(
                                    text = value,
                                    modifier = Modifier.align(Alignment.CenterStart)
                                        .padding(start = SingleLineListItemPaddingHorizontal)
                                )

                                AppIconButton(
                                    modifier = Modifier.align(alignment = Alignment.CenterEnd).padding(end = SingleLineListItemPaddingHorizontal),
                                    size = ButtonSize.XSmall,
                                    style = IconButtonStyle.Normal,
                                    icon = Res.drawable.delete,
                                ) {
                                    deleteHistory(value)
                                }

                                if (showDivider) {
                                    Divider(modifier = Modifier.align(Alignment.BottomCenter))
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = PagePadding),
        ) {
            Snackbar(snackbarData = it)
        }
    }
}