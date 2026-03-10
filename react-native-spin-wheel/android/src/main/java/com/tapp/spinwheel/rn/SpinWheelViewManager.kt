package com.tapp.spinwheel.rn

import com.tapp.spinwheel.SpinWheelView
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.SpinWheelManagerDelegate
import com.facebook.react.viewmanagers.SpinWheelManagerInterface

@ReactModule(name = SpinWheelViewManager.REACT_CLASS)
class SpinWheelViewManager(
    private val reactContext: ReactApplicationContext,
) : SimpleViewManager<SpinWheelView>(), SpinWheelManagerInterface<SpinWheelView> {

    private val delegate: SpinWheelManagerDelegate<SpinWheelView, SpinWheelViewManager> =
        SpinWheelManagerDelegate(this)

    override fun getDelegate(): ViewManagerDelegate<SpinWheelView> = delegate

    override fun getName(): String = REACT_CLASS

    override fun createViewInstance(reactContext: ThemedReactContext): SpinWheelView =
        SpinWheelView(reactContext)

    @ReactProp(name = "configUrl")
    override fun setConfigUrl(view: SpinWheelView, configUrl: String?) {
        if (configUrl != null) {
            view.setConfigUrl(configUrl)
        }
    }

    companion object {
        const val REACT_CLASS = "RCTSpinWheel"
    }
}
