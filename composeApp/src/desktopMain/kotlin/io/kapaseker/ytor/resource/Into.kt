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

@Composable
fun StringResource.inString(vararg formatArgs: Any):String {
    return stringResource(this, *formatArgs)
}

@Composable
fun StringArrayResource.inStringArray():List<String> {
    return stringArrayResource(this)
}
//
//fun @receiver:StringRes Int.getString():String {
//    return Utils.getApp().resources.getString(this)
//}
//
//fun @receiver:StringRes Int.getString(vararg formatArgs: Any):String {
//    return Utils.getApp().resources.getString(this, *formatArgs)
//}
//
//fun @receiver:StringRes Int.getStringArray():Array<String> {
//    return Utils.getApp().resources.getStringArray(this)
//}
//
//@Composable
//@ReadOnlyComposable
//fun @receiver:DimenRes Int.inDp(): Dp {
//    return dimensionResource(id = this)
//}
//
//@Composable
//fun @receiver:DimenRes Int.inPx(): Float {
//    return dimensionResource(id = this).toPx
//}
//
//@Composable
//fun @receiver:DimenRes Int.inSp(): TextUnit {
//    return dimensionResource(id = this).toSp
//}
//
//@Composable
//@ReadOnlyComposable
//fun Int.inColor(): Color {
//    return colorResource(id = this)
//}
//
//@Composable
//fun @receiver:ColorRes Int.colorInBrush(): Brush {
//    return SolidColor(colorResource(id = this))
//}
//
@Composable
fun  DrawableResource.inPainter(): Painter {
    return painterResource(this)
}


val Dp.toPxInt :Int @Composable get() =  with(LocalDensity.current) { this@toPxInt.toPx().toInt() }
val Dp.toPx :Float @Composable get() =  with(LocalDensity.current) { this@toPx.toPx() }
val Dp.toSp :TextUnit @Composable get() =  with(LocalDensity.current) { this@toSp.toSp() }
val Int.px :Dp @Composable get() =  with(LocalDensity.current) { this@px.toDp() }