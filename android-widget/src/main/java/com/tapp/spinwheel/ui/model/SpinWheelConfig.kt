package com.tapp.spinwheel.ui.model

import kotlinx.serialization.Serializable

@Serializable
data class WidgetConfigEnvelope(
    val data: List<WidgetConfig>,
    val meta: Meta? = null,
)

@Serializable
data class WidgetConfig(
    val id: String,
    val name: String,
    val type: String,
    val network: NetworkConfig,
    val wheel: WheelConfig,
)

@Serializable
data class NetworkConfig(
    val attributes: NetworkAttributes? = null,
    val assets: NetworkAssets,
)

@Serializable
data class NetworkAttributes(
    val refreshInterval: Long? = null,
    val networkTimeout: Long? = null,
    val retryAttempts: Int? = null,
    val cacheExpiration: Long? = null,
    val debugMode: Boolean? = null,
)

@Serializable
data class NetworkAssets(
    val host: String,
)

@Serializable
data class WheelConfig(
    val rotation: RotationConfig,
    val assets: WheelAssets,
)

@Serializable
data class RotationConfig(
    val duration: Long,
    val minimumSpins: Int,
    val maximumSpins: Int,
    val spinEasing: String? = null,
)

@Serializable
data class WheelAssets(
    val bg: String,
    val wheelFrame: String,
    val wheelSpin: String,
    val wheel: String,
)

@Serializable
data class Meta(
    val version: Int? = null,
    val copyright: String? = null,
)

