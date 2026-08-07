# Boxters

A hex-grid word puzzle game for Android. Trace adjacent hexagonal cells to spell words, complete objectives, and progress through 56 levels across 4 game modes.

## Game Overview

Players drag through hexagonal tiles to form words. Each mode introduces different mechanics:

- **Simple** — Tiles persist after use; illuminate cells by forming words through them
- **Clear** — Tiles are removed when used; clear the entire board
- **Chain** — Tiles get new letters after use; build combos by overlapping with your previous word
- **Illuminate** — Light up tiles by tracing through them; reach the illumination target percentage

Complete 14 levels in each mode to unlock the next. 3 lives per session with a 5-minute cooldown on game over.

## Screenshots

*Coming soon*

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Canvas (no game engine)
- **Architecture:** Single-Activity, single ViewModel state machine
- **Persistence:** SharedPreferences + kotlinx.serialization
- **Audio:** SoundPool for SFX, synthesized PCM tones for letter sounds and victory fanfare
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 35

## Project Structure

```
app/src/main/
├── java/com/artmondo/boxters/
│   ├── BoxtersApp.kt                  # Application class
│   ├── MainActivity.kt                # Single activity, edge-to-edge
│   ├── data/
│   │   ├── model/                     # HexCoord, HexCell, Board, Field, GameMode,
│   │   │                                GameState, LevelData, PlayerProfile, BoardSnapshot
│   │   └── repository/                # LevelRepository (56 levels), DictionaryRepository,
│   │                                    PlayerRepository
│   ├── domain/
│   │   ├── hex/HexMath.kt             # Axial coordinate math, pixel conversion, hex geometry
│   │   ├── dictionary/Dictionary.kt   # Trie-based dictionary (80K+ words)
│   │   ├── board/                     # BoardGenerator (4 strategies), SeededRNG (mulberry32),
│   │   │                                LetterGenerator
│   │   ├── tracer/WordTracer.kt       # Touch-to-trace word input with adjacency validation
│   │   ├── objectives/ObjectiveTracker.kt  # 8 objective types with word-claim deduplication
│   │   └── scoring/ScoringEngine.kt   # Move scoring, level scoring, star ratings
│   ├── ui/
│   │   ├── theme/                     # Colors, Typography (Cinzel + Inter), Theme
│   │   ├── screens/                   # LoadingScreen, RegisterScreen, WelcomeScreen, GameScreen
│   │   ├── game/                      # GameViewModel, GameUiState, GameEvent
│   │   └── canvas/GameCanvas.kt       # Full canvas rendering: hex grid, trace lines, HUD,
│   │                                    overlays, particles
│   ├── audio/AudioManager.kt          # SoundPool SFX + synthesized tones/fanfare
│   ├── particles/ParticleSystem.kt    # Particles + 150 background stars
│   ├── sharing/BoardSharing.kt        # URL encode/decode for board sharing + deep links
│   └── util/Constants.kt              # Timing, Gameplay, Scoring, LetterValues constants
├── assets/
│   ├── dictionary.txt                 # 80,363 words
│   └── sfx/                           # 7 MP3 sound effects
└── res/
    ├── font/                          # Cinzel + Inter TTF fonts
    ├── drawable/                      # Adaptive icon (hexagon with "B")
    └── values/                        # strings.xml, themes.xml
```

## Building

### Prerequisites

- Android Studio (with bundled JDK 17+)
- Android SDK (API 35)

### Build

```bash
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

### Install on device

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Key Features

- **Hex grid rendering** — Pointy-top hexagons with axial coordinates, drawn on Compose Canvas
- **Touch tracing** — Drag through adjacent cells to form words; visual feedback with colored trace lines
- **4 game modes** — Each with unique board generation strategy and tile behavior
- **Audio synthesis** — 26 letter tones on a pentatonic scale, word chords, error sounds, victory fanfare — all generated as PCM buffers at startup
- **Particle effects** — Victory bursts, clear/chain/illuminate effects, confetti, twinkling background stars
- **Board sharing** — Deterministic PRNG (mulberry32) enables shareable board URLs via deep links
- **Persistence** — Board state saved after each word; survives app kills and restores on relaunch
- **Scoring system** — Letter values, combo multipliers, efficiency bonuses, 1-3 star ratings

## Deep Links

The app handles `https://www.boxters.com/?l=<level>&b=<letters>&a=<anchors>` URLs to load shared boards.

## Origin

Ported from a web version (vanilla JS + HTML5 Canvas) at [boxters.com](https://www.boxters.com). The Android version uses the same dictionary, level definitions, and PRNG algorithm to ensure board compatibility.

## License

Copyright (c) ArtMondo. All rights reserved.
