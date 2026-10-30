package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.GlassBackgroundType
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GlassBackgroundView(
    backgroundType: GlassBackgroundType,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "backgroundAnim")
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animTime"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier.fillMaxSize()) {
        when (backgroundType) {
            GlassBackgroundType.IOS_WALLPAPER -> {
                Image(
                    painter = painterResource(id = R.drawable.ios_wallpaper),
                    contentDescription = "iOS Glass Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Floating subtle ambient light orbs over wallpaper
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val orb1X = size.width * (0.3f + 0.2f * sin(animTime))
                    val orb1Y = size.height * (0.4f + 0.15f * cos(animTime * 1.3f))
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x4038BDF8), Color.Transparent),
                            center = Offset(orb1X, orb1Y),
                            radius = size.width * 0.45f
                        ),
                        center = Offset(orb1X, orb1Y),
                        radius = size.width * 0.45f
                    )

                    val orb2X = size.width * (0.7f - 0.25f * cos(animTime * 0.9f))
                    val orb2Y = size.height * (0.65f - 0.2f * sin(animTime * 1.1f))
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x40EC4899), Color.Transparent),
                            center = Offset(orb2X, orb2Y),
                            radius = size.width * 0.4f
                        ),
                        center = Offset(orb2X, orb2Y),
                        radius = size.width * 0.4f
                    )
                }
            }

            GlassBackgroundType.CUSTOM_USER -> {
                Image(
                    painter = painterResource(id = R.drawable.custom_user_wallpaper),
                    contentDescription = "Tu Fondo de Pantalla",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            GlassBackgroundType.DYNAMIC_MESH -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF090D16))
                ) {
                    drawDynamicMesh(animTime)
                }
            }

            GlassBackgroundType.GEOMETRIC_GRID -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B0F19))
                ) {
                    drawOpticalGrid(animTime)
                }
            }

            GlassBackgroundType.TYPOGRAPHY_WARP -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0A0E1A))
                ) {
                    drawRefractionTypography(textMeasurer, animTime)
                }
            }
        }
    }
}

private fun DrawScope.drawDynamicMesh(t: Float) {
    // Deep vibrant base
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF030712)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        )
    )

    // Moving fluid mesh gradients
    val c1 = Offset(size.width * (0.4f + 0.35f * sin(t)), size.height * (0.3f + 0.2f * cos(t * 0.7f)))
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x998B5CF6), Color(0x336366F1), Color.Transparent),
            center = c1,
            radius = size.width * 0.6f
        ),
        center = c1,
        radius = size.width * 0.6f
    )

    val c2 = Offset(size.width * (0.65f - 0.3f * cos(t * 1.2f)), size.height * (0.7f + 0.2f * sin(t * 0.9f)))
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x9906B6D4), Color(0x333B82F6), Color.Transparent),
            center = c2,
            radius = size.width * 0.55f
        ),
        center = c2,
        radius = size.width * 0.55f
    )

    val c3 = Offset(size.width * (0.3f + 0.25f * cos(t * 1.5f)), size.height * (0.8f - 0.25f * sin(t)))
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x88EC4899), Color(0x22F43F5E), Color.Transparent),
            center = c3,
            radius = size.width * 0.5f
        ),
        center = c3,
        radius = size.width * 0.5f
    )
}

private fun DrawScope.drawOpticalGrid(t: Float) {
    val step = 48f
    val cols = (size.width / step).toInt() + 1
    val rows = (size.height / step).toInt() + 1

    // Fine grid lines
    for (i in 0..cols) {
        val x = i * step
        drawLine(
            color = Color(0x22FFFFFF),
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
    }
    for (j in 0..rows) {
        val y = j * step
        drawLine(
            color = Color(0x22FFFFFF),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
    }

    // Concentric calibration rings in center
    val center = Offset(size.width / 2f, size.height / 2f)
    for (k in 1..7) {
        val r = k * 55f + 10f * sin(t + k.toFloat())
        drawCircle(
            color = if (k % 2 == 0) Color(0x3338BDF8) else Color(0x22EC4899),
            center = center,
            radius = r,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
        )
    }

    // Diagonal calibration lines
    drawLine(
        color = Color(0x33F59E0B),
        start = Offset(0f, 0f),
        end = Offset(size.width, size.height),
        strokeWidth = 1.2f
    )
    drawLine(
        color = Color(0x33F59E0B),
        start = Offset(size.width, 0f),
        end = Offset(0f, size.height),
        strokeWidth = 1.2f
    )
}

private fun DrawScope.drawRefractionTypography(measurer: TextMeasurer, t: Float) {
    val titleStyle = TextStyle(
        color = Color(0x29FFFFFF),
        fontSize = 54.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.SansSerif
    )
    val subStyle = TextStyle(
        color = Color(0x4038BDF8),
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
    )
    val accentStyle = TextStyle(
        color = Color(0x33EC4899),
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.SansSerif
    )

    val words = listOf(
        "LIQUID GLASS",
        "iOS REFRACTION",
        "n = 1.458 CROWN",
        "SURFACE TENSION",
        "METABALL FLUID",
        "CAUSTICS & FRESNEL",
        "PRISMATIC SHIFT",
        "APPLE DESIGN LAB"
    )

    var currentY = 120f
    for ((index, word) in words.withIndex()) {
        val style = when (index % 3) {
            0 -> titleStyle
            1 -> subStyle
            else -> accentStyle
        }
        val layout = measurer.measure(word, style)
        val shiftX = 30f * sin(t + index)
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(40f + shiftX, currentY)
        )
        currentY += layout.size.height + 45f
    }
}
