# BUILD_AND_PACK.md

Short, practical steps for cloning the repo, building the Kotlin library, packing the
React Native wrapper, and running the demo app.
---

## 1. Clone the repo

```bash
git clone <this-repo-url> tapp
cd tapp
```

---

## 2. Build the Kotlin library (`android-widget`)

You can build it either with Android Studio or from the command line.

### Option A – Android Studio

1. Open **Android Studio**.
2. Choose **Open an Existing Project**.
3. Select the `android-widget` folder.
4. Let Gradle sync, then run **Build → Make Project**.

### Option B – Gradle CLI

```bash
cd android-widget
./gradlew assembleDebug
```

This gives you a compiled AAR inside `android-widget/build/outputs/aar/`.  
In this repo we use the module directly via Gradle project path instead of
publishing the AAR, but the compiled artifact is there if needed.

---

## 3. Pack the React Native wrapper (`react-native-spin-wheel`)

From the repo root:

```bash
cd react-native-spin-wheel

# 1) Build the JS/TS output (lib/)
npm install
npm run build

# 2) Create a .tgz tarball
npm pack
```

This will produce something like:

```text
react-native-spin-wheel-1.0.0.tgz
```

You can now install that tarball into another React Native app with:

```bash
cd /path/to/other/RN/app
npm install /absolute/path/to/react-native-spin-wheel-1.0.0.tgz
```

> Important: In this interview repo, the Android native code still expects the sibling
> Gradle module `:android-widget`. For a truly standalone package you would either:
> - Inline the `android-widget` code into the RN module’s `android/` folder, or
> - Publish `android-widget` to Maven and depend on it by coordinate.

---

## 4. Run the demo app (`demo-app`)

From the repo root:

```bash
cd demo-app
npm install
```

Start Metro:

```bash
npm start
```

In another terminal, build and run on Android:

```bash
cd demo-app
npm run android
```

You should see the spin wheel load, fetch its config, and allow you to tap the center
button to spin.

---

## 5. Try the packaged wrapper in a fresh RN app (optional)

If you want to demonstrate reuse:

1. Create a fresh RN app:

   ```bash
   npx @react-native-community/cli@latest init SpinWheelConsumerApp --version 0.76.0
   cd SpinWheelConsumerApp
   ```

2. Install the `.tgz`:

   ```bash
   npm install /absolute/path/to/react-native-spin-wheel-1.0.0.tgz
   ```

3. In `App.tsx`:

   ```tsx
   import React from 'react';
   import {SafeAreaView} from 'react-native';
   import {SpinWheelView} from 'react-native-spin-wheel';

   const CONFIG_URL =
     'https://raw.githubusercontent.com/ostilo/tapp/refs/heads/main/tapp_widget_config.json';

   export default function App() {
     return (
       <SafeAreaView style={{flex: 1}}>
         <SpinWheelView style={{flex: 1}} configUrl={CONFIG_URL} />
       </SafeAreaView>
     );
   }
   ```

4. Run:

   ```bash
   npx react-native start
   npx react-native run-android
   ```
---

## 6. Quick links
- Screenshots: [`screenshots/frame1.png`](./screenshots/frame1.png),
  [`screenshots/frame2.png`](./screenshots/frame2.png)

