package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.DynamicIslandMode
import com.example.model.GlassAppTab
import com.example.model.GlassConfig
import com.example.model.GlassTintStyle
import com.example.physics.LiquidPhysicsEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GlassViewModel : ViewModel() {

    val physicsEngine = LiquidPhysicsEngine()

    private val _config = MutableStateFlow(GlassConfig())
    val config: StateFlow<GlassConfig> = _config.asStateFlow()

    private val _activeTab = MutableStateFlow(GlassAppTab.PLAYGROUND)
    val activeTab: StateFlow<GlassAppTab> = _activeTab.asStateFlow()

    private val _islandMode = MutableStateFlow(DynamicIslandMode.COMPACT)
    val islandMode: StateFlow<DynamicIslandMode> = _islandMode.asStateFlow()

    private val _isMicroscopicZoomActive = MutableStateFlow(false)
    val isMicroscopicZoomActive: StateFlow<Boolean> = _isMicroscopicZoomActive.asStateFlow()

    fun toggleMicroscopicZoom() {
        _isMicroscopicZoomActive.value = !_isMicroscopicZoomActive.value
    }

    fun setMicroscopicZoom(active: Boolean) {
        _isMicroscopicZoomActive.value = active
    }

    fun updateConfig(newConfig: GlassConfig) {
        _config.value = newConfig
    }

    fun setTab(tab: GlassAppTab) {
        _activeTab.value = tab
    }

    fun setIslandMode(mode: DynamicIslandMode) {
        _islandMode.value = mode
    }

    fun setTint(tint: GlassTintStyle) {
        _config.update { it.copy(tintStyle = tint) }
    }
}
