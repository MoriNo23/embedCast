# Plan: Auto-Recovery & Token Refresh for JWPlayer

## Goal
Fix the issue where video streaming tokens expire (e.g., after hours of playback or pausing) causing the player to block completely. The current `forceReload` uses cached data, preserving the expired token. We will upgrade the RED button to perform a "Hard Reload" (clearing cache and forcing a fresh embed URL load to generate a new token). Additionally, we will inject a JavaScript watcher that listens for JWPlayer media/network errors and automatically triggers this recovery process without user intervention.

## Scope
- **IN SCOPE**:
  - Modifying `forceReload()` in `MainActivity.kt` to call `clearCache(true)` and `loadUrl(currentUrl)`.
  - Injecting JS event listeners into `jwplayer().on('error')` and `jwplayer().on('setupError')` to call `AndroidTV.onConnectionInterrupted()`.
- **OUT OF SCOPE**:
  - Handling non-JWPlayer error events (we will rely on the RED button manual reload for them).
  - Changing the `TvWebSocketServer` architecture.

## Guardrails & Assumptions
- **Guardrail (Endless Loop Prevention)**: If a video is genuinely broken (not just an expired token), the auto-recovery might loop infinitely. We will add a simple flag or let the user manually stop it if needed, but for now, the 3-second delay in `onConnectionInterrupted()` provides a buffer to back out.
- **Assumption**: `currentUrl` contains the embed page, and reloading it without cache forces the server to mint a new temporary token for the media file.

## Tasks

### Wave 1: Implement Hard Reload
- **File**: `app/src/main/java/com/tvremote/control/MainActivity.kt`
- **Action**: Locate `forceReload()` (around line 478). Replace the `webView?.reload()` logic with:
  ```kotlin
  if (webView != null && currentUrl.isNotEmpty()) {
      sendLog("Hard Reloading player (Clearing cache for new token)...")
      statusText.text = "Regenerando Token..."
      statusText.visibility = View.VISIBLE
      statusText.postDelayed({ statusText.visibility = View.GONE }, 1500)
      
      webView?.clearCache(true)
      webView?.loadUrl(currentUrl)
      webView?.requestFocus()
  }
  ```
- **QA/Scenario**: Pressing the RED button says "Regenerando Token..." and completely reloads the page, bypassing cache.

### Wave 2: Inject JS Error Watchers
- **File**: `app/src/main/java/com/tvremote/control/MainActivity.kt`
- **Action**: In the `extractQualities()` method (around line 364), right after setting up the qualities, inject listeners to watch for JWPlayer errors:
  ```javascript
  jwplayer().on('error', function(e) {
      console.log('JWPlayer Error:', e);
      if(typeof AndroidTV !== 'undefined') AndroidTV.onConnectionInterrupted();
  });
  jwplayer().on('setupError', function(e) {
      console.log('JWPlayer Setup Error:', e);
      if(typeof AndroidTV !== 'undefined') AndroidTV.onConnectionInterrupted();
  });
  ```
- **QA/Scenario**: If a video breaks or token expires, JWPlayer emits an error. The Android side catches it and shows "Connection lost... Auto-recovering in 3s", then forces a hard reload.

### Wave 3: Auto-Play After Hard Reload
- **File**: `app/src/main/java/com/tvremote/control/MainActivity.kt`
- **Action**: Modify `onConnectionInterrupted()` (around line 796) to call `forceReload()` as it already does, but also add a small delay after the reload to send `jwplayer().play(true)` via `evaluateJavascript`:
  ```kotlin
  override fun onConnectionInterrupted() {
      runOnUiThread {
          statusText.text = "Connection lost...\nAuto-recovering in 3s"
          statusText.visibility = View.VISIBLE
          statusText.postDelayed({
              forceReload()
              // Give the new page 2 seconds to load JWPlayer, then autoplay
              webView?.postDelayed({
                  webView?.evaluateJavascript("if(typeof jwplayer !== 'undefined') jwplayer().play(true);", null)
              }, 2000)
          }, 3000)
      }
  }
  ```
- **QA/Scenario**: After auto-recovery, the video should start playing automatically at the saved position (JWPlayer's default behavior with autoplay flag) without requiring the user to press Play.

## Final Verification Wave
- [ ] Build succeeds without compilation errors.
- [ ] RED button correctly performs a clear-cache reload.
- [ ] JS error watchers are successfully injected alongside qualities.
- [ ] Auto-play is triggered 2 seconds after hard reload in the auto-recovery flow.
- [ ] Testing a broken video or network drop triggers the auto-recovery sequence and video starts playing automatically.