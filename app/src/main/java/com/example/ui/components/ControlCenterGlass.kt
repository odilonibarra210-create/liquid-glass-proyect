package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GlassTintStyle
import com.example.ui.glass.liquidGlassEffect
import kotlin.math.sin

@Composable
fun ControlCenterGlass(
    tint: GlassTintStyle,
    onGlassTouch: ((Float, Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var volumeLevel by remember { mutableFloatStateOf(0.72f) }
    var brightnessLevel by remember { mutableFloatStateOf(0.85f) }

    var wifiActive by remember { mutableStateOf(true) }
    var bluetoothActive by remember { mutableStateOf(true) }
    var airplaneActive by remember { mutableStateOf(false) }
    var flashlightActive by remember { mutableStateOf(false) }
    var focusActive by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Centro de Control iOS",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Material de Cristal Líquido & Sliders Fluidos",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 12.sp
                )
            }
        }

        // Top Row: Connectivity (2x2) and Now Playing card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Connectivity module (Frosted glass container with 4 tiles)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .liquidGlassEffect(
                        shape = RoundedCornerShape(26.dp),
                        tint = tint,
                        elevation = 8.dp
                    )
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ControlTileButton(
                            icon = Icons.Default.AirplanemodeActive,
                            isActive = airplaneActive,
                            activeColor = Color(0xFFF59E0B),
                            onClick = { airplaneActive = !airplaneActive }
                        )
                        ControlTileButton(
                            icon = Icons.Default.Wifi,
                            isActive = wifiActive,
                            activeColor = Color(0xFF0284C7),
                            onClick = { wifiActive = !wifiActive }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ControlTileButton(
                            icon = Icons.Default.Bluetooth,
                            isActive = bluetoothActive,
                            activeColor = Color(0xFF2563EB),
                            onClick = { bluetoothActive = !bluetoothActive }
                        )
                        ControlTileButton(
                            icon = Icons.Default.GraphicEq,
                            isActive = true,
                            activeColor = Color(0xFF10B981),
                            onClick = {}
                        )
                    }
                }
            }

            // Media Player Module
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .liquidGlassEffect(
                        shape = RoundedCornerShape(26.dp),
                        tint = tint,
                        elevation = 8.dp
                    )
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Ahora Suena",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reflexión Líquida",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cupertino Wave",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // Middle Row: Fluid Sliders (Volume & Brightness with dynamic liquid meniscus!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Brightness Liquid Slider
            LiquidVerticalSlider(
                value = brightnessLevel,
                onValueChange = { brightnessLevel = it },
                icon = Icons.Default.BrightnessHigh,
                tint = tint,
                label = "Brillo",
                modifier = Modifier.weight(1f)
            )

            // Volume Liquid Slider
            LiquidVerticalSlider(
                value = volumeLevel,
                onValueChange = { volumeLevel = it },
                icon = Icons.Default.VolumeUp,
                tint = tint,
                label = "Volumen",
                modifier = Modifier.weight(1f)
            )
        }

        // Bottom Grid: Quick action frosted tiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            QuickGlassActionTile(
                icon = Icons.Default.FlashlightOn,
                isActive = flashlightActive,
                activeColor = Color(0xFFFBBF24),
                label = "Linterna",
                tint = tint,
                modifier = Modifier.weight(1f),
                onClick = { flashlightActive = !flashlightActive }
            )
            QuickGlassActionTile(
                icon = Icons.Default.DoNotDisturbOn,
                isActive = focusActive,
                activeColor = Color(0xFF8B5CF6),
                label = "Concentración",
                tint = tint,
                modifier = Modifier.weight(1f),
                onClick = { focusActive = !focusActive }
            )
            QuickGlassActionTile(
                icon = Icons.Default.ScreenShare,
                isActive = false,
                activeColor = Color(0xFF38BDF8),
                label = "Duplicar",
                tint = tint,
                modifier = Modifier.weight(1f),
                onClick = {}
            )
            QuickGlassActionTile(
                icon = Icons.Default.CameraAlt,
                isActive = false,
                activeColor = Color.White,
                label = "Cámara",
                tint = tint,
                modifier = Modifier.weight(1f),
                onClick = {}
            )
        }
    }
}

@Composable
fun LiquidVerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    icon: ImageVector,
    tint: GlassTintStyle,
    label: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "slosh")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1600, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "phase"
    )

    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.65f, stiffness = 350f),
        label = "sliderLevel"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .liquidGlassEffect(
                shape = RoundedCornerShape(26.dp),
                tint = tint,
                elevation = 3.dp
            )
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val newLevel = (1f - (change.position.y / size.height)).coerceIn(0f, 1f)
                    onValueChange(newLevel)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newLevel = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
                    onValueChange(newLevel)
                }
            }
    ) {
        // Liquid filling canvas with dynamic fluid sloshing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fillHeight = size.height * animatedValue
            val topY = size.height - fillHeight

            val sloshAmp = if (animatedValue in 0.05f..0.95f) 5.5f else 1.5f
            val fluidPath = Path().apply {
                moveTo(0f, size.height)
                lineTo(0f, topY)
                val wave1 = (sin(wavePhase.toDouble()) * sloshAmp).toFloat()
                val wave2 = (sin((wavePhase + 2.2f).toDouble()) * sloshAmp).toFloat()
                quadraticTo(
                    size.width * 0.35f,
                    topY + wave1,
                    size.width * 0.7f,
                    topY + wave2
                )
                quadraticTo(
                    size.width * 0.88f,
                    topY - wave1,
                    size.width,
                    topY
                )
                lineTo(size.width, size.height)
                close()
            }

            drawPath(
                path = fluidPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.82f)
                    ),
                    startY = topY,
                    endY = size.height
                )
            )

            // Specular meniscus highlight line
            if (animatedValue > 0.02f) {
                drawLine(
                    color = Color.White,
                    start = Offset(0f, topY),
                    end = Offset(size.width, topY),
                    strokeWidth = 2.5f
                )
            }
        }

        // Bottom icon
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (value > 0.18f) Color(0xFF1E293B) else Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // Percentage text at top
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            Text(
                text = "${(value * 100).toInt()}%",
                color = if (value > 0.92f) Color(0xFF1E293B) else Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ControlTileButton(
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(
                if (isActive) activeColor else Color.White.copy(alpha = 0.16f)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun QuickGlassActionTile(
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    label: String,
    tint: GlassTintStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(76.dp)
            .liquidGlassEffect(
                shape = RoundedCornerShape(20.dp),
                tint = tint,
                elevation = 6.dp
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) activeColor else Color.White.copy(alpha = 0.18f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive && activeColor == Color.White) Color.Black else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
