<div align="center">

🇩🇪 <a href="README.md">Deutsch</a> &nbsp;·&nbsp; 🇬🇧 <b>English</b>

<img src="docs/icon.png" width="112" alt="WiFi Disabler">

# WiFi Disabler

**Turns Wi-Fi off automatically as soon as your Android device connects to a network you chose.**

[Download](../../releases/latest) · [Setup](#setup) · [Features](#features)

</div>

<table>
<tr>
<td width="33%"><img src="docs/en/start.png" alt="Home"></td>
<td width="33%"><img src="docs/en/dark.png" alt="Dark theme"></td>
<td width="33%"><img src="docs/en/setup.png" alt="Setup"></td>
</tr>
</table>

## Setup

Download `WiFi-Disabler-vX.Y.Z.apk` from [Releases](../../releases/latest) and install it. Android or Play Protect
will warn that the app was built for an older version of Android. That is intended (see
[Why Android 9?](#why-android-9)), so choose *Install anyway*.

1. **Add a network:** tap *Add “…”* while connected, or type its name (SSID).
2. **Location › Allow all the time:** Android only reveals Wi-Fi names to apps with location access, also in the background.
3. **Keep location turned on:** with location off, Android hides network names from every app.

That's it. The app only shows steps that are still missing. It doesn't have to keep running: Android wakes it as
soon as the device joins a Wi-Fi network.

### If the system stops the app

- **Battery › Unrestricted:** the app shows this step while it is missing.
- **Autostart:** only exists on Xiaomi, Huawei, Honor, Oppo, Realme, OnePlus, Vivo, Meizu, Tecno and Infinix, where
  the app opens the right page directly. Samsung, Pixel, Motorola and others don't have this setting and don't need
  it. On Samsung, just don't list the app as sleeping under *Battery › Background usage limits*.
- **Cleaning and *Force stop*:** memory boosters, cleaner apps and *Force stop* in the app info stop the app
  completely. Android also drops the Wi-Fi watch, and no app may restart itself after that. WiFi Disabler stays off
  until you open it again. Fix: lock the app in the recent apps overview (lock icon or *Keep open*) or exclude it in
  the cleaner. *Clear cache* is harmless, *Clear storage* also deletes your network list.
- **Reliable mode:** a silent background service that Android restarts after a normal stop. Android requires a
  notification for it. *Hide notification* in the app switches it off, the service keeps running.

Device-specific guides are on [dontkillmyapp.com](https://dontkillmyapp.com).

> If you only never want to join a network again, you don't need an app: forget the network or turn off
> *Auto-connect* in the Wi-Fi settings.

## Features

| | |
|---|---|
| **Network list** | Add the current network with one tap or type an SSID |
| **Unobtrusive** | No permanent notification, no questions after setup |
| **Reliable mode** | Optional silent background service for systems that stop apps |
| **Tile** | Pause and resume from Quick Settings |
| **Log** | The last 20 switch-offs, stored locally only |
| **Language** | German or English, following the system |

## Why Android 9?

Since Android 10, only apps that target Android 9 (API 28) may switch Wi-Fi off via `setWifiEnabled()`. For all
others it silently does nothing; the only official alternatives are root, Shizuku/ADB or device owner. The app
therefore targets API 28 on purpose. That works without root from Android 8 to today, but rules out Google Play.
Should Android ever stop installing apps with this target, a Shizuku or root path can be added.

Limits: Wi-Fi can only be switched off *after* the connection exists, so a few seconds of traffic through that
network are possible. Wi-Fi then stays off. Matching is by name only, any network with the same SSID triggers.

## Privacy

No account, no ads, no tracking and no internet permission. Only the network list, settings and log are stored,
on the device only.

The location permission is used solely to read the Wi-Fi name; the app never requests a position. Notifications
and the foreground service are only used for reliable mode and the optional notices. If Android's device backup
is enabled, the system may include the settings in it.

## Build

```sh
./gradlew assembleRelease
```

Every push to `main` builds the APK and publishes it as a release.

## License

The code is licensed under the [MIT License](LICENSE). Bundled libraries have their own licenses, see
[third-party notices](THIRD_PARTY_NOTICES.md).

Wi-Fi® is a registered trademark of the Wi-Fi Alliance, Android a trademark of Google LLC. WiFi Disabler is an
independent project and not affiliated with any of them. The icon is original artwork, not the Wi-Fi logo.
