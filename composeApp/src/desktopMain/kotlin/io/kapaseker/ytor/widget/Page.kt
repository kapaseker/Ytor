package io.kapaseker.ytor.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.kapaseker.ytor.resource.PagePadding

@Composable
fun Page(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier.safeContentPadding().fillMaxSize().padding(PagePadding).then(modifier),
        contentAlignment = contentAlignment,
        content = content
    )
}