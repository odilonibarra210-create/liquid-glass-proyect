package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GlassConfig
import com.example.model.GlassTintStyle
import com.example.ui.glass.GlassRenderer
import com.example.ui.glass.liquidGlassEffect
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun WidgetsGlassShowcase(
    config: GlassConfig,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "slosh")
    val sloshPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sloshPhase"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Frosted Glass Clock
        GlassClockWidget(tint = config.tintStyle)

        // Row of 2 Widgets: Liquid Battery and Weather
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Liquid Battery Widget
            LiquidBatteryWidget(
                batteryLevel = 0.88f,
                sloshPhase = sloshPhase,
                tint = config.tintStyle,
                modifier = Modifier.weight(1f)
            )

            // Liquid Weather Widget
            LiquidWeatherWidget(
                tint = config.tintStyle,
                modifier = Modifier.weight(1f)
            )
        }

        // Draggable Refraction Lens Card
        DraggableLensDemoCard(config = config)
    }
}

@Composable
fun GlassClockWidget(
    tint: GlassTintStyle,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassEffect(
                shape = RoundedCornerShape(28.dp),
                tint = tint,
                elevation = 8.dp
            )
            .padding(vertical = 20.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Viernes, 30 de Octubre",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "09:41",
                color = Color.White,
                fontSize = 58.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-1.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "23° • Cielo despejado",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LiquidBatteryWidget(
    batteryLevel: Float,
    sloshPhase: Float,
    tint: GlassTintStyle,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .liquidGlassEffect(
                shape = RoundedCornerShape(26.dp),
                tint = tint,
                elevation = 8.dp
            )
            .padding(14.dp)
    ) {
        // Background liquid sloshing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fillHeight = size.height * batteryLevel
            val topY = size.height - fillHeight

            val wavePath = Path().apply {
                moveTo(0f, size.height)
                lineTo(0f, topY)
                val waveAmp = 5f
                quadraticTo(
                    size.width * 0.35f,
                    topY + sin(sloshPhase) * waveAmp,
                    size.width * 0.7f,
                    topY + sin(sloshPhase + 1.8f) * waveAmp
                )
                quadraticTo(
                    size.width * 0.85f,
                    topY - sin(sloshPhase) * waveAmp,
                    size.width,
                    topY
                )
                lineTo(size.width, size.height)
                close()
            }

            drawPath(
                path = wavePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF34D399).copy(alpha = 0.75f),
                        Color(0xFF059669).copy(alpha = 0.85f)
                    ),
                    startY = topY,
                    endY = size.height
                )
            )

            // Meniscus specular
            drawLine(
                color = Color(0xFFA7F3D0).copy(alpha = 0.9f),
                start = Offset(0f, topY),
                end = Offset(size.width, topY),
                strokeWidth = 2f
            )
        }

        // Foreground content
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = "${(batteryLevel * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Batería Líquida",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun LiquidWeatherWidget(
    tint: GlassTintStyle,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .liquidGlassEffect(
                shape = RoundedCornerShape(26.dp),
                tint = tint,
                elevation = 8.dp
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cupertino",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = "22°",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lluvia de Cristal",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
                Text(
                    text = "H: 26°  L: 17°",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun DraggableLensDemoCard(
    config: GlassConfig,
    modifier: Modifier = Modifier
) {
    var lensOffsetX by remember { mutableFloatStateOf(0f) }
    var lensOffsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .liquidGlassEffect(
                shape = RoundedCornerShape(26.dp),
                tint = config.tintStyle,
                elevation = 8.dp
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Lente de Refracción Óptica",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Arrastra la lente de cristal líquido sobre el panel",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("n = 1.458", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive lens track area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.6f))
            ) {
                // Background grid inside card
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (x in 0..size.width.toInt() step 30) {
                        drawLine(
                            color = Color(0x2238BDF8),
                            start = Offset(x.toFloat(), 0f),
                            end = Offset(x.toFloat(), size.height),
                            strokeWidth = 1f
                        )
                    }
                    for (y in 0..size.height.toInt() step 30) {
                        drawLine(
                            color = Color(0x2238BDF8),
                            start = Offset(0f, y.toFloat()),
                            end = Offset(size.width, y.toFloat()),
                            strokeWidth = 1f
                        )
                    }

                    // Draw lens at current drag offset
                    val defaultX = size.width / 2f
                    val defaultY = size.height / 2f
                    val currentCenter = Offset(
                        (defaultX + lensOffsetX).coerceIn(45f, size.width - 45f),
                        (defaultY + lensOffsetY).coerceIn(45f, size.height - 45f)
                    )

                    GlassRenderer.drawRefractionLens(
                        drawScope = this,
                        lensCenter = currentCenter,
                        lensRadius = 42f,
                        config = config
                    )
                }

                // Invisible touch interceptor over the lens
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                lensOffsetX += dragAmount.x
                                lensOffsetY += dragAmount.y
                            }
                        }
                )
            }
        }
    }
}
