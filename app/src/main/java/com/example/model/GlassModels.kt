package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class LiquidDroplet(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var radius: Float,
    var targetRadius: Float = radius,
    val hue: Float = 200f,
    var isDragging: Boolean = false,
    var squishX: Float = 1f,
    var squishY: Float = 1f,
    var bounceVelocity: Float = 0f,
    var creationTime: Long = System.currentTimeMillis()
)

data class GlassRipple(
    val id: Long,
    val x: Float,
    val y: Float,
    var currentRadius: Float = 10f,
    val maxRadius: Float = 320f,
    var alpha: Float = 1f,
    val color: Color = Color.White
)

enum class GlassTintStyle(
    val displayName: String,
    val fillColor: Color,
    val borderColor: Color,
    val specularColor: Color,
    val causticsTint: Color
) {
    IOS_CLEAR(
        displayName = "Cristal Puro",
        fillColor = Color(0x1FFFFFFF),
        borderColor = Color(0x59FFFFFF),
        specularColor = Color(0xF2FFFFFF),
        causticsTint = Color(0x4DF0F9FF)
    ),
    FROSTED_WHITE(
        displayName = "Glaseado Mate",
        fillColor = Color(0x3BFFFFFF),
        borderColor = Color(0x80FFFFFF),
        specularColor = Color(0xFFFFFFFF),
        causticsTint = Color(0x4DFFFFFF)
    ),
    OBSIDIAN(
        displayName = "Obsidiana Espacial",
        fillColor = Color(0x4D0F172A),
        borderColor = Color(0x3B94A3B8),
        specularColor = Color(0xD9E2E8F0),
        causticsTint = Color(0x3838BDF8)
    ),
    AURORA(
        displayName = "Aurora Neón",
        fillColor = Color(0x337C3AED),
        borderColor = Color(0x99C084FC),
        specularColor = Color(0xE6F472B6),
        causticsTint = Color(0x66A855F7)
    ),
    ROSE_QUARTZ(
        displayName = "Cuarzo Rosa",
        fillColor = Color(0x33E11D48),
        borderColor = Color(0x99FB7185),
        specularColor = Color(0xE6FDA4AF),
        causticsTint = Color(0x66F43F5E)
    ),
    EMERALD(
        displayName = "Esmeralda Fluida",
        fillColor = Color(0x33059669),
        borderColor = Color(0x9934D399),
        specularColor = Color(0xE6A7F3D0),
        causticsTint = Color(0x6610B981)
    )
}

enum class GlassBackgroundType(val displayName: String) {
    IOS_WALLPAPER("Wallpaper iOS 18"),
    CUSTOM_USER("Tu Fondo de Pantalla"),
    DYNAMIC_MESH("Malla Aurora"),
    GEOMETRIC_GRID("Cuadrícula Óptica"),
    TYPOGRAPHY_WARP("Texto de Refracción")
}

data class GlassConfig(
    val ior: Float = 1.45f, // Index of Refraction (1.0 to 1.8)
    val blurStrength: Float = 0.65f, // 0.0 to 1.0
    val viscosity: Float = 0.55f, // 0.1 to 1.0
    val chromaticAberration: Float = 0.65f, // 0.0 to 1.0
    val specularIntensity: Float = 0.85f, // 0.0 to 1.0
    val shadowIntensity: Float = 0.25f, // 0.0 (totalmente desombreado) to 1.0 (sombra oscura)
    val tintStyle: GlassTintStyle = GlassTintStyle.IOS_CLEAR,
    val backgroundType: GlassBackgroundType = GlassBackgroundType.IOS_WALLPAPER,
    val gravityEnabled: Boolean = false,
    val tiltGravityX: Float = 0f,
    val tiltGravityY: Float = 0f,
    val hapticsEnabled: Boolean = true,
    val showMetaballBridges: Boolean = true,
    val showLensMagnifier: Boolean = false
)

enum class DynamicIslandMode {
    COMPACT,
    MUSIC_PLAYER,
    INCOMING_CALL,
    TIMER
}

enum class GlassAppTab(val title: String) {
    PLAYGROUND("Líquido"),
    IOS_SHOWCASE("Interfaz iOS"),
    LAB("Laboratorio")
}
