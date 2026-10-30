package com.example

import com.example.model.GlassConfig
import com.example.physics.LiquidPhysicsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPhysicsInitialization() {
        val engine = LiquidPhysicsEngine()
        engine.initializeDefaultDroplets(1080f, 2400f)
        assertEquals(5, engine.droplets.size)
    }

    @Test
    fun testAddDropletAndRipple() {
        val engine = LiquidPhysicsEngine()
        val droplet = engine.addDroplet(100f, 200f, 40f)
        assertNotNull(droplet)
        assertEquals(1, engine.droplets.size)
        assertEquals(1, engine.ripples.size)
    }

    @Test
    fun testSplitDroplet() {
        val engine = LiquidPhysicsEngine()
        val droplet = engine.addDroplet(200f, 300f, 60f)
        val splitSuccess = engine.splitDroplet(droplet.id)
        assertTrue(splitSuccess)
        assertEquals(2, engine.droplets.size)
    }

    @Test
    fun testPhysicsUpdateStability() {
        val engine = LiquidPhysicsEngine()
        engine.initializeDefaultDroplets(1080f, 2400f)
        val config = GlassConfig()

        for (i in 0 until 60) {
            engine.update(1080f, 2400f, config, 1f)
        }

        assertTrue(engine.droplets.isNotEmpty())
        for (d in engine.droplets) {
            assertTrue(!d.x.isNaN())
            assertTrue(!d.y.isNaN())
            assertTrue(d.x in 0f..1080f)
            assertTrue(d.y in 0f..2400f)
        }
    }
}
