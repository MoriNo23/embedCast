# Plan: Native UX Navigation (D-Pad & Numbers)

## Goal
Delegate the handling of directional and numeric keys entirely to the native WebView player interface (like JWPlayer or YouTube). This improves UX by allowing users to navigate internal player menus (like quality or settings) using the D-Pad natively, and seek using numbers, instead of relying on forced JavaScript injections.

## Scope
- **IN SCOPE**:
  - Modifying `MainActivity.kt` to stop capturing numeric keys (`1-9`) and D-Pad horizontal keys (`LEFT`, `RIGHT`, and media rewind/fast-forward) in the Android `dispatchKeyEvent` layer.
  - Ensuring the `webView` correctly receives focus so key events fall through to the HTML player.
  - Updating `remote_guide.html` to reflect the new native UX in the Legend.
- **OUT OF SCOPE**:
  - Modifying the laptop remote control logic (WebSocket `TvWebSocketServer`). The laptop will still be able to send `seek` and `quality` commands via API.
  - Modifying system media session controls (`MediaSession`).

## Guardrails & Assumptions
- **Guardrail (Focus Loss)**: If the WebView doesn't have focus, key events won't reach the HTML player. We must ensure `webView?.requestFocus()` is called when the guide hides.
- **Guardrail (Commented Code)**: Instead of just deleting the Kotlin cases, we should remove them but leave a small Kotlin comment explaining that these keys are intentionally left to the native WebView.
- **Assumption**: The target web players (JWPlayer, native HTML5, etc.) support native keyboard navigation for their menus and seeking.

## Tasks

### Wave 1: Clean Kotlin Interceptors
- **File**: `app/src/main/java/com/tvremote/control/MainActivity.kt`
- **Action**: 
  - Locate `dispatchKeyEvent` (around lines 599-630).
  - **Remove** the cases handling `KEYCODE_1` to `KEYCODE_9`.
  - **Remove** the cases handling `KEYCODE_DPAD_LEFT`, `KEYCODE_MEDIA_REWIND`.
  - **Remove** the cases handling `KEYCODE_DPAD_RIGHT`, `KEYCODE_MEDIA_FAST_FORWARD`.
  - Leave a comment in the `else` block or above it stating: `// Numbers and D-Pad directional keys fall through to the WebView to allow native player navigation and seeking.`
- **QA/Scenario**: Compile the app. Verify that pressing `Right` or `1` on the remote no longer triggers the `sendLog` or `controlVideo` functions from Android, and instead the event goes straight to the WebView.

### Wave 2: Update Remote Guide UI Legend
- **File**: `app/src/main/assets/remote_guide.html`
- **Action**: 
  - Under `<div class="legend-category">` for "Navegación y UI", change the text for `▲ / ▼` and `◄ / ►` to explain that they navigate the native interface of the video player.
  - Remove the entire `<div class="legend-category">` for "🔢 Calidad de Video" (the one talking about keys 1-9 and 0).
  - Add a new item under "Navegación y UI" (or a new category) explaining that Number keys `0-9` jump to different percentages of the video (e.g., "1-9, 0: Salto rápido a porcentaje del video (depende del reproductor)").
- **QA/Scenario**: Open `remote_guide.html` and verify the text accurately reflects the new native UX logic without mentioning forced quality changes.

## Final Verification Wave
- [ ] Code compiles without errors (`./gradlew assembleDebug`).
- [ ] `MainActivity.kt` no longer intercepts numbers or left/right D-pad keys.
- [ ] `remote_guide.html` legend is updated and visually clean.