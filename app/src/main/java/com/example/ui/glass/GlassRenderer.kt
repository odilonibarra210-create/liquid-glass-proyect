package com.example.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.GlassConfig
import com.example.model.GlassRipple
import com.example.model.GlassTintStyle
import com.example.model.LiquidDroplet
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GlassRenderer {

    /**
     * Draws an authentic liquid glass droplet with caustic refraction,
     * chromatic fringe, and dynamic specular reflections.
     */
    fun drawDroplet(
        drawScope: DrawScope,
        droplet: LiquidDroplet,
        config: GlassConfig,
        tint: GlassTintStyle = config.tintStyle
    ) {
        val center = Offset(droplet.x, droplet.y)
        val r = droplet.radius
        val sx = droplet.squishX
        val sy = droplet.squishY

        val effectiveRx = r * sx
        val effectiveRy = r * sy

        // 1. Drop shadow / Ground ambient occlusion (Desombreado sutil)
        val shadowOffset = Offset(0f, effectiveRy * 0.18f)
        val shadowAlpha = (0.12f * config.shadowIntensity * config.specularIntensity).coerceIn(0f, 0.4f)
        if (shadowAlpha > 0.005f) {
            drawScope.drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = shadowAlpha),
                        Color.Black.copy(alpha = shadowAlpha * 0.25f),
                        Color.Transparent
                    ),
                    center = center + shadowOffset,
                    radius = effectiveRx * 1.15f
                ),
                topLeft = Offset(center.x - effectiveRx * 1.15f, center.y - effectiveRy * 0.8f + shadowOffset.y),
                size = Size(effectiveRx * 2.3f, effectiveRy * 1.8f)
            )
        }

        // 2. Chromatic Aberration Fringe (Red/Cyan offset)
        if (config.chromaticAberration > 0.05f) {
            val caOffset = 2.5f * config.chromaticAberration
            // Cyan fringe (shifted slightly top-left)
            drawScope.drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x3306B6D4).copy(alpha = 0.35f * config.chromaticAberration)
                    ),
                    center = center - Offset(caOffset, caOffset),
                    radius = effectiveRx * 1.05f
                ),
                topLeft = Offset(center.x - effectiveRx - caOffset, center.y - effectiveRy - caOffset),
                size = Size(effectiveRx * 2f, effectiveRy * 2f)
            )
            // Magenta/Red fringe (shifted slightly bottom-right)
            drawScope.drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x33EC4899).copy(alpha = 0.35f * config.chromaticAberration)
                    ),
                    center = center + Offset(caOffset, caOffset),
                    radius = effectiveRx * 1.05f
                ),
                topLeft = Offset(center.x - effectiveRx + caOffset, center.y - effectiveRy + caOffset),
                size = Size(effectiveRx * 2f, effectiveRy * 2f)
            )
        }

        // 3. Liquid Glass Body (Refractive center transparency + frosted tint rim)
        val bodyBrush = Brush.radialGradient(
            colors = listOf(
                tint.fillColor.copy(alpha = (tint.fillColor.alpha * 0.35f).coerceIn(0f, 1f)),
                tint.fillColor,
                tint.causticsTint.copy(alpha = 0.45f)
            ),
            center = center,
            radius = effectiveRx * 1.1f
        )
        drawScope.drawOval(
            brush = bodyBrush,
            topLeft = Offset(center.x - effectiveRx, center.y - effectiveRy),
            size = Size(effectiveRx * 2f, effectiveRy * 2f)
        )

        // 4. Internal Lower Caustic Glow (Light focused through lens)
        val causticCenter = Offset(center.x + effectiveRx * 0.22f, center.y + effectiveRy * 0.32f)
        val causticRadius = effectiveRx * 0.55f
        drawScope.drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    tint.specularColor.copy(alpha = 0.65f * config.specularIntensity),
                    tint.causticsTint.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = causticCenter,
                radius = causticRadius
            ),
            topLeft = Offset(causticCenter.x - causticRadius, causticCenter.y - causticRadius * 0.7f),
            size = Size(causticRadius * 2f, causticRadius * 1.4f)
        )

        // 5. Specular Rim / Fresnel edge
        drawScope.drawOval(
            brush = Brush.sweepGradient(
                colors = listOf(
                    tint.specularColor.copy(alpha = 0.9f * config.specularIntensity),
                    tint.borderColor.copy(alpha = 0.3f),
                    Color.White.copy(alpha = 0.15f),
                    tint.borderColor.copy(alpha = 0.4f),
                    tint.specularColor.copy(alpha = 0.9f * config.specularIntensity)
                ),
                center = center
            ),
            topLeft = Offset(center.x - effectiveRx, center.y - effectiveRy),
            size = Size(effectiveRx * 2f, effectiveRy * 2f),
            style = Stroke(width = (2.2f * (r / 50f)).coerceIn(1.5f, 4f))
        )

        // 6. Primary Specular Highlight (Reflection of light source at top-left)
        val specRx = effectiveRx * 0.38f
        val specRy = effectiveRy * 0.22f
        val specCenter = Offset(center.x - effectiveRx * 0.32f, center.y - effectiveRy * 0.35f)

        drawScope.drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f * config.specularIntensity),
                    Color.White.copy(alpha = 0.5f * config.specularIntensity),
                    Color.Transparent
                ),
                center = specCenter,
                radius = specRx
            ),
            topLeft = Offset(specCenter.x - specRx, specCenter.y - specRy),
            size = Size(specRx * 2f, specRy * 2f)
        )

        // Secondary soft specular bounce at lower edge
        val bounceRx = effectiveRx * 0.22f
        val bounceRy = effectiveRy * 0.10f
        val bounceCenter = Offset(center.x + effectiveRx * 0.28f, center.y + effectiveRy * 0.42f)
        drawScope.drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.45f * config.specularIntensity),
                    Color.Transparent
                ),
                center = bounceCenter,
                radius = bounceRx
            ),
            topLeft = Offset(bounceCenter.x - bounceRx, bounceCenter.y - bounceRy),
            size = Size(bounceRx * 2f, bounceRy * 2f)
        )
    }

    /**
     * Draws the metaball fluid bridge connecting two adjacent liquid droplets.
     */
    fun drawMetaballBridge(
        drawScope: DrawScope,
        d1: LiquidDroplet,
        d2: LiquidDroplet,
        config: GlassConfig,
        tint: GlassTintStyle = config.tintStyle
    ) {
        val dx = d2.x - d1.x
        val dy = d2.y - d1.y
        val dist = sqrt(dx * dx + dy * dy)
        val sumR = d1.radius + d2.radius

        // Only draw bridge when within surface tension threshold
        if (dist >= sumR * 1.35f || dist <= 1f) return

        val r1 = d1.radius
        val r2 = d2.radius

        val angle = atan2(dy, dx)
        val u1 = acos(((r1 - r2) / dist).coerceIn(-1f, 1f))
        val v = acos(((r1 + r2) / (dist * 1.35f)).coerceIn(-1f, 1f))

        // Waist radius (narrowest part of the liquid neck)
        val factor = 1f - (dist / (sumR * 1.35f)).coerceIn(0f, 1f)
        val waist = (minOf(r1, r2) * 0.7f * factor * config.viscosity).coerceAtLeast(3f)

        val midX = (d1.x + d2.x) * 0.5f
        val midY = (d1.y + d2.y) * 0.5f

        val perpAngle = angle + (Math.PI / 2).toFloat()
        val p1x = midX + cos(perpAngle) * waist
        val p1y = midY + sin(perpAngle) * waist
        val p2x = midX - cos(perpAngle) * waist
        val p2y = midY - sin(perpAngle) * waist

        val path = Path().apply {
            val a1 = angle + (Math.PI / 2).toFloat() * 0.8f
            val a2 = angle - (Math.PI / 2).toFloat() * 0.8f

            moveTo(d1.x + cos(a1) * r1, d1.y + sin(a1) * r1)
            quadraticTo(p1x, p1y, d2.x + cos(a1) * r2, d2.y + sin(a1) * r2)
            lineTo(d2.x + cos(a2) * r2, d2.y + sin(a2) * r2)
            quadraticTo(p2x, p2y, d1.x + cos(a2) * r1, d1.y + sin(a2) * r1)
            close()
        }

        // Fill bridge with translucent liquid
        drawScope.drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(tint.fillColor, tint.causticsTint.copy(alpha = 0.5f), tint.fillColor),
                start = Offset(d1.x, d1.y),
                end = Offset(d2.x, d2.y)
            ),
            style = Fill
        )

        // Edge highlights along the neck
        drawScope.drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(
                    tint.specularColor.copy(alpha = 0.7f * config.specularIntensity),
                    tint.borderColor.copy(alpha = 0.3f),
                    tint.specularColor.copy(alpha = 0.7f * config.specularIntensity)
                ),
                start = Offset(d1.x, d1.y),
                end = Offset(d2.x, d2.y)
            ),
            style = Stroke(width = 1.8f)
        )
    }

    /**
     * Draws expanding refractive ripples on touch or droplet collisions.
     */
    fun drawRipple(
        drawScope: DrawScope,
        ripple: GlassRipple,
        config: GlassConfig
    ) {
        val center = Offset(ripple.x, ripple.y)
        val r = ripple.currentRadius
        val a = ripple.alpha

        // Inner wave
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    ripple.color.copy(alpha = a * 0.15f),
                    Color.Transparent
                ),
                center = center,
                radius = r
            ),
            center = center,
            radius = r,
            style = Stroke(width = 14f * a)
        )

        // Crisp crest with chromatic shift
        val ca = 2f * config.chromaticAberration
        // Cyan inner crest
        drawScope.drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = a * 0.45f * config.chromaticAberration),
            center = center - Offset(ca, ca),
            radius = (r - 2f).coerceAtLeast(1f),
            style = Stroke(width = 2f * a)
        )
        // Main white crest
        drawScope.drawCircle(
            color = Color.White.copy(alpha = a * 0.85f * config.specularIntensity),
            center = center,
            radius = r,
            style = Stroke(width = 2.5f * a)
        )
        // Magenta outer crest
        drawScope.drawCircle(
            color = Color(0xFFF472B6).copy(alpha = a * 0.45f * config.chromaticAberration),
            center = center + Offset(ca, ca),
            radius = r + 2f,
            style = Stroke(width = 2f * a)
        )
    }

    /**
     * Draws a magnifying refraction lens that distorts and zooms what's underneath.
     */
    fun drawRefractionLens(
        drawScope: DrawScope,
        lensCenter: Offset,
        lensRadius: Float,
        config: GlassConfig
    ) {
        // Outer bezel ring
        drawScope.drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color(0xFF64748B).copy(alpha = 0.5f),
                    Color.White.copy(alpha = 0.2f),
                    Color.White.copy(alpha = 0.8f)
                ),
                center = lensCenter
            ),
            center = lensCenter,
            radius = lensRadius,
            style = Stroke(width = 4.dp.value)
        )

        // Glass curvature lens reflection
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.05f),
                    Color(0x3338BDF8),
                    Color.White.copy(alpha = 0.25f)
                ),
                center = lensCenter,
                radius = lensRadius
            ),
            center = lensCenter,
            radius = lensRadius
        )

        // Arc specular reflection at top of lens
        val specOffset = Offset(lensCenter.x - lensRadius * 0.28f, lensCenter.y - lensRadius * 0.35f)
        drawScope.drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.85f),
                    Color.Transparent
                ),
                center = specOffset,
                radius = lensRadius * 0.4f
            ),
            topLeft = Offset(specOffset.x - lensRadius * 0.4f, specOffset.y - lensRadius * 0.2f),
            size = Size(lensRadius * 0.8f, lensRadius * 0.4f)
        )
    }
}

