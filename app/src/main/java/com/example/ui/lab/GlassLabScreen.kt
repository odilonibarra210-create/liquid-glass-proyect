package com.example.ui.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GlassBackgroundType
import com.example.model.GlassConfig
import com.example.model.GlassTintStyle
import com.example.ui.glass.liquidGlassEffect

@Composable
fun GlassLabScreen(
    config: GlassConfig,
    onConfigChange: (GlassConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Laboratorio Óptico",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Física de fluidos, IOR y refracción de cristal iOS",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable {
                            // Reset to defaults
                            onConfigChange(GlassConfig())
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Restablecer",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Quick Presets
        item {
            Text(
                text = "PREAJUSTES DE MATERIAL",
                color = Color(0xFF38BDF8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    PresetChip(
                        name = "iOS 18 Puro",
                        isSelected = config.tintStyle == GlassTintStyle.IOS_CLEAR && config.viscosity == 0.55f,
                        onClick = {
                            onConfigChange(
                                config.copy(
                                    ior = 1.45f,
                                    blurStrength = 0.65f,
                                    viscosity = 0.55f,
                                    chromaticAberration = 0.65f,
                                    specularIntensity = 0.85f,
                                    tintStyle = GlassTintStyle.IOS_CLEAR
                                )
                            )
                        }
                    )
                }
                item {
                    PresetChip(
                        name = "Glaseado Mate",
                        isSelected = config.tintStyle == GlassTintStyle.FROSTED_WHITE,
                        onClick = {
                            onConfigChange(
                                config.copy(
                                    ior = 1.35f,
                                    blurStrength = 0.9f,
                                    viscosity = 0.6f,
                                    chromaticAberration = 0.4f,
                                    specularIntensity = 0.7f,
                                    tintStyle = GlassTintStyle.FROSTED_WHITE
                                )
                            )
                        }
                    )
                }
                item {
                    PresetChip(
                        name = "Mercurio Líquido",
                        isSelected = config.ior > 1.7f,
                        onClick = {
                            onConfigChange(
                                config.copy(
                                    ior = 1.75f,
                                    blurStrength = 0.2f,
                                    viscosity = 0.9f,
                                    chromaticAberration = 0.9f,
                                    specularIntensity = 1.0f,
                                    tintStyle = GlassTintStyle.OBSIDIAN
                                )
                            )
                        }
                    )
                }
                item {
                    PresetChip(
                        name = "Aurora Prismática",
                        isSelected = config.tintStyle == GlassTintStyle.AURORA,
                        onClick = {
                            onConfigChange(
                                config.copy(
                                    ior = 1.6f,
                                    blurStrength = 0.5f,
                                    viscosity = 0.7f,
                                    chromaticAberration = 1.0f,
                                    specularIntensity = 0.95f,
                                    tintStyle = GlassTintStyle.AURORA
                                )
                            )
                        }
                    )
                }
                item {
                    PresetChip(
                        name = "Cuarzo Rosé",
                        isSelected = config.tintStyle == GlassTintStyle.ROSE_QUARTZ,
                        onClick = {
                            onConfigChange(
                                config.copy(
                                    ior = 1.52f,
                                    blurStrength = 0.7f,
                                    viscosity = 0.65f,
                                    chromaticAberration = 0.8f,
                                    specularIntensity = 0.9f,
                                    tintStyle = GlassTintStyle.ROSE_QUARTZ
                                )
                            )
                        }
                    )
                }
            }
        }

        // Optical Parameters Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassEffect(
                        shape = RoundedCornerShape(26.dp),
                        tint = config.tintStyle,
                        elevation = 8.dp
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Propiedades Ópticas",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // IOR
                    LabSliderRow(
                        title = "Índice de Refracción (IOR)",
                        valueText = String.format("%.2f", config.ior),
                        subtext = if (config.ior < 1.35f) "Agua líquida" else if (config.ior < 1.55f) "Vidrio Corona (iOS)" else "Cristal de Sílice",
                        value = (config.ior - 1.0f) / 0.8f,
                        onValueChange = { norm ->
                            onConfigChange(config.copy(ior = 1.0f + norm * 0.8f))
                        }
                    )

                    // Frosted Blur
                    LabSliderRow(
                        title = "Desenfoque Glaseado",
                        valueText = "${(config.blurStrength * 100).toInt()}%",
                        subtext = "Translucidez y difusión del material",
                        value = config.blurStrength,
                        onValueChange = { onConfigChange(config.copy(blurStrength = it)) }
                    )

                    // Chromatic Aberration
                    LabSliderRow(
                        title = "Dispersión Cromática (Prisma)",
                        valueText = "${(config.chromaticAberration * 100).toInt()}%",
                        subtext = "Desfase espectral rojo/cian en bordes",
                        value = config.chromaticAberration,
                        onValueChange = { onConfigChange(config.copy(chromaticAberration = it)) }
                    )

                    // Specular Intensity
                    LabSliderRow(
                        title = "Intensidad Especular & Cáusticas",
                        valueText = "${(config.specularIntensity * 100).toInt()}%",
                        subtext = "Brillo de reflexión y puntos de luz",
                        value = config.specularIntensity,
                        onValueChange = { onConfigChange(config.copy(specularIntensity = it)) }
                    )

                    // Shadow & Desombreado
                    LabSliderRow(
                        title = "Sombra y Relieve (Desombreado)",
                        valueText = if (config.shadowIntensity <= 0.1f) "Desombreado Puro" else "${(config.shadowIntensity * 100).toInt()}%",
                        subtext = if (config.shadowIntensity <= 0.3f) "Cristal desombreado, ligero y luminoso" else "Sombra de profundidad",
                        value = config.shadowIntensity,
                        onValueChange = { onConfigChange(config.copy(shadowIntensity = it)) }
                    )
                }
            }
        }

        // Fluid Physics Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassEffect(
                        shape = RoundedCornerShape(26.dp),
                        tint = config.tintStyle,
                        elevation = 8.dp
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Física de Fluidos & Tensión Superficial",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Viscosity
                    LabSliderRow(
                        title = "Viscosidad de Fusión",
                        valueText = "${(config.viscosity * 100).toInt()}%",
                        subtext = "Resistencia y elasticidad al coalescer",
                        value = config.viscosity,
                        onValueChange = { onConfigChange(config.copy(viscosity = it)) }
                    )

                    // Toggle Metaball Bridges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Puentes Metaball",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Cuello líquido al aproximarse gotas",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = config.showMetaballBridges,
                            onCheckedChange = { onConfigChange(config.copy(showMetaballBridges = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF38BDF8)
                            )
                        )
                    }

                    // Toggle Haptics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Respuesta Háptica",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Vibración al colisionar y fusionar",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = config.hapticsEnabled,
                            onCheckedChange = { onConfigChange(config.copy(hapticsEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF34D399)
                            )
                        )
                    }
                }
            }
        }

        // Tint Style Selector
        item {
            Text(
                text = "ESTILO Y TINTE DE CRISTAL",
                color = Color(0xFF38BDF8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassTintStyle.values().take(3).forEach { tint ->
                    TintCard(
                        tint = tint,
                        isSelected = config.tintStyle == tint,
                        modifier = Modifier.weight(1f),
                        onClick = { onConfigChange(config.copy(tintStyle = tint)) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassTintStyle.values().drop(3).forEach { tint ->
                    TintCard(
                        tint = tint,
                        isSelected = config.tintStyle == tint,
                        modifier = Modifier.weight(1f),
                        onClick = { onConfigChange(config.copy(tintStyle = tint)) }
                    )
                }
            }
        }

        // Background Type Selector
        item {
            Text(
                text = "FONDO Y ENTORNO ÓPTICO",
                color = Color(0xFF38BDF8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassBackgroundType.values().forEach { bg ->
                    BackgroundOptionRow(
                        bg = bg,
                        isSelected = config.backgroundType == bg,
                        onClick = { onConfigChange(config.copy(backgroundType = bg)) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun LabSliderRow(
    title: String,
    valueText: String,
    subtext: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtext,
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 11.sp
                )
            }
            Text(
                text = valueText,
                color = Color(0xFF38BDF8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF38BDF8),
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun PresetChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.12f)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color.White.copy(alpha = 0.6f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = name,
            color = if (isSelected) Color(0xFF0F172A) else Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun TintCard(
    tint: GlassTintStyle,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(tint.fillColor.copy(alpha = 0.7f), tint.causticsTint.copy(alpha = 0.6f))
                )
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.White else tint.borderColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tint.displayName,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun BackgroundOptionRow(
    bg: GlassBackgroundType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF38BDF8) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = bg.displayName,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8))
                )
            }
        }
    }
}
