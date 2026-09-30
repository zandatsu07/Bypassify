# ⚡ Bypass Charging

A lightweight, root-only Android app that makes **bypass charging possible outside of gaming mode** on Samsung devices that support bypass charging, when used with a **PPS-supported charger**.

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Root](https://img.shields.io/badge/requires-root-red)
![Device](https://img.shields.io/badge/device-Galaxy%20S23%2B-1428A0)
![UI](https://img.shields.io/badge/UI-Material%203-6750A4)

## Overview

By default, Samsung only enables bypass charging while a game is running. This app removes that limitation: on Samsung devices that support bypass charging, it lets you turn bypass charging on and off **anytime, in any app or while idle, outside of gaming mode**, as long as you use a **PPS-supported charger**.

With bypass charging active, power is supplied straight to the phone instead of going through the battery, which reduces heat and battery wear during long charging sessions. Developed and tested on the Galaxy S23+.

## Features

- 🔘 One-tap **enable / disable** of bypass charging
- 📊 Live **status display** (ON / OFF)
- 🧩 **Quick Settings tile** for toggling from the notification shade
- 🎨 Clean **Material 3** interface with dynamic (Material You) colors and light/dark mode

## Requirements

| Requirement | Details |
|---|---|
| **Root access** | **Mandatory.** The app will not work on a non-rooted device. |
| **Device** | Any Samsung device that can bypass charge while gaming (confirmed working; developed on the Galaxy S23+) |
| **Charger** | A charger that supports **USB PD PPS** (Programmable Power Supply) |
| **Android** | 11 (API 30) or newer |

> [!IMPORTANT]
> This app **only works on rooted devices**. Root permission must be granted to the app (via Magisk, KernelSU, etc.) or it cannot change the setting.

> [!NOTE]
> Bypass charging depends on your charger. Without a **PPS-capable** charger and cable, enabling the setting may have no visible effect.

## How it works

The app changes a single system setting using root:

```bash
settings put system pass_through 1   # enable bypass charging
settings put system pass_through 0   # disable bypass charging
settings get system pass_through     # check current status
```

## No root? Use ADB instead

You can also control bypass charging **without root and without this app**, using ADB from a computer with USB debugging enabled:

```bash
# Turn on bypass charging
adb shell settings put system pass_through 1

# Turn off bypass charging
adb shell settings put system pass_through 0

# Check bypass charging status (1 = on, 0 = off)
adb shell settings get system pass_through
```

> [!TIP]
> Enable **Developer options > USB debugging** on your phone, connect it to your computer, and approve the authorization prompt. You'll need [Android platform-tools](https://developer.android.com/tools/releases/platform-tools) installed. The same charger requirement (PPS) applies.

## Installation

1. Download the latest APK from the [**Releases**](../../releases) page.
2. On your phone, allow **Install unknown apps** for your browser or file manager.
3. Install the APK and open the app.
4. When prompted by your root manager, **grant root access** (choose *Remember* so the Quick Settings tile works without prompting).
5. *(Optional)* Add the tile: swipe down twice > tap the edit (pencil) icon > drag **Bypass charging** into your panel.

## Usage

1. Plug in a **PPS-compatible** charger.
2. Open the app, or use the Quick Settings tile.
3. Turn bypass charging **ON**. The status card should show **Bypass ON**.
4. Turn it **OFF** to return to normal charging.

## Building from source

Every tag starting with `v` (for example `v1.0`) triggers a GitHub Actions build that publishes the APK to Releases. To build locally, open the project in Android Studio and run **Build > Build APK**.

## Disclaimer

This software modifies system settings using root access and is provided **as is, without warranty**. You use it at your own risk. The author is not responsible for any damage, data loss, or warranty issues resulting from its use. Rooting may void your device warranty and trip Samsung Knox.

## Contributing

Issues and pull requests are welcome, especially reports of other devices where this works.
