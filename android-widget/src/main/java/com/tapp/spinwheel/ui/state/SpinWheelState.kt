package com.tapp.spinwheel.ui.state

/**
 * High-level rendering state for the spin wheel view.
 *
 * The view uses this to decide whether to draw the skeleton, the fully
 * interactive wheel, or an inline error with retry affordance.
 */
internal sealed interface SpinWheelState {
    data object Loading : SpinWheelState
    data object Ready : SpinWheelState
    data object Error : SpinWheelState
}

