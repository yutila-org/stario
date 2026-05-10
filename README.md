<h1 align="center">Stario</h1>

<div align="center">
  <img src="docs/representative.png" alt="Stario representative">
</div>

<p align="center">
  <a href="https://github.com/yutila-org/stario/actions/workflows/build.yml"><img src="https://img.shields.io/github/actions/workflow/status/yutila-org/stario/build.yml?label=Build" alt="Build"></a>
  <a href="https://github.com/yutila-org/stario/actions/workflows/trivy.yml"><img src="https://img.shields.io/github/actions/workflow/status/yutila-org/stario/trivy.yml?label=Trivy" alt="Trivy"></a>
  <a href="https://github.com/yutila-org/stario/actions/workflows/gitleaks.yml"><img src="https://img.shields.io/github/actions/workflow/status/yutila-org/stario/gitleaks.yml?label=Secret%20Scan" alt="Secret Scan"></a>
  <a href="https://github.com/yutila-org/stario/actions/workflows/sbom.yml"><img src="https://img.shields.io/github/actions/workflow/status/yutila-org/stario/sbom.yml?label=SBOM" alt="SBOM"></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Kotlin-0095D5?style=flat&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white" alt="Docker">
</p>

<div align="center">
  <b><a href="#-overview">Overview</a></b> •
  <b><a href="#-features">Features</a></b> •
  <b><a href="#-download">Download</a></b> •
  <b><a href="#-compatibility">Compatibility</a></b> •
  <b><a href="#-development">Development</a></b> •
  <b><a href="#-building">Building</a></b>
</div>

<br>

## <img src="https://api.iconify.design/material-symbols/info-outline.svg?color=%230288D1" width="24" height="24" align="absmiddle"> Overview

