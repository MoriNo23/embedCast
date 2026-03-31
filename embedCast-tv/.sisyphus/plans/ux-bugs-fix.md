# Plan: UX Bugs Fix (Numbers, Quality Menu, Resume)

## Goal
Fix 3 bugs reported by the user after implementing the native quality menu:
1. Number keys (0-9) don't seek to percentage of video (JWPlayer doesn't support this natively)
2. Quality menu shows empty when no qualities are available (should show a fallback message)
3. Video restarts from beginning instead of resuming from history (race condition with extractQualities)

## Scope
- **IN SCOPE**:
  - Restoring KEYCODE_1..9 interception in `dispatchKeyEvent` to seek to percentage via JS
  - Updating `onQualitiesFound()` to show a fallback button when quality list is empty
  - Delaying `extractQualities()` call to avoid race condition with video resume logic
- **OUT OF SCOPE**:
  - Changing the quality menu UI design
  - Changing the remote guide HTML

## Guardrails & Assumptions
- **Guardrail (Resume Timing)**: `extractQualities()` must NOT fire before the video resume logic completes. Add a 2-second delay.
- **Guardrail (Empty Qualities)**: If `jsonArray.length() == 0`, create a single "Calidad Unica (Auto)" button that just closes the menu.
- **Assumption**: JWPlayer's `getDuration()` returns the total video duration in seconds.

## Tasks

### Wave 1: Restore Number Keys (0-9) for Percentage Seeking
- **File**: `app/src/main/java/com/tvremote/control/MainActivity.kt`
- **Action**: In `dispatchKeyEvent`, add cases for KEYCODE_1 through KEYCODE_9 and KEYCODE_0:
  - KEYCODE_1: seek to 10% (`controlVideo("seekPercent", "0.1")`)
  - KEYCODE_2: seek to 20% (`controlVideo("seekPercent", "0.2")`)
  - ... up to KEYCODE_9: seek to 90%
  - KEYCODE_0: seek to 0% (beginning)
- **Also**: Add a new JS block in `controlVideo()` for the `seekPercent` action that calculates `jwplayer().getDuration() * fraction` and calls `jwplayer().seek()`.
- **QA/Scenario**: Pressing 5 on the remote jumps to 50% of the video.

### Wave 2: Fix Empty Quality Menu
- **File**: `app/src/main/java/com/tvremote/control/MainActivity.kt`
- **Action**: In `onQualitiesFound()`, after parsing the JSON, check if `jsonArray.length() == 0`. If so, create a single Button with text "Calidad Unica (Auto)" that calls `closeQualityMenu()` on click.
- **QA/Scenario**: Opening the quality menu on a video with only one quality shows "Calidad Unica (Auto)" instead of an empty panel.

### Wave 3: Fix Video Resume Race Condition
- **File**: `app/src/main/java/com/tvremote/control/MainActivity.kt`
- **Action**: In `onPageFinished` override inside `createNewWebView()`, delay the `extractQualities()` call by 2 seconds using `view?.postDelayed({ extractQualities() }, 2000)`.
- **QA/Scenario**: Video resumes from saved position instead of restarting from beginning.

## Final Verification Wave
- [ ] Build succeeds.
- [ ] Number keys 0-9 seek to correct percentage.
- [ ] Quality menu shows fallback message when empty.
- [ ] Video resumes from history correctly.