package io.kapaseker.ytor.resource

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import org.jetbrains.compose.resources.*


@Composable
fun StringResource.inString():String {
    return stringResource(this)
}

suspend fun StringResource.getString():String {
    return getString(resource = this)
}

@Composable
fun StringResource.inString(vararg formatArgs: Any):String {
    return stringResource(this, *formatArgs)
}

suspend fun StringResource.getString(vararg formatArgs: Any):String {
    return getString(resource = this, *formatArgs)
}

@Composable
fun StringArrayResource.inStringArray():List<String> {
    return stringArrayResource(this)
}

@Composable
fun  DrawableResource.inPainter(): Painter {
    return painterResource(this)
}


val Dp.toPxInt :Int @Composable get() =  with(LocalDensity.current) { this@toPxInt.toPx().toInt() }
val Dp.toPx :Float @Composable get() =  with(LocalDensity.current) { this@toPx.toPx() }
val Dp.toSp :TextUnit @Composable get() =  with(LocalDensity.current) { this@toSp.toSp() }
val Int.px :Dp @Composable get() =  with(LocalDensity.current) { this@px.toDp() }