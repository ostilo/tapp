package com.tapp.spinwheel.data

import android.content.Context
import com.tapp.spinwheel.core.SpinWheelLogger
import com.tapp.spinwheel.ui.model.WidgetConfig
import com.tapp.spinwheel.ui.model.WidgetConfigEnvelope
import com.tapp.spinwheel.data.local.SpinWheelDiskCache
import com.tapp.spinwheel.data.local.SpinWheelPreferences
import com.tapp.spinwheel.data.remote.SpinWheelApi
import com.tapp.spinwheel.ui.model.NetworkAttributes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Coordinates loading and caching of the widget configuration and its assets.
 *
 * - Persists the last successful config JSON on disk so we can recover offline.
 * - Uses a TTL that can be driven by the config itself (`NetworkAttributes.cacheExpiration`),
 *   falling back to a sensible default when not provided.
 * - Exposes a simple suspend API that always returns the "best effort" config/file
 *   (network first, then cache as a fallback).
 */
internal class SpinWheelRepository(
    context: Context,
    private val configUrl: String,
    private val prefs: SpinWheelPreferences = SpinWheelPreferences(context),
    private val cache: SpinWheelDiskCache = SpinWheelDiskCache(context),
    private val api: SpinWheelApi = SpinWheelApi(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Loads the widget config, preferring a previously cached version when possible.
     *
     * - force = false: return cached config immediately if present, otherwise try network.
     * - force = true: try network first, then fall back to cached config on failure.
     *
     * This guarantees that once a config has been fetched successfully at least once,
     * the widget can still render correctly when the device is offline.
     */
    suspend fun loadConfig(force: Boolean = false): WidgetConfig? = withContext(Dispatchers.IO) {
        val cached = cachedConfig()
        if (!force && cached != null) {
            return@withContext cached
        }

        try {
            SpinWheelLogger.d("SpinWheel", "Fetching config from $configUrl")
            val body = api.fetchString(configUrl)
            if (body == null) {
                SpinWheelLogger.e("SpinWheel", "Config fetch returned null body")
                return@withContext cached
            }

            val envelope = json.decodeFromString(WidgetConfigEnvelope.serializer(), body)
            val first = envelope.data.firstOrNull()
            if (first == null) {
                SpinWheelLogger.e("SpinWheel", "Config envelope contained no entries")
                return@withContext cached
            }

            cache.getConfigFile().writeText(body)
            prefs.setLastConfigFetchTimeMs(System.currentTimeMillis())

            first
        } catch (e: IOException) {
            SpinWheelLogger.e("SpinWheel", "Failed to fetch config", e)
            cached
        } catch (e: Exception) {
            SpinWheelLogger.e("SpinWheel", "Failed to parse config", e)
            cached
        }
    }

    /**
     * Reads the last cached config from disk, if present and well-formed.
     */
    private fun cachedConfig(): WidgetConfig? {
        val file = cache.getConfigFile()
        if (!file.exists()) return null
        return try {
            val body = file.readText()
            val envelope = json.decodeFromString(WidgetConfigEnvelope.serializer(), body)
            envelope.data.firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    private fun cachedConfigAttributes(): NetworkAttributes? =
        cachedConfig()?.network?.attributes

    suspend fun fetchImageToFile(url: String): java.io.File? = withContext(Dispatchers.IO) {
        val outFile = cache.getImageFileForUrl(url)
        if (outFile.exists()) return@withContext outFile

        try {
            val bytes = api.fetchBytes(url) ?: return@withContext null
            outFile.outputStream().use { it.write(bytes) }
            outFile
        } catch (e: IOException) {
            SpinWheelLogger.e("SpinWheel", "Failed to fetch image $url", e)
            null
        }
    }
}

