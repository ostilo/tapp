# android-widget

Kotlin library that implements the **native spin wheel UI**. This module is a plain Android
library and does not depend on React Native; it can be embedded into any Android app and is
wrapped by the `react-native-spin-wheel` package for React Native consumers.

## High‑level design

- **Public entry point**
  - `com.tapp.spinwheel.SpinWheelView` – a custom `View` that:
    - Fetches a JSON config from a `configUrl`.
    - Downloads and caches the wheel assets to disk.
    - Renders loading, ready, and error states.
    - Handles tap‑to‑spin on the center button only.

- **Data layer**
  - `data/remote/SpinWheelApi` – thin OkHttp client for string/byte responses.
  - `data/local/SpinWheelPreferences` – wraps `SharedPreferences` to track last config fetch.
  - `data/local/SpinWheelDiskCache` – stores the last config JSON and hashed image files.
  - `data/SpinWheelRepository` – orchestrates remote + local:
    - `loadConfig(force: Boolean)`:
      - Returns cached config immediately when available (offline‑friendly).
      - When `force = true`, tries network first and falls back to cached config on failure.
    - `fetchImageToFile(url: String)`:
      - Downloads images once and reuses them from disk on subsequent runs.

- **UI models / state**
  - `data/model/SpinWheelConfig` – Kotlinx‑serializable DTOs for the config schema
    (`WidgetConfig`, `NetworkConfig`, `WheelConfig`, etc.).
  - `ui/state/SpinWheelState` – `Loading`, `Ready`, `Error` for the view.
  - `ui/model/SpinWheelBitmaps` – grouped bitmaps (bg, wheel, frame, spin).
  - `core/SpinWheelColors` – central palette for skeleton + accent colors.
  - `core/SpinWheelLogger` – debug‑only logging helper.

## Config format

The widget expects a JSON envelope shaped like:

- `WidgetConfigEnvelope` with a `data` array of `WidgetConfig`.
- The first `WidgetConfig` is used.
- `NetworkConfig.assets.host` is used as the base URL for assets.
- `WheelAssets` defines:
  - `bg`, `wheelFrame`, `wheelSpin`, `wheel` – relative asset paths under the host.

The example config used in this repo is hosted in GitHub and consumed by the demo app.

## UX details

- **Loading**
  - While config/assets are loading, the view shows:
    - A pulsing skeleton for the wheel + frame.
    - A small animated dot orbiting the wheel.

- **Ready**
  - Background is center‑cropped and slightly dimmed so the wheel stands out.
  - Wheel always sits inside the frame (frame rect is larger than wheel rect).
  - The spin button is centered; only tapping inside this region triggers a spin.
  - Spin animation uses `ValueAnimator` + `DecelerateInterpolator` for smooth motion.

- **Error**
  - When config or assets cannot be loaded, an inline overlay is drawn:
    - Title: “Tap to retry”.
    - Optional subtitle with the underlying error message (in debug builds).
  - Tapping the button in error state calls `retryLoad(force = true)` and re‑attempts fetch.

## Offline behaviour

Once a config has been successfully loaded at least once:

- The last JSON is persisted to disk.
- Subsequent `loadConfig(force = false)` calls return the cached config immediately,
  even if the device is offline.
- Asset files are also cached on disk and reused when the network is not available.

This means the spin wheel continues to render correctly after a configuration has been
fetched at least once, which is important for a homescreen‑like experience.

## Homescreen widget

This module also ships a small homescreen AppWidget implementation that can be reused
by any host app:

- Provider: `com.tapp.spinwheel.SpinWheelAppWidgetProvider`
- Layout: `res/layout/widget_spin_wheel.xml`
- Provider config: `res/xml/spin_wheel_widget_info.xml`
- Supporting resources: `values/strings.xml`, `values/dimens.xml`, `values/colors.xml`,
  and `drawable/widget_spin_wheel_bg.xml`, `drawable/widget_spin_button_bg.xml`

To expose the widget from an application, add a `<receiver>` entry to the app’s
`AndroidManifest.xml` that points at the provider class and config XML, for example:

```xml
<receiver
    android:name="com.tapp.spinwheel.SpinWheelAppWidgetProvider"
    android:exported="true"
    android:label="@string/widget_spin_title">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/spin_wheel_widget_info" />
</receiver>
```

## Building

From the `android-widget` folder:

```bash
./gradlew assembleDebug
```

This produces an AAR under `build/outputs/aar/` (e.g. `android-widget-debug.aar`) that
can be embedded into Android apps directly. In this monorepo, the `demo-app` includes
`android-widget` as a Gradle subproject and compiles it as part of the Android build.

