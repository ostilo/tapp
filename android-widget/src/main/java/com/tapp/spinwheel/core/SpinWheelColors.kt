package com.tapp.spinwheel.core

import androidx.core.graphics.toColorInt

/**
 * Palette for UI-only colors used by the spin wheel.
 *
 * Keeping them in one place makes it trivial to tweak branding later.
 */
internal object SpinWheelColors {
    /** Base color for the loading skeleton + background shapes. */
    val LoaderBase = "#2A2A2A".toColorInt()

    /** Accent color used for the animated loading dot. */
    val LoaderAccent = "#7C3AED".toColorInt()
}

