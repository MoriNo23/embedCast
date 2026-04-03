# EmbedCast - AI Assistant Guide

> **Purpose:** Project context and rules for AI coding assistants.
> **Update:** When adding dependencies or changing protocols.

## Project Overview

**EmbedCast** - Android TV app for video casting with remote control via WebSocket.

| Component | Technology | Criticality |
|-----------|------------|-------------|
| Android TV | Kotlin, Leanback | 🔴 Core |
| Host Control | .NET 10.0, C# | 🔴 Core |
| Communication | WebSocket (port 8080) | 🔴 Critical |

## Architecture

```
Linux Host ←→ WebSocket (8080) ←→ Android TV
CLI/Web GUI              JWPlayer
```

## Critical Files

| File | Purpose | DO NOT MODIFY |
|------|---------|----------------|
| `embedCast-tv/app/.../MainActivity.kt` | Core activity | Without approval |
| `embedCast-tv/app/.../WebSocketManager.kt` | WebSocket server | - |
| `embedCast-sdk/` | Local SDK | ❌ Never |

## Build Commands

### Android TV (embedCast-tv/)
```bash
cd embedCast-tv
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease          # Release build
./gradlew test                     # Run all tests
./gradlew test --tests "WebSocketManagerTest"  # Single test class
./gradlew test --tests "*Test"     # Run all test classes
./gradlew installDebug             # Install on device
./gradlew clean                    # Clean build
```

### Host Tools
```bash
# CLI Tool
cd embedCast-host/cli-tool
dotnet build --configuration Release
dotnet run -- <command>

# Web GUI
cd embedCast-host/web-gui
dotnet build --configuration Release
dotnet run
```

## Code Style Guidelines

### Kotlin (Android)
- **Indentation:** 4 spaces
- **Imports:** Group by package, no wildcards
- **Naming:**
  - Classes: `PascalCase` (e.g., `MainActivity`)
  - Functions: `camelCase` (e.g., `startServer()`)
  - Constants: `SCREAMING_SNAKE_CASE` in companion objects
  - Private fields: `camelCase` with `private` modifier
- **Error Handling:** Use specific exceptions, log with `Log.e(TAG, message, exception)`
- **Null Safety:** Prefer `?.let` over `!!`, use `lateinit` for injected dependencies
- **Functions:** Keep under 50 lines, single responsibility
- **Classes:** Keep under 500 lines

### C# (Host Tools)
- **Indentation:** 4 spaces
- **Naming:**
  - Classes: `PascalCase`
  - Methods: `PascalCase`
  - Variables: `camelCase`
  - Constants: `PascalCase`
- **Error Handling:** Use try-catch with specific exception types
- **Async:** Use `async/await` consistently, avoid `.Result`

## Testing

### Android Tests
- **Framework:** JUnit 4 + Mockito
- **Location:** `src/test/java/com/tvremote/control/`
- **Naming:** `*Test.kt` (e.g., `WebSocketManagerTest.kt`)
- **Run single test:** `./gradlew test --tests "WebSocketManagerTest"`
- **Annotations:** `@RunWith(MockitoJUnitRunner::class)` for mock tests

### Test Patterns
```kotlin
@RunWith(MockitoJUnitRunner::class)
class WebSocketManagerTest {
    @Mock
    private lateinit var mockServer: TvWebSocketServer
    
    @Before
    fun setup() { }
    
    @Test
    fun `descriptive test name`() { }
}
```

## WebSocket Protocol

**Port:** 8080

```json
// Commands
{"action": "load", "url": "..."}
{"action": "play|pause|stop|reload"}
{"action": "seek", "seconds": 10}
{"action": "quality", "value": "1"}

// Responses
{"type": "status", "currentTime": 0, "duration": 0, "paused": false}
```

## Development Rules

### ✅ DO
- Use Kotlin for Android code
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Keep files under 500 lines
- Use `PackageInfoCompat` for version checks
- Use `overrideActivityTransition()` for API 34+
- Update docs when changing protocols
- Test WebSocket compatibility after changes
- Run tests before committing: `./gradlew test`

### ❌ DON'T
- Modify `embedCast-sdk/`
- Change port 8080 without updating both sides
- Add dependencies without approval
- Break backward compatibility
- Use deprecated Android APIs
- Commit without running tests

## Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| Activities | `*Activity.kt` | `SplashActivity.kt` |
| Managers | `*Manager.kt` | `VideoPlayerManager.kt` |
| Helpers | `*Helper.kt` | `LoggingHelper.kt` |
| Layouts | `activity_*.xml` | `activity_splash.xml` |
| Tests | `*Test.kt` | `WebSocketManagerTest.kt` |

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Java-WebSocket | 1.5.4 | WebSocket server |
| AndroidX Media | 1.7.0 | Media session |
| JUnit | 4.13.2 | Testing |
| Mockito | 5.5.0 | Mocking |

## Troubleshooting

### Build Fails
1. Check `local.properties` SDK path
2. Run `./gradlew clean`
3. Clear cache: `rm -rf ~/.gradle/caches/`

### WebSocket Issues
1. Check port 8080 availability
2. Verify network connectivity
3. Check `AndroidManifest.xml` for INTERNET permission

### App Crashes
1. Check logs: `adb logcat | grep EmbedCast`
2. Verify `minSdk` compatibility

## Example Prompts

```
# Add new video action
Add a "volume" action to WebSocketManager that accepts level 0-100

# Fix video player
Debug why videos don't resume after pause in VideoPlayerManager

# Add new feature
Implement quality selection menu in MainActivity following Leanback guidelines

# Run specific test
Run the WebSocketManagerTest to verify the fix
```

## Update This File When

- Adding new dependencies
- Changing WebSocket protocol
- Modifying project structure
- Adding new build steps
- Changing test framework
