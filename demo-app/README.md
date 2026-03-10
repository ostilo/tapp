# Spin Wheel Demo App

React Native demo application showcasing:

- The `react-native-spin-wheel` native module.
- The underlying Kotlin `android-widget` spin wheel view.
- A small homescreen AppWidget that launches the experience.

## Prerequisites

- Node.js 18+
- npm or yarn
- Android: Android Studio, SDK 34, and an emulator or device
- iOS (optional): Xcode and CocoaPods

## One-time setup (already done in this repo)

This repository already contains the generated `android/` and `ios/` projects under
`demo-app/`, and `android-widget` is wired in as a Gradle subproject. The steps below
are only needed if you want to recreate the native folders from scratch:

1. Create a temporary React Native app with the same version as this demo.
2. Copy its `android/` and `ios/` folders into `demo-app/`.
3. Add `android-widget` as a Gradle subproject in `demo-app/android/settings.gradle`.
4. Delete the temporary app.

## Run the app

1. **Install dependencies**

   ```bash
   cd demo-app
   npm install
   ```

2. **Start Metro** (in this terminal)

   ```bash
   npm start
   ```

3. **Run on Android** (in a second terminal)

   ```bash
   cd demo-app
   npm run android
   ```

   Use an emulator or a device with USB debugging enabled.

4. **Run on iOS** (optional)

   ```bash
   cd demo-app/ios && pod install && cd ..
   npm run ios
   ```

## Android homescreen widget

The demo exposes a simple AppWidget entry point that launches the full React Native
experience. The reusable widget implementation itself lives in the `android-widget`
library:

- Provider class: `com.tapp.spinwheel.SpinWheelAppWidgetProvider` (in `android-widget`)
- Layout: `android-widget/src/main/res/layout/widget_spin_wheel.xml`
- Provider config: `android-widget/src/main/res/xml/spin_wheel_widget_info.xml`

Once the app is installed:

1. Long‑press on the home screen.
2. Open the **Widgets** picker.
3. Look for **SpinWheelDemoApp** / “Spin the wheel”.
4. Drag the widget to the home screen and tap it to open the app.

## Scripts

- `npm start` – start Metro bundler
- `npm run android` – build and run on Android
- `npm run ios` – build and run on iOS
