package com.example.ui.micro

import androidx.compose.animation.AnimatedVisibility
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GlassTintStyle
import com.example.ui.glass.liquidGlassEffect
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class MolecularMesogen(
    val id: Int,
    var x: Float,
    var y: Float,
    var angle: Float,
    var targetAngle: Float,
    val length: Float = 36f,
    val width: Float = 9f,
    val hue: Float = 200f
)

@Composable
fun MicroscopicZoomOverlay(
    isActive: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(400))
    ) {
        val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
        val animZoom = remember { Animatable(1f) }
        var touchDistortOffset by remember { mutableStateOf(Offset.Zero) }
        var touchDistortStrength by remember { mutableFloatStateOf(0f) }

        // Animate up to 20,000x when activated
        LaunchedEffect(isActive) {
            if (isActive) {
                animZoom.snapTo(1f)
                animZoom.animateTo(
                    targetValue = 20000f,
                    animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing)
                )
            }
        }

        val infiniteTransition = rememberInfiniteTransition(label = "molecularJiggle")
        val jigglePhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 6.28318f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "jiggle"
        )

        val currentZoom = animZoom.value

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            touchDistortOffset = offset
                            touchDistortStrength = 1f
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            touchDistortOffset = change.position
                            touchDistortStrength = 1f
                        },
                        onDragEnd = {
                            touchDistortStrength = 0f
                        },
                        onDragCancel = {
                            touchDistortStrength = 0f
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            touchDistortOffset = offset
                            touchDistortStrength = 1f
                        }
                    )
                }
        ) {
            // Molecular & Optical Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawMicroscopicView(
                    zoom = currentZoom,
                    jigglePhase = jigglePhase,
                    touchOffset = touchDistortOffset,
                    touchStrength = touchDistortStrength
                )
            }

            // Top Telemetry Bar
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassEffect(
                            shape = RoundedCornerShape(26.dp),
                            tint = GlassTintStyle.OBSIDIAN,
                            blurAlpha = 0.95f,
                            elevation = 16.dp
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8).copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ZOOM ",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${currentZoom.toInt()}x",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = if (currentZoom > 10000f) "Red Molecular Nemática (50 nm)" else "Transición Óptica",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Close Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Crosshair overlay and nanometer scale
            CrosshairScaleOverlay(
                zoom = currentZoom,
                modifier = Modifier.align(Alignment.Center)
            )

            // Bottom Controls Bar (Manual Zoom Scrubbing & Gesture Hint)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Info Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassEffect(
                            shape = RoundedCornerShape(22.dp),
                            tint = GlassTintStyle.OBSIDIAN,
                            blurAlpha = 0.95f,
                            elevation = 12.dp
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Alineación de Moléculas 5CB",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Birefringencia Activa",
                                color = Color(0xFF34D399),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Manual zoom scrubber (Logarithmic scale 1 to 20,000)
                        val logProgress = (log10(currentZoom.coerceAtLeast(1f)) / log10(20000f)).coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                            Slider(
                                value = logProgress,
                                onValueChange = { norm ->
                                    val newZoom = 10.0.pow(norm * log10(20000.0)).toFloat()
                                    coroutineScope.launch {
                                        animZoom.snapTo(newZoom)
                                    }
                                },
                                enabled = true,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF38BDF8),
                                    activeTrackColor = Color(0xFF38BDF8),
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "👆 Arrastra el dedo para desviar la polarización",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 11.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                                    .clickable { onDismiss() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Volver a 1x", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawMicroscopicView(
    zoom: Float,
    jigglePhase: Float,
    touchOffset: Offset,
    touchStrength: Float
) {
    val center = Offset(size.width / 2f, size.height / 2f)

    // Stage 1: Macro background to Deep Molecular (1x to 20,000x)
    val zoomFactorNorm = (zoom / 20000f).coerceIn(0f, 1f)

    // Base background: Polarized chromatic interference colors
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF1E1B4B),
                Color(0xFF0F172A),
                Color(0xFF020617)
            ),
            center = center,
            radius = size.width
        )
    )

    // Optical Birefringence Rainbow Clouds (characteristic of liquid crystals under polarized microscopy)
    val rainbowColors = listOf(
        Color(0x33EC4899),
        Color(0x338B5CF6),
        Color(0x3306B6D4),
        Color(0x3310B981),
        Color(0x33F59E0B)
    )

    for (k in 0..4) {
        val angle = jigglePhase * 0.4f + k * 1.25f
        val dist = 140f + 60f * sin(jigglePhase + k)
        val cloudCenter = Offset(center.x + cos(angle) * dist, center.y + sin(angle) * dist)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(rainbowColors[k % rainbowColors.size], Color.Transparent),
                center = cloudCenter,
                radius = 280f
            ),
            center = cloudCenter,
            radius = 280f
        )
    }

    // Molecular Lattice of Liquid Crystal (Nematic Director Molecules)
    // As zoom increases towards 20,000x, molecules become huge and clearly defined rods
    val gridStep = 42f
    val cols = (size.width / gridStep).toInt() + 1
    val rows = (size.height / gridStep).toInt() + 1

    val moleculeAlpha = (zoomFactorNorm * 1.2f).coerceIn(0.15f, 1f)

    for (i in 0..cols) {
        for (j in 0..rows) {
            val posX = i * gridStep + ((j % 2) * gridStep * 0.5f)
            val posY = j * gridStep

            // Base alignment director with fluid hydrodynamic wave motion
            val fluidTurbulence = sin(posX * 0.012f + jigglePhase * 1.4f) * cos(posY * 0.012f + jigglePhase * 0.9f)
            var baseAngle = 0.55f + 0.32f * fluidTurbulence

            // Touch perturbation: creates fluid vortex swirls
            if (touchStrength > 0.05f) {
                val dx = posX - touchOffset.x
                val dy = posY - touchOffset.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 280f) {
                    val influence = (1f - dist / 280f) * touchStrength
                    val vortexAngle = atan2(dy, dx) + (Math.PI / 2).toFloat() * 0.6f
                    baseAngle += vortexAngle * influence * 1.8f
                }
            }

            // Molecular Mesogen dimensions scaled with zoom
            val molLength = (24f + 16f * zoomFactorNorm)
            val molWidth = (6f + 5f * zoomFactorNorm)

            val p1x = posX - cos(baseAngle) * (molLength * 0.5f)
            val p1y = posY - sin(baseAngle) * (molLength * 0.5f)
            val p2x = posX + cos(baseAngle) * (molLength * 0.5f)
            val p2y = posY + sin(baseAngle) * (molLength * 0.5f)

            // Molecule rod gradient (biphasic polar mesogen)
            val mesogenColor1 = if ((i + j) % 2 == 0) Color(0xFF38BDF8) else Color(0xFFC084FC)
            val mesogenColor2 = if ((i + j) % 3 == 0) Color(0xFFF472B6) else Color(0xFF34D399)

            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        mesogenColor1.copy(alpha = moleculeAlpha),
                        mesogenColor2.copy(alpha = moleculeAlpha)
                    ),
                    start = Offset(p1x, p1y),
                    end = Offset(p2x, p2y)
                ),
                start = Offset(p1x, p1y),
                end = Offset(p2x, p2y),
                strokeWidth = molWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // Specular core dot on each molecule at 20,000x
            if (zoom > 8000f) {
                drawCircle(
                    color = Color.White.copy(alpha = moleculeAlpha * 0.75f),
                    center = Offset(posX, posY),
                    radius = molWidth * 0.28f
                )
            }
        }
    }

    // Vignette and microscope ocular ring
    drawCircle(
        color = Color.Black.copy(alpha = 0.35f),
        center = center,
        radius = size.width * 0.75f,
        style = Stroke(width = size.width * 0.4f)
    )
}

