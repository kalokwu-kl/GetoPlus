<div align = "center">

<img width="100" src="app/src/main/ic_launcher-playstore.png" alt="Geto+" align="center">

# Geto+

Toggle system settings from your Quick Settings tile — globally, with one tap.

![GitHub Release](https://img.shields.io/github/v/release/kalokwu-kl/GetoPlus?style=for-the-badge)
![GitHub License](https://img.shields.io/github/license/kalokwu-kl/GetoPlus?style=for-the-badge)

</div>

About The Project
==================

This is a **fork** of [Geto](https://github.com/JackEblan/Geto) by JackEblan, adapted for a simplified
workflow. Instead of configuring settings per app, Geto+ provides a **Quick Settings tile** that
toggles a global configuration with a single tap. Check/uncheck which settings you want to include,
tap the tile, and done.

Requires `android.permission.WRITE_SECURE_SETTINGS` to modify system settings values.

How It Works
=============

1. **Add your settings** via the app — label, key, values for on/off
2. **Check which ones** you want to include in the toggle
3. **Pull down Quick Settings** and tap the Geto+ tile to apply — tap again to revert
4. An ongoing notification shows the current status

Getting Started
===============

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable)
- Java 17 or 21 (bundled with Android Studio, or via [SDKMAN](https://sdkman.io))
- Android SDK (installed via Android Studio's SDK Manager; provides `adb` from platform-tools)

### Build from source

```bash
# Clone the repository
git clone https://github.com/kalokwu-kl/GetoPlus
cd GetoPlus

# Build a debug APK
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

### Build a release APK

The easiest way is Android Studio's **Build > Generate Signed App Bundle / APK** wizard
(**APK**): create or reuse a keystore, and the signed APK lands at
`app/build/outputs/apk/release/app-release.apk`.

Alternatively, `./gradlew assembleRelease` produces an **unsigned** APK
(`app-release-unsigned.apk`) that cannot be installed unless you add your own
`signingConfigs` in `app/build.gradle.kts`.

Release builds use R8 shrinking and resource minification for a smaller APK.

### Install on device

```bash
# With a connected device or emulator
./gradlew installDebug
```

Or open the project in Android Studio and press **Run** (▶).

### Grant the required permission

The app needs `WRITE_SECURE_SETTINGS`. Enable **Developer options > USB debugging**,
connect the device, then run:

```bash
adb shell pm grant com.android.geto.debug android.permission.WRITE_SECURE_SETTINGS  # debug build
adb shell pm grant com.android.geto android.permission.WRITE_SECURE_SETTINGS         # release build
```

License
=======

**Geto+** is licensed under the GNU General Public License v3.0. See the [license](LICENSE) for more
information.
