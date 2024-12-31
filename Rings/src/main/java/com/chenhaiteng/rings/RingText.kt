package com.chenhaiteng.rings
import android.content.Context
import androidx.collection.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.fitSquare
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.fullBoundingBox
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.Canvas

data class RingTextComponent(val text: String, val color:Color, val style: TextStyle)

fun cachedText(context: Context,
               text: Array<RingTextComponent>,
               measurer: TextMeasurer,
               size: Size,
               fontSize: Float,
               drawingRect: Rect,
               textDegrees: Float = 0f,
               layoutDirection: LayoutDirection,
               outline: Float = 0f,
               outlineColor: Color? = null,
               shadow: Shadow? = null,
               showBlueprint: Boolean = false): ImageBitmap = ImageBitmap(size.width.toInt(), size.height.toInt()).apply {
    println("[RingText] start cache")
    val textCanvas = Canvas(this)
    val step = 360f / text.size
    CanvasDrawScope().draw(
        Density(context),
        layoutDirection = layoutDirection,
        canvas = textCanvas,
        size = size
    ) {
        drawRect(color = Color.Transparent)
        text.forEachIndexed { i, component ->
            val layout = measurer.measure(
                component.text,
                style = component.style.copy(fontSize = fontSize.toSp())
            )
            val bound = layout.fullBoundingBox()
            val offset = Offset(drawingRect.center.x - bound.width / 2f, drawingRect.top)
            rotate(step * i, drawingRect.center) {
                if (showBlueprint) {
                    //show blue print
                    drawTextBlueprint(
                        textBound = bound,
                        offset = offset,
                        baseline = layout.lastBaseline
                    )
                }
                rotate(textDegrees, bound.translate(offset).center) {
                    if (outline > 0.0f) {
                        drawText(
                            layout,
                            topLeft = offset,
                            color = outlineColor ?: component.color,
                            drawStyle = Stroke(width = outline)
                        )
                    }
                    drawText(
                        layout,
                        topLeft = offset,
                        color = component.color,
                        shadow = shadow,
                        drawStyle = Fill
                    )
                }
            }
        }
    }
}

private var _ringTextCache: LruCache<String, ImageBitmap> = object : LruCache<String, ImageBitmap>(1024*1024*4*2) {
    override fun sizeOf(key: String, value: ImageBitmap): Int  = value.asAndroidBitmap().byteCount
}

val defaultCache = _ringTextCache

fun Context.cachedRingText(name:String, size: Size = Size.Zero, cache: LruCache<String, ImageBitmap> = defaultCache, init: ()->ImageBitmap): ImageBitmap {
    val key = "$name-${size.width.toInt()}x${size.height.toInt()}"
    return cache[key] ?: init().apply {
        cache.put(key, this)
    }
}

@Composable
fun RingText(modifier: Modifier,
             name: String,
             text: Array<RingTextComponent>,
             fontRatio: Float = 0.2f,
             textDegrees: Float = 0f,
             insetRatio: Float = 0f,
             shadow: Shadow? = null,
             outline: Float = 0.0f,
             outlineColor: Color? = null,
             brush: Brush? = null,
             blendMode: BlendMode = BlendMode.Src,
             showBlueprint: Boolean = false) {
    val context = LocalContext.current
    val measurer = rememberTextMeasurer()

    Canvas(modifier = modifier.graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen
    }.drawWithCache {
        val center = Offset(size.width/2f, size.height/2f)
        val drawingRect = size.fitSquare(center, insetRatio)
        val fontSize = size.fitSquare(center).height/2f*fontRatio

        val textBitmap = context.cachedRingText(name, size) {
            cachedText(context, text, measurer, size, fontSize, drawingRect, textDegrees, layoutDirection, outline, outlineColor, shadow, showBlueprint)
        }
        onDrawWithContent {
            drawContent()
            drawImage(textBitmap, blendMode = blendMode)
            if(showBlueprint) {
                drawBlueprint(insetRatio = insetRatio)
            }
        }
    }) {
        if(brush != null) {
            drawRect(brush)
        } else {
            drawRect(color = text.first().color)
        }
    }
}