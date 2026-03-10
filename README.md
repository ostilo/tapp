# Kotlin Spin Wheel + React Native Wrapper

This repository contains an interview implementation of a **config‑driven spin wheel UI
component** written in Kotlin and exposed to React Native, plus a small demo app,
homescreen widget, and packaging notes.

Everything is kept intentionally clear and simple so a junior developer (or reviewer)
can follow along without getting lost in build tooling.

---

## 1. Problem statement

Build a reusable **spin wheel UI** that:

- Is implemented natively in Kotlin as a custom Android `View`.
- Loads its look & behaviour from a remote JSON config (config‑driven).
- Exposes the view to **React Native** as a typed component.
- Includes a minimal RN demo app and an Android homescreen widget entry point.
- Shows you understand **architecture**, **offline behaviour**, and **packaging**.

There is also:

- Screenshots in `screenshots/frame1.png` and `screenshots/frame2.png`.

 The app will load the wheel config (requires network on first run; then works offline from cache). To build from source instead, see [§4](#4-how-to-build-and-run) and [`BUILD_AND_PACK.md`](./BUILD_AND_PACK.md).

---

## 2. High‑level architecture

### android-widget (Kotlin library)

- `SpinWheelView` (`com.tapp.spinwheel.SpinWheelView`)
  - Custom `View` that:
    - Fetches JSON config from a `configUrl`.
    - Downloads and caches wheel assets to disk.
    - Renders loading / ready / error states.
    - Handles tap‑to‑spin on the center button only.
- Data and models are split cleanly into:
  - `data/remote` – thin OkHttp client.
  - `data/local` – shared prefs + disk cache.
  - `data/SpinWheelRepository` – orchestrates remote + local and exposes:
    - `loadConfig(force: Boolean)` – always returns the best config (network or cache).
    - `fetchImageToFile(url: String)` – image download + reuse.
  - `data/model` – `WidgetConfig`, `NetworkConfig`, `WheelConfig`, etc.
  - `ui/state` – `SpinWheelState` (`Loading`, `Ready`, `Error`).
  - `ui/model` – `SpinWheelBitmaps`.
  - `core` – colors + logger.

### react-native-spin-wheel (RN native module)

- JS/TS:
  - `src/specs/SpinWheelNativeComponent.ts` – Fabric spec with `codegenNativeComponent`.
  - `src/index.ts` – exports `SpinWheelView` for JS.
- Android:
  - `SpinWheelViewManager` bridges the `configUrl` prop to the Kotlin `SpinWheelView`.
  - Uses the React Native Gradle plugin + codegen.
- In this monorepo it depends on the sibling `:android-widget` Gradle project for the
  actual UI code.

### demo-app (React Native demo + homescreen widget)

- RN app that imports `react-native-spin-wheel` and renders:

  ```tsx
  <SpinWheelView configUrl="https://raw.githubusercontent.com/ostilo/tapp/refs/heads/main/tapp_widget_config.json" />
  ```

- Minimal Android homescreen widget:
  - Simple gradient card with icon + text.
  - Tapping it launches the React Native `MainActivity`.

---

The widget provider implementation lives in `android-widget` as
`com.tapp.spinwheel.SpinWheelAppWidgetProvider`; the demo app just declares
a manifest `<receiver>` pointing at that class.

-```<receiver
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

## 3. Repo layout

- `android-widget/`
  - Kotlin spin wheel library (pure Android).
- `react-native-spin-wheel/`
  - React Native wrapper module.
- `demo-app/`
  - RN demo using the wrapper and the Kotlin view.
- `screenshots/`
  - `frame1.png`, `frame2.png` – for README / documentation.

Each folder also has its own `README.md` with more detail.

---

## 4. How to build and run

### 4.1 Build the Kotlin library (android-widget)

From the repo root:

```bash
cd android-widget
./gradlew assembleDebug
```

Android Studio can also open this folder directly and run/inspect the module.

### 4.2 Run the React Native demo (demo-app)

From the repo root:

```bash
cd demo-app
npm install

# terminal 1
npm start

# terminal 2
npm run android
```

The app uses the **New Architecture** (Fabric) and autolinks the native module.

### 4.3 Generate the RN wrapper `.tgz` (react-native-spin-wheel)

From the repo root:

```bash
cd react-native-spin-wheel
npm run build
npm pack   # produces react-native-spin-wheel-1.0.0.tgz
```

You can install that tarball into another RN app with:

```bash
npm install /absolute/path/to/react-native-spin-wheel-1.0.0.tgz
```

> In this repo the Android side still expects the sibling `:android-widget` module via
> Gradle. Making the package fully standalone would mean either inlining the widget
> code into this module or publishing `android-widget` as an AAR and depending on it
> by Maven coordinate (both are noted in the module README).

---

## 5. Deliverables checklist

- [x] Kotlin spin wheel view with config‑driven behaviour.
- [x] Clean data / UI architecture (repository, models, state).
- [x] React Native Fabric wrapper (`react-native-spin-wheel`).
- [x] Demo RN app showing the wheel and integrating the native module.
- [x] Android homescreen widget that launches the experience.
- [x] Offline‑friendly behaviour (uses cached config + assets when network is off).
- [x] Packaging notes (`.tgz` for RN wrapper) and build instructions.

For step‑by‑step build + packaging instructions, see
[`BUILD_AND_PACK.md`](./BUILD_AND_PACK.md).