@Composable
private fun CrosshairScaleOverlay(
    zoom: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Center target rings
            drawCircle(
                color = Color(0x6638BDF8),
                center = center,
                radius = 70f,
                style = Stroke(width = 1.2f)
            )
            drawCircle(
                color = Color(0x3338BDF8),
                center = center,
                radius = 110f,
                style = Stroke(width = 1f)
            )

            // Crosshair ticks
            drawLine(
                color = Color(0x8838BDF8),
                start = Offset(center.x - 25f, center.y),
                end = Offset(center.x + 25f, center.y),
                strokeWidth = 1.5f
            )
            drawLine(
                color = Color(0x8838BDF8),
                start = Offset(center.x, center.y - 25f),
                end = Offset(center.x, center.y + 25f),
                strokeWidth = 1.5f
            )

            // Scale bar at bottom of crosshair
            val scaleWidth = 90f
            val scaleY = center.y + 85f
            drawLine(
                color = Color.White,
                start = Offset(center.x - scaleWidth / 2f, scaleY),
                end = Offset(center.x + scaleWidth / 2f, scaleY),
                strokeWidth = 2.5f
            )
            drawLine(
                color = Color.White,
                start = Offset(center.x - scaleWidth / 2f, scaleY - 5f),
                end = Offset(center.x - scaleWidth / 2f, scaleY + 5f),
                strokeWidth = 2.5f
            )
            drawLine(
                color = Color.White,
                start = Offset(center.x + scaleWidth / 2f, scaleY - 5f),
                end = Offset(center.x + scaleWidth / 2f, scaleY + 5f),
                strokeWidth = 2.5f
            )
        }

        // Scale bar label text
        Text(
            text = if (zoom >= 15000f) "50 nm" else if (zoom >= 5000f) "200 nm" else "1 µm",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
    }
}
