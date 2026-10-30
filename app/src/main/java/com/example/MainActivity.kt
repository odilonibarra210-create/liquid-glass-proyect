package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GlassAppTab
import com.example.model.GlassTintStyle
import com.example.ui.canvas.LiquidGlassPlayground
import com.example.ui.components.ControlCenterGlass
import com.example.ui.components.DynamicIslandGlass
import com.example.ui.components.GlassBackgroundView
import com.example.ui.components.WidgetsGlassShowcase
import com.example.ui.glass.liquidGlassEffect
import com.example.ui.lab.GlassLabScreen
import com.example.ui.micro.MicroscopicZoomOverlay
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GlassViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LiquidGlassApp()
            }
        }
    }
}

@Composable
fun LiquidGlassApp(viewModel: GlassViewModel = viewModel()) {
    val config by viewModel.config.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val islandMode by viewModel.islandMode.collectAsState()
    val isMicroscopicZoomActive by viewModel.isMicroscopicZoomActive.collectAsState()

    // Handle back button navigation
    BackHandler(enabled = isMicroscopicZoomActive || activeTab != GlassAppTab.PLAYGROUND) {
        if (isMicroscopicZoomActive) {
            viewModel.setMicroscopicZoom(false)
        } else {
            viewModel.setTab(GlassAppTab.PLAYGROUND)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // Three-finger touch gesture detector: expands and triggers 20,000x zoom!
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val pressed = event.changes.filter { it.pressed }
                            if (pressed.size >= 3) {
                                viewModel.setMicroscopicZoom(true)
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
                }
        ) {
            // 1. Base Layer: Dynamic background (Wallpaper, mesh, grid or typography)
            GlassBackgroundView(backgroundType = config.backgroundType)

            // 2. Main Content Screens
            Crossfade(
                targetState = activeTab,
                label = "tabCrossfade",
                modifier = Modifier.fillMaxSize()
            ) { tab ->
                when (tab) {
                    GlassAppTab.PLAYGROUND -> {
                        LiquidGlassPlayground(
                            physicsEngine = viewModel.physicsEngine,
                            config = config,
                            onConfigChange = { viewModel.updateConfig(it) }
                        )
                    }

                    GlassAppTab.IOS_SHOWCASE -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 110.dp, bottom = 90.dp)
                        ) {
                            item {
                                ControlCenterGlass(tint = config.tintStyle)
                            }
                            item {
                                WidgetsGlassShowcase(config = config)
                            }
                            item {
                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }
                    }

                    GlassAppTab.LAB -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 100.dp)
                        ) {
                            GlassLabScreen(
                                config = config,
                                onConfigChange = { viewModel.updateConfig(it) }
                            )
                        }
                    }
                }
            }

            // 3. Top Floating Dynamic Island
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 10.dp)
            ) {
                DynamicIslandGlass(
                    mode = islandMode,
                    tint = config.tintStyle,
                    onModeChange = { viewModel.setIslandMode(it) }
                )
            }

            // 4. Floating 3-Finger Gesture Shortcut Pill
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 68.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .testTag("three_finger_zoom_button")
                        .liquidGlassEffect(
                            shape = RoundedCornerShape(20.dp),
                            tint = GlassTintStyle.AURORA,
                            blurAlpha = 0.9f,
                            elevation = 8.dp
                        )
                        .clickable { viewModel.toggleMicroscopicZoom() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = "Zoom 20,000x",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "3 Dedos: 20,000x",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 5. Floating Bottom iOS Glass Navigation Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp, start = 20.dp, end = 20.dp)
            ) {
                FloatingGlassNavigationBar(
                    selectedTab = activeTab,
                    onTabSelected = { viewModel.setTab(it) }
                )
            }

            // 6. 20,000x Microscopic Screen Expansion Overlay
            MicroscopicZoomOverlay(
                isActive = isMicroscopicZoomActive,
                onDismiss = { viewModel.setMicroscopicZoom(false) }
            )
        }
    }
}

@Composable
fun FloatingGlassNavigationBar(
    selectedTab: GlassAppTab,
    onTabSelected: (GlassAppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassEffect(
                shape = RoundedCornerShape(32.dp),
                tint = com.example.model.GlassTintStyle.OBSIDIAN,
                elevation = 14.dp,
                blurAlpha = 0.95f
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassNavTabItem(
            title = "Líquido",
            icon = Icons.Default.WaterDrop,
            isSelected = selectedTab == GlassAppTab.PLAYGROUND,
            testTag = "nav_tab_liquid",
            onClick = { onTabSelected(GlassAppTab.PLAYGROUND) }
        )

        GlassNavTabItem(
            title = "Interfaz iOS",
            icon = Icons.Default.Widgets,
            isSelected = selectedTab == GlassAppTab.IOS_SHOWCASE,
            testTag = "nav_tab_showcase",
            onClick = { onTabSelected(GlassAppTab.IOS_SHOWCASE) }
        )

        GlassNavTabItem(
            title = "Laboratorio",
            icon = Icons.Default.Tune,
            isSelected = selectedTab == GlassAppTab.LAB,
            testTag = "nav_tab_lab",
            onClick = { onTabSelected(GlassAppTab.LAB) }
        )
    }
}

@Composable
private fun GlassNavTabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.28f) else Color.Transparent
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.65f),
                modifier = Modifier.size(20.dp)
            )
            if (isSelected) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
