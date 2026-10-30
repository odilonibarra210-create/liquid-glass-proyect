package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import kotlinx.coroutines.launch
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DynamicIslandMode
import com.example.model.GlassTintStyle
import com.example.ui.glass.liquidGlassEffect
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun DynamicIslandGlass(
    mode: DynamicIslandMode,
    tint: GlassTintStyle,
    onModeChange: (DynamicIslandMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val animOffsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    val animOffsetY = remember { androidx.compose.animation.core.Animatable(0f) }
    var isPlaying by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "islandWave")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave"
    )

    // Pill fluid elastic deformation with bouncy spring release
    val squishModifier = Modifier
        .offset { IntOffset(animOffsetX.value.roundToInt(), animOffsetY.value.roundToInt()) }
        .pointerInput(Unit) {
            detectDragGestures(
                onDrag = { change, dragAmount ->
                    change.consume()
                    coroutineScope.launch {
                        animOffsetX.snapTo((animOffsetX.value + dragAmount.x * 0.45f).coerceIn(-70f, 70f))
                        animOffsetY.snapTo((animOffsetY.value + dragAmount.y * 0.45f).coerceIn(-30f, 90f))
                    }
                },
                onDragEnd = {
                    coroutineScope.launch {
                        animOffsetX.animateTo(0f, spring(dampingRatio = 0.48f, stiffness = 260f))
                    }
                    coroutineScope.launch {
                        animOffsetY.animateTo(0f, spring(dampingRatio = 0.48f, stiffness = 260f))
                    }
                },
                onDragCancel = {
                    coroutineScope.launch {
                        animOffsetX.animateTo(0f, spring(dampingRatio = 0.48f, stiffness = 260f))
                    }
                    coroutineScope.launch {
                        animOffsetY.animateTo(0f, spring(dampingRatio = 0.48f, stiffness = 260f))
                    }
                }
            )
        }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(squishModifier),
        contentAlignment = Alignment.TopCenter
    ) {
        val cornerRadius = if (mode == DynamicIslandMode.COMPACT) 26.dp else 34.dp

        Box(
            modifier = Modifier
                .testTag("dynamic_island_container")
                .animateContentSize(animationSpec = spring(dampingRatio = 0.58f, stiffness = 280f))
                .liquidGlassEffect(
                    shape = RoundedCornerShape(cornerRadius),
                    tint = GlassTintStyle.OBSIDIAN,
                    blurAlpha = 0.95f,
                    elevation = 4.dp
                )
                .clickable {
                    // Tap to cycle mode
                    val nextMode = when (mode) {
                        DynamicIslandMode.COMPACT -> DynamicIslandMode.MUSIC_PLAYER
                        DynamicIslandMode.MUSIC_PLAYER -> DynamicIslandMode.INCOMING_CALL
                        DynamicIslandMode.INCOMING_CALL -> DynamicIslandMode.TIMER
                        DynamicIslandMode.TIMER -> DynamicIslandMode.COMPACT
                    }
                    onModeChange(nextMode)
                }
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            AnimatedContent(
                targetState = mode,
                label = "islandContent"
            ) { targetMode ->
                when (targetMode) {
                    DynamicIslandMode.COMPACT -> {
                        CompactIslandContent(waveScale = waveScale)
                    }
                    DynamicIslandMode.MUSIC_PLAYER -> {
                        MusicPlayerIslandContent(
                            isPlaying = isPlaying,
                            onTogglePlay = { isPlaying = !isPlaying },
                            onClose = { onModeChange(DynamicIslandMode.COMPACT) }
                        )
                    }
                    DynamicIslandMode.INCOMING_CALL -> {
                        IncomingCallIslandContent(
                            onDismiss = { onModeChange(DynamicIslandMode.COMPACT) }
                        )
                    }
                    DynamicIslandMode.TIMER -> {
                        TimerIslandContent(
                            onDismiss = { onModeChange(DynamicIslandMode.COMPACT) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactIslandContent(waveScale: Float) {
    Row(
        modifier = Modifier
            .width(180.dp)
            .height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left camera cutout indicator
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(Color(0xFF030712))
        )

        // Center mini label
        Text(
            text = "Liquid Glass",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Right audio wave visualizer
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height((8.dp.value * (0.4f + waveScale * 0.6f)).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF38BDF8))
            )
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height((14.dp.value * waveScale).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF818CF8))
            )
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height((11.dp.value * (1f - waveScale * 0.4f)).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFF472B6))
            )
        }
    }
}

@Composable
private fun MusicPlayerIslandContent(
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(310.dp)
            .padding(top = 4.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art with frosted glass glow
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .liquidGlassEffect(
                        shape = RoundedCornerShape(14.dp),
                        tint = GlassTintStyle.AURORA,
                        elevation = 6.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Refraction Harmony",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "Liquid Audio Engine • iOS 18",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            // Close / minimize button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Text("✕", color = Color.White, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(5.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFFC084FC))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Playback controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(26.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
                    .clickable { onTogglePlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun IncomingCallIslandContent(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .width(310.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFF818CF8))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Cupertino Lab",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Llamada entrante...",
                    color = Color(0xFF34D399),
                    fontSize = 12.sp
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "Rechazar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Aceptar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun TimerIslandContent(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .width(290.dp)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.HourglassTop,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Temporizador Líquido",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
                Text(
                    text = "14:28",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .clickable { onDismiss() }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text("Pausar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
