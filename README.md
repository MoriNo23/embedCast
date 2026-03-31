# 🎬 EmbedCast

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![WebSocket](https://img.shields.io/badge/Protocol-WebSocket-orange.svg)](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket)
[![GitHub Stars](https://img.shields.io/github/stars/MoriNo23/embedCast)](https://github.com/MoriNo23/embedCast/stargazers)
[![GitHub Issues](https://img.shields.io/github/issues/MoriNo23/embedCast)](https://github.com/MoriNo23/embedCast/issues)

> **EmbedCast** is an Android TV application for video casting and playback. Cast videos from a Linux host to your Android TV device with full remote control via WebSocket.

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#-usage)
- [API Reference](#-api-reference)
- [Contributing](#-contributing)
- [Roadmap](#-roadmap)
- [License](#-license)
- [Contact](#-contact)

## ✨ Features

- 🎥 **Video Casting** - Cast videos from Linux host to Android TV
- 🎮 **Remote Control** - Full playback control via WebSocket
- ⏯️ **Playback Controls** - Play, pause, seek, stop, quality selection
- 📺 **Android TV Optimized** - Built with Leanback for TV experience
- 🔄 **Auto-Reconnect** - Automatic WebSocket reconnection on disconnect
- 💾 **Resume Playback** - Save and restore video positions
- 🎨 **Animated Splash** - Custom animated splash screen with logo
- 📡 **Real-time Status** - Live video status synchronization

## 🏗️ Architecture

```
┌─────────────────┐     WebSocket (8080)     ┌─────────────────┐
│                 │◄────────────────────────►│                 │
│   Linux Host    │                          │   Android TV    │
│   (CLI/Web GUI) │     JSON Protocol        │   (EmbedCast)   │
│                 │                          │                 │
└─────────────────┘                          └─────────────────┘
```

### Communication Protocol

| Action   | Description              | Parameters           |
| -------- | ------------------------ | -------------------- |
| `load`   | Load video URL           | `url: string`        |
| `play`   | Play/Pause toggle        | -                    |
| `pause`  | Pause video              | -                    |
| `stop`   | Stop and reset           | -                    |
| `seek`   | Seek to position         | `seconds: int`       |
| `quality`| Change video quality     | `value: string`      |
| `reload` | Force reload (new token) | -                    |

## 🛠️ Tech Stack

### Android TV App
- **Language:** Kotlin
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **UI Framework:** Android TV Leanback
- **Video Player:** JWPlayer (WebView)
- **Networking:** Java-WebSocket 1.5.4
- **Media:** AndroidX Media 1.7.0

### Linux Host
- **CLI Tool:** .NET 10.0 (C#)
- **Web GUI:** ASP.NET Core 10.0
- **Update Server:** Python 3

## 📁 Project Structure

```
embedCast/
├── embedCast-tv/                 # 📺 Android TV Application
│   ├── app/
│   │   ├── build.gradle.kts      # Build configuration
│   │   └── src/main/
│   │       ├── java/com/embedcast/tv/
│   │       │   ├── MainActivity.kt       # Main activity
│   │       │   ├── SplashActivity.kt     # Splash screen
│   │       │   ├── VideoPlayerManager.kt # Video controls
│   │       │   ├── WebSocketManager.kt   # WebSocket server
│   │       │   └── PreferencesManager.kt # Preferences
│   │       ├── res/                      # Resources
│   │       └── AndroidManifest.xml
│   └── gradle/
├── embedCast-host/               # 🖥️ Linux Host Controller
│   ├── cli-tool/                 # Command-line interface
│   └── web-gui/                  # Web-based GUI
├── experimental/                 # 🧪 Work in Progress
│   └── embedCast-updater/        # Update notification system
├── docs/                         # 📚 Documentation
├── AGENTS.md                     # AI Assistant Guide
├── .gitignore
└── README.md
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2024.1) or later
- JDK 17+
- Android SDK with API 34
- Linux host machine (for CLI/Web GUI)
- .NET 10.0 SDK (for host tools)
- Python 3.10+ (for update server)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/MoriNo23/embedCast.git
   cd embedCast
   ```

2. **Open Android TV project**
   ```bash
   cd embedCast-tv
   ```

3. **Configure local.properties**
   ```properties
   sdk.dir=/path/to/your/Android/Sdk
   ```

4. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

5. **Install on device**
   ```bash
   ./gradlew installDebug
   ```

## 📖 Usage

### Starting the WebSocket Server

The Android TV app automatically starts a WebSocket server on port 8080 when launched.

```kotlin
// The server starts automatically in MainActivity
webSocketManager.startServer { json -> 
    handleCommand(json) 
}
```

### Sending Commands from Host

```bash
# Using the CLI tool
./embedCast-host/cli-tool/bin/Debug/net10.0/cli-tool load "https://example.com/video.mp4"

# Using WebSocket directly (Python example)
import websocket
ws = websocket.WebSocket()
ws.connect("ws://TV_IP:8080")
ws.send('{"action": "play"}')
```

## 📡 API Reference

### WebSocket Messages

**Request Format:**
```json
{
  "action": "load|play|pause|stop|seek|quality|reload",
  "url": "https://...",      // for load action
  "seconds": 10,             // for seek action
  "value": "1"               // for quality action
}
```

**Response Format:**
```json
{
  "type": "status",
  "currentTime": 120.5,
  "duration": 3600.0,
  "paused": false
}
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Guidelines

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Keep `MainActivity.kt` under 500 lines
- Use `PackageInfoCompat` for version checks
- Test WebSocket compatibility after changes

## 🗺️ Roadmap

- [ ] Adaptive icons for Android 8.0+
- [ ] CI/CD pipeline with GitHub Actions
- [ ] Multi-device casting support
- [ ] Video playlist management
- [ ] Subtitle support
- [ ] Chromecast compatibility
- [ ] iOS companion app

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

## 📬 Contact

**MoriNo23** - [@MoriNo23](https://github.com/MoriNo23)

Project Link: [https://github.com/MoriNo23/embedCast](https://github.com/MoriNo23/embedCast)

---

⭐ **Star this repo if you find it useful!**
