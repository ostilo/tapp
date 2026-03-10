package com.tapp.spinwheel.ui.model

import android.graphics.Bitmap

/**
 * Grouped bitmaps required to render the wheel.
 *
 * This keeps the loading pipeline strongly typed instead of passing around
 * loosely-ordered tuples.
 */
internal data class SpinWheelBitmaps(
    val bg: Bitmap?,
    val wheel: Bitmap?,
    val frame: Bitmap?,
    val spin: Bitmap?,
)

