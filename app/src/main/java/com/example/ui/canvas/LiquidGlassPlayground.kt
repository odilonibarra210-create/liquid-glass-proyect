package com.example.ui.canvas

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.South
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GlassConfig
import com.example.model.GlassTintStyle
import com.example.model.LiquidDroplet
import com.example.physics.LiquidPhysicsEngine
import com.example.ui.glass.GlassRenderer
import com.example.ui.glass.liquidGlassEffect
import kotlin.math.sqrt

@Composable
fun LiquidGlassPlayground(
    physicsEngine: LiquidPhysicsEngine,
    config: GlassConfig,
    onConfigChange: (GlassConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }
    var draggedDroplet by remember { mutableStateOf<LiquidDroplet?>(null) }
    var isLensActive by remember { mutableStateOf(false) }
    var lensCenter by remember { mutableStateOf(Offset(200f, 300f)) }

    fun triggerHaptic(strong: Boolean = false) {
        if (!config.hapticsEnabled || vibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (strong) {
                    VibrationEffect.createOneShot(28, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createOneShot(12, 100)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(15)
            }
        } catch (_: Exception) {}
    }

    // Set callback for fusion splash
    LaunchedEffect(physicsEngine) {
        physicsEngine.onFusionOccurred = {
            triggerHaptic(strong = true)
        }
    }

    // Continuous 60fps physics simulation loop
    LaunchedEffect(canvasWidth, canvasHeight, config) {
        if (canvasWidth > 0 && canvasHeight > 0) {
            if (physicsEngine.droplets.isEmpty()) {
                physicsEngine.initializeDefaultDroplets(canvasWidth, canvasHeight)
            }
        }
        var lastNano = System.nanoTime()
        while (true) {
            withFrameNanos { now ->
                val dt = ((now - lastNano) / 1_000_000_000f).coerceIn(0.005f, 0.033f)
                lastNano = now
                physicsEngine.update(
                    boundsWidth = canvasWidth,
                    boundsHeight = canvasHeight,
                    config = config,
                    dt = dt * 60f
                )
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Interactive Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("liquid_glass_canvas")
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            val touched = physicsEngine.findDropletAt(offset.x, offset.y)
                            if (touched != null) {
                                // Poke droplet
                                touched.squishX = 1.35f
                                touched.squishY = 0.65f
                                touched.bounceVelocity = 0.25f
                                physicsEngine.addRipple(touched.x, touched.y, touched.radius * 2.8f)
                                triggerHaptic(false)
                            } else {
                                // Tap creates liquid wave
                                physicsEngine.addRipple(offset.x, offset.y, 240f)
                                triggerHaptic(false)
                            }
                        },
                        onDoubleTap = { offset ->
                            val touched = physicsEngine.findDropletAt(offset.x, offset.y)
                            if (touched != null) {
                                val split = physicsEngine.splitDroplet(touched.id)
                                if (split) triggerHaptic(true)
                            } else {
                                // Spawn new droplet on double tap
                                physicsEngine.addDroplet(offset.x, offset.y, radius = 48f)
                                triggerHaptic(true)
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (isLensActive && (offset - lensCenter).getDistance() < 90f) {
                                // Dragging lens
                                return@detectDragGestures
                            }
                            val target = physicsEngine.findDropletAt(offset.x, offset.y)
                            if (target != null) {
                                target.isDragging = true
                                target.vx = 0f
                                target.vy = 0f
                                draggedDroplet = target
                                triggerHaptic(false)
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (isLensActive && (change.position - lensCenter).getDistance() < 120f) {
                                lensCenter += dragAmount
                            } else {
                                draggedDroplet?.let { d ->
                                    d.x = (d.x + dragAmount.x).coerceIn(d.radius, canvasWidth - d.radius)
                                    d.y = (d.y + dragAmount.y).coerceIn(d.radius, canvasHeight - d.radius)
                                    d.vx = dragAmount.x * 0.85f
                                    d.vy = dragAmount.y * 0.85f

                                    // Dynamic fluid elongation squish along drag
                                    val dragSpeed = sqrt(dragAmount.x * dragAmount.x + dragAmount.y * dragAmount.y)
                                    d.squishX = (1f + dragSpeed * 0.025f).coerceIn(0.7f, 1.45f)
                                    d.squishY = (2f - d.squishX).coerceIn(0.6f, 1.3f)

                                    // Emit subtle fluid wake ripples during movement
                                    if (dragSpeed > 6f && Math.random() < 0.25) {
                                        physicsEngine.addRipple(d.x, d.y, maxRadius = d.radius * 2.2f, color = Color.White.copy(alpha = 0.35f))
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            draggedDroplet?.isDragging = false
                            draggedDroplet = null
                        },
                        onDragCancel = {
                            draggedDroplet?.isDragging = false
                            draggedDroplet = null
                        }
                    )
                }
        ) {
            canvasWidth = size.width
            canvasHeight = size.height

            // 1. Draw Metaball fluid bridges between droplets
            if (config.showMetaballBridges) {
                for (i in physicsEngine.droplets.indices) {
                    for (j in (i + 1) until physicsEngine.droplets.size) {
                        GlassRenderer.drawMetaballBridge(
                            drawScope = this,
                            d1 = physicsEngine.droplets[i],
                            d2 = physicsEngine.droplets[j],
                            config = config
                        )
                    }
                }
            }

            // 2. Draw all liquid droplets
            for (droplet in physicsEngine.droplets) {
                GlassRenderer.drawDroplet(
                    drawScope = this,
                    droplet = droplet,
                    config = config
                )
            }

            // 3. Draw refractive wave ripples
            for (ripple in physicsEngine.ripples) {
                GlassRenderer.drawRipple(
                    drawScope = this,
                    ripple = ripple,
                    config = config
                )
            }

            // 4. Draw optical lens magnifier if enabled
            if (isLensActive) {
                GlassRenderer.drawRefractionLens(
                    drawScope = this,
                    lensCenter = lensCenter,
                    lensRadius = 80f,
                    config = config
                )
            }
        }

        // Top Status HUD (Droplet telemetry & hint)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        ) {
            Row(
                modifier = Modifier
                    .liquidGlassEffect(
                        shape = RoundedCornerShape(20.dp),
                        tint = GlassTintStyle.OBSIDIAN,
                        elevation = 6.dp
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF34D399))
                )
                Text(
                    text = "${physicsEngine.droplets.size} Gotas de Cristal",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "•",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp
                )
                Text(
                    text = "Toca o Arrastra para Fusionar",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }
        }

        // Floating Bottom Quick Action Dock
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        ) {
            Row(
                modifier = Modifier
                    .liquidGlassEffect(
                        shape = RoundedCornerShape(30.dp),
                        tint = config.tintStyle,
                        elevation = 12.dp
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Add Droplet Action
                DockActionButton(
                    icon = Icons.Default.Add,
                    label = "Añadir",
                    onClick = {
                        val newX = (canvasWidth * 0.3f + Math.random().toFloat() * canvasWidth * 0.4f)
                        val newY = (canvasHeight * 0.3f + Math.random().toFloat() * canvasHeight * 0.4f)
                        physicsEngine.addDroplet(newX, newY, radius = (38f + Math.random().toFloat() * 25f))
                        triggerHaptic(true)
                    }
                )

                // Gravity Toggle
                DockActionButton(
                    icon = Icons.Default.South,
                    label = "Gravedad",
                    isActive = config.gravityEnabled,
                    activeColor = Color(0xFFF59E0B),
                    onClick = {
                        onConfigChange(config.copy(gravityEnabled = !config.gravityEnabled))
                        triggerHaptic(false)
                    }
                )

                // Lens Magnifier Toggle
                DockActionButton(
                    icon = Icons.Default.Search,
                    label = "Lente",
                    isActive = isLensActive,
                    activeColor = Color(0xFF38BDF8),
                    onClick = {
                        isLensActive = !isLensActive
                        lensCenter = Offset(canvasWidth / 2f, canvasHeight / 2f)
                        triggerHaptic(false)
                    }
                )

                // Cycle Tint Style
                DockActionButton(
                    icon = Icons.Default.ColorLens,
                    label = "Tinte",
                    onClick = {
                        val values = GlassTintStyle.values()
                        val nextIndex = (config.tintStyle.ordinal + 1) % values.size
                        onConfigChange(config.copy(tintStyle = values[nextIndex]))
                        triggerHaptic(false)
                    }
                )

                // Reset canvas
                DockActionButton(
                    icon = Icons.Default.Refresh,
                    label = "Limpiar",
                    onClick = {
                        physicsEngine.initializeDefaultDroplets(canvasWidth, canvasHeight)
                        triggerHaptic(true)
                    }
                )
            }
        }
    }
}

@Composable
private fun DockActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean = false,
    activeColor: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) activeColor else Color.White.copy(alpha = 0.16f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive && activeColor != Color.White) Color.White else (if (isActive) Color.Black else Color.White),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