/**
 * Reusable Compose modifier for authentic Apple iOS Liquid Glass cards and containers.
 */
fun Modifier.liquidGlassEffect(
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    tint: GlassTintStyle = GlassTintStyle.IOS_CLEAR,
    blurAlpha: Float = 0.65f,
    elevation: Dp = 4.dp,
    hasSheen: Boolean = true
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = Color.Black.copy(alpha = 0.06f),
        spotColor = Color.Black.copy(alpha = 0.08f)
    )
    .clip(shape)
    .background(
        brush = Brush.linearGradient(
            colors = listOf(
                tint.fillColor.copy(alpha = (tint.fillColor.alpha * (0.8f + blurAlpha * 0.4f)).coerceIn(0.15f, 0.95f)),
                tint.fillColor.copy(alpha = (tint.fillColor.alpha * (0.5f + blurAlpha * 0.3f)).coerceIn(0.1f, 0.85f))
            ),
            start = Offset(0f, 0f),
            end = Offset(400f, 400f)
        )
    )
    .border(
        width = 1.2.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                tint.specularColor.copy(alpha = 0.85f),
                tint.borderColor.copy(alpha = 0.45f),
                Color.White.copy(alpha = 0.1f),
                tint.borderColor.copy(alpha = 0.35f),
                tint.specularColor.copy(alpha = 0.6f)
            ),
            start = Offset(0f, 0f),
            end = Offset(600f, 600f)
        ),
        shape = shape
    )
    .drawBehind {
        if (hasSheen) {
            // Diagonal top specular gleam
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.25f, 0f),
                    radius = size.width * 0.5f
                ),
                topLeft = Offset(0f, -size.height * 0.2f),
                size = Size(size.width * 0.8f, size.height * 0.6f)
            )
        }
    }
