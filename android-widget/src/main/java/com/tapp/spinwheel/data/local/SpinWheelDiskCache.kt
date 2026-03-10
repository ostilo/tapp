package com.tapp.spinwheel.data.local

import android.content.Context
import java.io.File
import java.security.MessageDigest

/**
 * Disk-based cache for config JSON and downloaded image assets.
 *
 * Paths are treated as an implementation detail so callers only deal with Files.
 */
internal class SpinWheelDiskCache(context: Context) {
    private val cacheDir: File = File(context.cacheDir, "spinwheel").apply { mkdirs() }

    fun getConfigFile(): File = File(cacheDir, CONFIG_CACHE_FILE)

    fun getImageFileForUrl(url: String): File = File(cacheDir, "${url.sha256()}.img")

    private fun String.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val CONFIG_CACHE_FILE = "config.json"
    }
}

