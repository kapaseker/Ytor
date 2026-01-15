package io.kapaseker.ytor.page.index

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import io.kapaseker.ytor.LocalController
import io.kapaseker.ytor.nav.SettingNav
import io.kapaseker.ytor.nav.StartNav
import io.kapaseker.ytor.page.index.biz.IndexViewModel
import io.kapaseker.ytor.resource.*
import io.kapaseker.ytor.storage.DownloadTask
import io.kapaseker.ytor.util.openFileExplorer
import io.kapaseker.ytor.widget.AppIconButton
import io.kapaseker.ytor.widget.IconButtonStyle
import io.kapaseker.ytor.widget.Page
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import ytor.composeapp.generated.resources.Res
import ytor.composeapp.generated.resources.icon_add
import ytor.composeapp.generated.resources.setting
import ytor.composeapp.generated.resources.tab_completed
import ytor.composeapp.generated.resources.tab_downloading
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TaskTab(val titleRes: StringResource) {
    Downloading(Res.string.tab_downloading),
    Completed(Res.string.tab_completed)
}

@Composable
fun IndexPage(
    entry: NavBackStackEntry,
    vm: IndexViewModel = viewModel { IndexViewModel() }
) {
    val controller = LocalController.current
    val pagerState = rememberPagerState(pageCount = { TaskTab.entries.size })
    val scope = rememberCoroutineScope()

    val downloadingTasks by vm.downloadingTasks.collectAsState()
    val completedTasks by vm.completedTasks.collectAsState()

    // Track previous task count to detect when tasks go from empty to non-empty
    val previousTaskCount = remember { mutableIntStateOf(0) }
    val hasCheckedInitialState = remember { mutableIntStateOf(0) }

    // Auto-switch to Downloading tab when page is first entered with existing download tasks
    LaunchedEffect(Unit) {
        if (hasCheckedInitialState.intValue == 0) {
            hasCheckedInitialState.intValue = 1
            if (downloadingTasks.isNotEmpty() && pagerState.currentPage != 0) {
                pagerState.animateScrollToPage(0)
            }
        }
    }

    // Auto-switch to Downloading tab when new download task is detected (from empty to non-empty)
    LaunchedEffect(downloadingTasks.size) {
        val currentCount = downloadingTasks.size
        if (previousTaskCount.intValue == 0 && currentCount > 0 && pagerState.currentPage != 0) {
            pagerState.animateScrollToPage(0)
        }
        previousTaskCount.intValue = currentCount
    }

    Page {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top bar with settings button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = PagePadding),
                    horizontalArrangement = Arrangement.End
                ) {
                    AppIconButton(
                        icon = Res.drawable.setting,
                        style = IconButtonStyle.Filled,
                        contentDescription = Res.string.setting.inString(),
                    ) {
                        controller.navigate(SettingNav)
                    }
                }

                // TabRow
                PrimaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TaskTab.entries.forEachIndexed { index, tab ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(text = tab.titleRes.inString())
                            }
                        )
                    }
                }

                // HorizontalPager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                        0 -> DownloadingPageContent(tasks = downloadingTasks)
                        1 -> CompletedPageContent(tasks = completedTasks)
                    }
                }
            }

            // FloatingActionButton in bottom right corner
            FloatingActionButton(
                onClick = {
                    controller.navigate(StartNav)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(PagePadding)
            ) {
                Icon(
                    painter = Res.drawable.icon_add.inPainter(),
                    contentDescription = "Add download task",
                    modifier = Modifier.size(FloatingActionButtonIconSize)
                )
            }
        }
    }
}

@Composable
private fun DownloadingPageContent(tasks: List<DownloadTask>) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Click + to download",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(PagePadding)
        ) {
            items(tasks, key = { it.id }) { task ->
                DownloadingTaskItem(task = task)
            }
        }
    }
}

@Composable
private fun CompletedPageContent(tasks: List<DownloadTask>) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No completed tasks",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(PagePadding)
        ) {
            items(tasks, key = { it.id }) { task ->
                CompletedTaskItem(task = task)
            }
        }
    }
}

@Composable
private fun DownloadingTaskItem(task: DownloadTask) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CardVerticalPadding),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PagePadding)
        ) {
            Text(
                text = task.title ?: task.url,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(SpacingMedium))
            LinearProgressIndicator(
                progress = {
                    task.progress / 100f
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(SpacingSmall))
            Text(
                text = "${task.progress.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val etaString by derivedStateOf {
                task.eta.orEmpty()
            }

            Text(
                text = "ETA: $etaString",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = task.destination,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CompletedTaskItem(task: DownloadTask) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .padding(vertical = CardVerticalPadding)
            .clickable {
                openFileExplorer(task.destination)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PagePadding)
        ) {
            Text(
                text = task.title ?: task.url,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(SpacingMedium))
            Text(
                text = task.destination,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            task.completedTime?.let { time ->
                Text(
                    text = "Completed: ${dateFormat.format(Date(time))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

