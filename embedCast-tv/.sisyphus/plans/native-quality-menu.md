# Plan: Native Quality Menu (Android TV UX)

## Goal
Implement a native Android TV UI overlay for video quality selection. Instead of relying on the web player's difficult-to-navigate interface, pressing Up/Down will open a native side-panel menu. This menu will dynamically list the available qualities extracted from JWPlayer, allowing standard D-pad navigation and selection. Left/Right keys will be restored to their native `-10s / +10s` seeking behavior when the menu is closed.

## Scope
- **IN SCOPE**:
  - Modifying `activity_main.xml` to add a `qualityMenuContainer` (a right-aligned LinearLayout/ScrollView, initially GONE).
  - Modifying `MainActivity.kt` to:
    - Inject JS that reliably fetches `jwplayer().getQualityLevels()` and passes it to Android.
    - Parse the JSON received in `onQualitiesFound` and dynamically create Android `Button` views in the menu.
    - Manage D-pad key events (`dispatchKeyEvent`) to act as a state machine: open menu, navigate menu, or seek.
- **OUT OF SCOPE**:
  - Changing the visual design of the `guideWebView` (`remote_guide.html`).
  - Handling qualities for non-JWPlayer HTML5 players (fallback to toast message if no JWPlayer is found).

## Guardrails & Assumptions
- **Guardrail (JWPlayer Readiness)**: The JS injected to fetch qualities must ensure `jwplayer().getState()` is ready before calling `getQualityLevels()`.
- **Guardrail (Focus Restoration)**: When the quality menu is closed (via Left/Right/Back or after making a selection), `webView.requestFocus()` MUST be called to prevent the Android TV remote from losing focus.
- **Guardrail (JSON Safety)**: Parsing the quality JSON must be wrapped in a try-catch to prevent crashes on malformed data.
- **Assumption**: The target video uses JWPlayer. If not, the menu won't populate, and we should degrade gracefully.

## Tasks

### Wave 1: XML Layout & JS Extraction
- **File 1**: `app/src/main/res/layout/activity_main.xml`
- **Action 1**: Inside the root `FrameLayout`, add a new `LinearLayout` (id: `qualityMenuContainer`).
  - Attributes: `layout_width="250dp"`, `layout_height="match_parent"`, `layout_gravity="end"`, `background="#E61A1A2E"`, `orientation="vertical"`, `padding="20dp"`, `visibility="gone"`, `clickable="true"`, `focusable="true"`.
  - Inside it, add a `TextView` for the title ("Calidad de Video") and a `ScrollView` containing a `LinearLayout` (id: `qualityListContainer`, `orientation="vertical"`).
- **File 2**: `app/src/main/java/com/tvremote/control/MainActivity.kt`
- **Action 2**: Create a method `extractQualities()` that evaluates JS to get qualities. The JS must check `if(typeof window.jwplayer !== 'undefined')` and then call `AndroidTV.onQualitiesFound(JSON.stringify(jwplayer().getQualityLevels()))`. Call this method when the video loads (e.g., in `onPageFinished` of the video WebView).
- **QA/Scenario**: App compiles. The layout contains the hidden container.

### Wave 3: Data Parsing & UI Generation
- **File**: `app/src/main/java/com/tvremote/control/MainActivity.kt`
- **Action**: 
  - Update `onQualitiesFound(qualities: String)` to parse the JSON array.
  - Switch to the main UI thread (`runOnUiThread`).
  - Clear `qualityListContainer.removeAllViews()`.
  - Iterate the JSON. For each quality (extract `label` and index), create a native `Button`.
  - Button attributes: text = label, `isFocusable = true`, `isFocusableInTouchMode = true`, background/text color styling for TV visibility.
  - Set an `OnClickListener` on the Button that:
    - Calls `evaluateJavascript("jwplayer().setCurrentQuality($index)", null)`
    - Calls `closeQualityMenu()` (which hides the container and calls `webView?.requestFocus()`).
- **QA/Scenario**: When a video loads, the buttons are silently created in the background container.

### Wave 4: D-Pad State Machine
- **File**: `app/src/main/java/com/tvremote/control/MainActivity.kt`
- **Action**: Update `dispatchKeyEvent`:
  - If `qualityMenuContainer.visibility == View.VISIBLE`:
    - Intercept `KEYCODE_DPAD_LEFT` or `KEYCODE_DPAD_RIGHT` or `KEYCODE_BACK`: Call `closeQualityMenu()`, return `true`.
    - Let `UP`, `DOWN`, `CENTER`, `ENTER` fall through (`return super.dispatchKeyEvent`) so Android handles button focus and clicks.
  - If `qualityMenuContainer.visibility == View.GONE`:
    - Intercept `KEYCODE_DPAD_UP` or `KEYCODE_DPAD_DOWN`: Call `openQualityMenu()` (sets `visibility = VISIBLE` and calls `qualityListContainer.getChildAt(0)?.requestFocus()`), return `true`.
    - Intercept `KEYCODE_DPAD_LEFT`: Call `controlVideo("seek", "-10")`, return `true`.
    - Intercept `KEYCODE_DPAD_RIGHT`: Call `controlVideo("seek", "10")`, return `true`.
- **QA/Scenario**: Pressing UP opens the menu. Focus works. Pressing Left closes it. Pressing Left while closed skips back 10s.

## Final Verification Wave
- [ ] Build succeeds.
- [ ] UP/DOWN opens the native quality menu.
- [ ] Menu correctly lists the JWPlayer qualities.
- [ ] D-pad correctly navigates the menu buttons.
- [ ] Selecting a quality changes it in the player and closes the menu.
- [ ] Closing the menu restores focus to the WebView.