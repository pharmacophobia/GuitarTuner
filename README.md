# 🎸 GuitarTuner Pro — Android Instrument Tuner

> **Sub-cent precision. Zero lag. Beautiful UI.**

GuitarTuner Pro is a high-precision, low-latency instrument tuner for Android built with Jetpack Compose and a custom **YIN Autocorrelation Pitch Detection DSP Engine**. Works for guitar, bass, ukulele, violin, and more.

---

## ✨ Features

- **🎯 Sub-Cent Precision DSP**
  - YIN Normalized Difference Autocorrelation algorithm with parabolic peak interpolation
  - Detects fundamentals from **40 Hz (low E on bass)** to **1200+ Hz** with < 0.1 Hz tolerance
  - Built-in noise gate and RMS silence suppression prevents jitter from background noise

- **📊 Animated Needle Gauge**
  - Shows cents deviation from -50 (flat) to +50 (sharp)
  - Radiant emerald green in-tune sweet spot (±3 cents)

- **🎸 Interactive Headstock String Selector**
  - Displays all 6 string pegs with note names and target frequencies
  - **Auto-Detect Mode** — automatically identifies which string you're playing

- **🎵 Multi-Instrument Support**
  - Guitar (Standard, Drop D, Open G, DADGAD, and more)
  - Bass Guitar (4, 5, 6 string)
  - Ukulele, Violin, Mandolin

---

## 📱 Requirements

- Android 7.0 (API 24) or higher
- Microphone permission

---

## 🚀 Build & Install

```bash
git clone https://github.com/pharmacophobia/GuitarTuner.git
cd GuitarTuner
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or grab the latest APK from [Releases](../../releases).

---

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **DSP**: Custom YIN pitch detection (pure Kotlin, no JNI)
- **Audio**: Android AudioRecord API (low-latency)

---

## 📄 License

MIT License — free to use, modify, and distribute.
