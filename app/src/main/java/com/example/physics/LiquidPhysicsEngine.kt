package com.example.physics

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.model.GlassConfig
import com.example.model.GlassRipple
import com.example.model.LiquidDroplet
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LiquidPhysicsEngine {
    private var nextDropletId = 1L
    private var nextRippleId = 1L

    val droplets = mutableListOf<LiquidDroplet>()
    val ripples = mutableListOf<GlassRipple>()

    var onFusionOccurred: (() -> Unit)? = null
    private var fluidTime = 0f

    fun initializeDefaultDroplets(width: Float, height: Float) {
        droplets.clear()
        if (width <= 0 || height <= 0) return

        val centerX = width / 2f
        val centerY = height / 2f

        droplets.add(LiquidDroplet(id = nextDropletId++, x = centerX, y = centerY, radius = 70f, hue = 210f))
        droplets.add(LiquidDroplet(id = nextDropletId++, x = centerX - 120f, y = centerY - 100f, radius = 50f, hue = 190f))
        droplets.add(LiquidDroplet(id = nextDropletId++, x = centerX + 110f, y = centerY - 70f, radius = 45f, hue = 270f))
        droplets.add(LiquidDroplet(id = nextDropletId++, x = centerX - 70f, y = centerY + 130f, radius = 40f, hue = 320f))
        droplets.add(LiquidDroplet(id = nextDropletId++, x = centerX + 90f, y = centerY + 140f, radius = 55f, hue = 160f))
    }

    fun addDroplet(x: Float, y: Float, radius: Float = 45f, hue: Float = 200f): LiquidDroplet {
        val droplet = LiquidDroplet(
            id = nextDropletId++,
            x = x,
            y = y,
            radius = radius,
            hue = hue,
            squishX = 1.3f,
            squishY = 0.7f
        )
        droplets.add(droplet)
        addRipple(x, y, 160f, Color.White.copy(alpha = 0.5f))
        return droplet
    }

    fun addRipple(x: Float, y: Float, maxRadius: Float = 280f, color: Color = Color.White) {
        if (ripples.size > 15) {
            ripples.removeAt(0)
        }
        ripples.add(
            GlassRipple(
                id = nextRippleId++,
                x = x,
                y = y,
                currentRadius = 15f,
                maxRadius = maxRadius,
                alpha = 0.9f,
                color = color
            )
        )
    }

    fun splitDroplet(dropletId: Long): Boolean {
        val index = droplets.indexOfFirst { it.id == dropletId }
        if (index == -1) return false
        val original = droplets[index]
        if (original.radius < 32f) return false // too small to split

        droplets.removeAt(index)
        val newRadius = original.radius * 0.68f
        val offsetDist = original.radius * 0.75f

        val d1 = LiquidDroplet(
            id = nextDropletId++,
            x = original.x - offsetDist,
            y = original.y,
            vx = -6f,
            vy = (Math.random() * 4 - 2).toFloat(),
            radius = newRadius,
            hue = (original.hue + 25f) % 360f,
            squishX = 1.3f,
            squishY = 0.7f
        )
        val d2 = LiquidDroplet(
            id = nextDropletId++,
            x = original.x + offsetDist,
            y = original.y,
            vx = 6f,
            vy = (Math.random() * 4 - 2).toFloat(),
            radius = newRadius,
            hue = (original.hue + 60f) % 360f,
            squishX = 0.7f,
            squishY = 1.3f
        )
        droplets.add(d1)
        droplets.add(d2)
        addRipple(original.x, original.y, 220f, Color.White.copy(alpha = 0.7f))
        return true
    }

    fun update(
        boundsWidth: Float,
        boundsHeight: Float,
        config: GlassConfig,
        dt: Float = 1f
    ) {
        if (boundsWidth <= 0 || boundsHeight <= 0) return

        val damping = 0.94f + (1f - config.viscosity) * 0.04f
        val gravityX = if (config.gravityEnabled) config.tiltGravityX * 12f else 0f
        val gravityY = if (config.gravityEnabled) (config.tiltGravityY * 12f + 1.2f) else 0f

        // Update ambient fluid time for organic hydrodynamic breathing
        fluidTime += dt * 0.05f

        // Update individual droplets with hydrodynamic drift and fluid oscillation
        for (i in droplets.indices) {
            val d = droplets[i]
            if (d.isDragging) continue

            // Ambient gentle fluid currents (buoyancy drift)
            val ambientVx = 0.45f * sin(fluidTime * 0.9f + d.id * 1.7f)
            val ambientVy = 0.35f * cos(fluidTime * 0.7f + d.id * 2.1f)

            d.vx += (gravityX * 0.1f) + ambientVx * 0.08f
            d.vy += (gravityY * 0.1f) + ambientVy * 0.08f

            d.vx *= damping
            d.vy *= damping

            d.x += d.vx * dt
            d.y += d.vy * dt

            // Screen boundary collisions with fluid damping & squish
            val pad = d.radius
            if (d.x < pad) {
                d.x = pad
                d.vx = -d.vx * 0.72f
                d.squishX = 0.7f
                d.squishY = 1.3f
                d.bounceVelocity = 0.25f
            } else if (d.x > boundsWidth - pad) {
                d.x = boundsWidth - pad
                d.vx = -d.vx * 0.72f
                d.squishX = 0.7f
                d.squishY = 1.3f
                d.bounceVelocity = 0.25f
            }

            if (d.y < pad) {
                d.y = pad
                d.vy = -d.vy * 0.72f
                d.squishX = 1.3f
                d.squishY = 0.7f
                d.bounceVelocity = 0.25f
            } else if (d.y > boundsHeight - pad) {
                d.y = boundsHeight - pad
                d.vy = -d.vy * 0.72f
                d.squishX = 1.3f
                d.squishY = 0.7f
                d.bounceVelocity = 0.25f
            }

            // Smooth jelly oscillation towards roundness
            val spring = 0.16f
            d.bounceVelocity += (1f - d.squishX) * spring
            d.bounceVelocity *= 0.88f
            d.squishX += d.bounceVelocity
            d.squishY = 2f - d.squishX // volume conservation approximation

            // Clamp squish
            d.squishX = d.squishX.coerceIn(0.55f, 1.55f)
            d.squishY = d.squishY.coerceIn(0.55f, 1.55f)
        }

        // Pairwise interactions: Surface tension attraction, elastic fluid compression & fusion
        var i = 0
        while (i < droplets.size) {
            val d1 = droplets[i]
            var j = i + 1
            while (j < droplets.size) {
                val d2 = droplets[j]
                val dx = d2.x - d1.x
                val dy = d2.y - d1.y
                val dist = sqrt(dx * dx + dy * dy)
                val sumR = d1.radius + d2.radius

                if (dist > 0f) {
                    val nx = dx / dist
                    val ny = dy / dist

                    // 1. Coalescent fusion threshold
                    val fusionThreshold = sumR * 0.72f
                    if (dist < fusionThreshold && !d1.isDragging && !d2.isDragging) {
                        // Merge d2 into d1 with volume conservation
                        val combinedRadius = sqrt(d1.radius * d1.radius + d2.radius * d2.radius)
                            .coerceAtMost(120f)

                        val midX = (d1.x * d1.radius + d2.x * d2.radius) / (d1.radius + d2.radius)
                        val midY = (d1.y * d1.radius + d2.y * d2.radius) / (d1.radius + d2.radius)

                        d1.x = midX
                        d1.y = midY
                        d1.vx = (d1.vx + d2.vx) * 0.55f
                        d1.vy = (d1.vy + d2.vy) * 0.55f
                        d1.radius = combinedRadius
                        d1.squishX = 1.45f
                        d1.squishY = 0.55f
                        d1.bounceVelocity = 0.28f

                        droplets.removeAt(j)
                        addRipple(midX, midY, combinedRadius * 3.8f, Color.White.copy(alpha = 0.85f))
                        onFusionOccurred?.invoke()
                        continue
                    }

                    // 2. Elastic fluid contact bounce (when touching before fusing)
                    if (dist < sumR) {
                        val overlap = sumR - dist
                        val pushForce = overlap * 0.08f
                        if (!d1.isDragging) {
                            d1.vx -= nx * pushForce
                            d1.vy -= ny * pushForce
                        }
                        if (!d2.isDragging) {
                            d2.vx += nx * pushForce
                            d2.vy += ny * pushForce
                        }
                        // Fluid squish reaction
                        d1.squishX = (1f + 0.15f * (overlap / sumR)).coerceAtMost(1.3f)
                        d2.squishX = (1f + 0.15f * (overlap / sumR)).coerceAtMost(1.3f)
                    }

                    // 3. Surface tension pull when approaching
                    val surfaceTensionDist = sumR * 1.35f
                    if (dist < surfaceTensionDist && dist >= sumR) {
                        val overlap = surfaceTensionDist - dist
                        val pullForce = overlap * 0.045f * config.viscosity

                        if (!d1.isDragging) {
                            d1.vx += nx * pullForce
                            d1.vy += ny * pullForce
                        }
                        if (!d2.isDragging) {
                            d2.vx -= nx * pullForce
                            d2.vy -= ny * pullForce
                        }
                    }
                }
                j++
            }
            i++
        }

        // Update ripples
        val rippleIterator = ripples.iterator()
        while (rippleIterator.hasNext()) {
            val ripple = rippleIterator.next()
            ripple.currentRadius += 10f * (1f + config.viscosity * 0.5f)
            val progress = ripple.currentRadius / ripple.maxRadius
            ripple.alpha = (1f - progress).coerceIn(0f, 1f)

            if (ripple.currentRadius >= ripple.maxRadius || ripple.alpha <= 0.01f) {
                rippleIterator.remove()
            }
        }
    }

    fun findDropletAt(x: Float, y: Float): LiquidDroplet? {
        // Return top-most droplet under finger
        for (i in droplets.indices.reversed()) {
            val d = droplets[i]
            val dx = x - d.x
            val dy = y - d.y
            if (dx * dx + dy * dy <= (d.radius * 1.25f) * (d.radius * 1.25f)) {
                return d
            }
        }
        return null
    }
}