Stario is a minimalist Android launcher designed to organize applications efficiently without distractions. This codebase is an active fork maintained by [Yutila](https://github.com/yutila-org) for internal infrastructure alignment and continued development.

## <img src="https://api.iconify.design/material-symbols/star-outline.svg?color=%23FBC02D" width="24" height="24" align="absmiddle"> Features

> **<img src="https://api.iconify.design/material-symbols/palette-outline.svg?color=%23E91E63" width="18" height="18" align="absmiddle"> Material You Support**<br>
> Integrates seamlessly with Android’s Material You dynamic theming system, adapting colors based on your wallpaper and device settings.

> **<img src="https://api.iconify.design/material-symbols/settings-outline.svg?color=%23607D8B" width="18" height="18" align="absmiddle"> Application Customization**<br>
> Customize your home screen with various icon packs and shapes to personalize your experience.

> **<img src="https://api.iconify.design/material-symbols/partly-cloudy-day-outline.svg?color=%230288D1" width="18" height="18" align="absmiddle"> Built-In Weather Widget**<br>
> Check current weather conditions and forecasts right from your home screen.

> **<img src="https://api.iconify.design/material-symbols/search.svg?color=%2343A047" width="18" height="18" align="absmiddle"> Global Search Integration**<br>
> Perform fast, privacy-respecting searches using Kagi directly from the launcher.

> **<img src="https://api.iconify.design/material-symbols/play-circle-outline.svg?color=%23F44336" width="18" height="18" align="absmiddle"> Minimalistic Media Player Controls**<br>
> Manage your media playback easily with integrated controls.

> **<img src="https://api.iconify.design/material-symbols/folder-outline.svg?color=%23FBC02D" width="18" height="18" align="absmiddle"> Application Categories**<br>
> Organize your app drawer with customizable categories for better app management.

> **<img src="https://api.iconify.design/material-symbols/rss-feed.svg?color=%23EF6C00" width="18" height="18" align="absmiddle"> RSS/Atom Reader**<br>
> Stay up-to-date with news and blog feeds via the integrated RSS/Atom reader.

> **<img src="https://api.iconify.design/material-symbols/sort.svg?color=%237E57C2" width="18" height="18" align="absmiddle"> Page Sorting**<br>
> Easily reorder your home screen pages to suit your workflow.

## <img src="https://api.iconify.design/mdi/download.svg?color=%2343A047" width="24" height="24" align="absmiddle"> Download

> [!TIP]
> **Distribution:**  
> Yutila designates the [GitHub Releases](https://github.com/yutila-org/stario/releases/latest) page as the sole authoritative distribution mechanism for compiled Stario APKs. All other distribution channels have been deprecated.

## <img src="https://api.iconify.design/mdi/cellphone-arrow-down.svg?color=%23009688" width="24" height="24" align="absmiddle"> Installation

Yutila guarantees that Stario will remain a freely accessible, independent binary, distributed independently of the Google Play ecosystem and irrespective of future OS-level verification constraints.

### Standard Installation

1. Download the compiled `APK` directly from the [GitHub Releases](https://github.com/yutila-org/stario/releases/latest) page.
2. Navigate to **Settings > Apps > Special app access > Install unknown apps** (path may vary by OEM).
3. Grant installation permissions to your designated file manager or browser.
4. Execute the APK package to initiate installation.

### CLI ~(Fallback)~

For OEM builds or future OS versions that aggressively block on-device unverified package parsing, utilize the Android Debug Bridge (`adb`):

```bash
# Verify device connection
adb devices

# Install the application package
adb install /path/to/stario-release.apk
```

## <img src="https://api.iconify.design/material-symbols/devices.svg?color=%237E57C2" width="24" height="24" align="absmiddle"> Compatibility

- Requires **Android SDK 29+** (Android 10.0 or later)
- Compatible with AOSP and most major OEM devices
- Should work with custom ROMs, though these are not officially tested — user feedback is welcome

## <img src="https://api.iconify.design/material-symbols/code.svg?color=%23607D8B" width="24" height="24" align="absmiddle"> Development

You can quickly set up the development environment using the provided Dockerfile:

```bash
docker build --platform linux/amd64 -t stario-dev .

docker run --platform linux/amd64 --rm -it \
  -v </path/to/output>:/usr/local/stario/build \
  stario-dev
```

> [!TIP]
> Use `--rm` to automatically remove the container after use.

## <img src="https://api.iconify.design/material-symbols/build-outline.svg?color=%23EF6C00" width="24" height="24" align="absmiddle"> Building

Should you wish to build the application yourself, run the build
script from within the development environment:

```bash
# Optionally, checkout to the tagged commit
git checkout v2.9

./build.sh
```

Alternatively, to also build a signed copy (APK and AAB), pass a keystore to the build script:

```bash
docker run --platform linux/amd64 --rm -it \
  -v </path/to/output>:/usr/local/stario/build \
  -v </path/to/keystore>:/usr/local/stario/keystore \
  stario-dev

# Optionally, checkout to the tagged commit
git checkout v2.9
  
./build.sh \
  -K /usr/local/stario/keystore/keystore.jks \
  -P keystore_password \
  -a key_alias \
  -p key_password
```

## <img src="https://api.iconify.design/material-symbols/verified-outline.svg?color=%23009688" width="24" height="24" align="absmiddle"> Reproducible Builds

Check for RBs with the locally built unsigned APK and [apksigcopier](https://github.com/obfusk/apksigcopier). 

Firstly, copy the signature from the signed APK onto your built unsigned APK:

```bash
apksigcopier copy signed-from-source.apk unsigned-built-locally.apk out.apk
```

Then compare the two APKs:

```bash
apksigcopier compare stario-from-source.apk stario-built-locally.apk
```

> [!NOTE]
> `apksigcopier compare` requires [apksigner](https://developer.android.com/tools/apksigner).

## <img src="https://api.iconify.design/mdi/heart-outline.svg?color=%23E91E63" width="24" height="24" align="absmiddle"> Attribution

Original work and Stario concept developed by [Răzvan Albu](https://github.com/albu-razvan).
