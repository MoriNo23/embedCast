# Plan: Remote Guide SVG Connectors

## Goal
Improve the visual quality and accuracy of the TV remote guide overlay by replacing text emojis with scalable inline SVG icons and fixing the connector lines to point precisely to their respective buttons regardless of screen size.

## Scope
- **IN SCOPE**: 
  - Modifying `app/src/main/assets/remote_guide.html` to include inline SVGs and dynamic positioning JS.
  - Modifying `app/src/main/java/com/tvremote/control/MainActivity.kt` to trigger the positioning logic.
- **OUT OF SCOPE**: Changing the overall layout structure, modifying CSS colors/themes, or changing key event handling.

## Guardrails & Assumptions
- **Guardrail (Timing Race Condition)**: Ensure a small delay (`setTimeout` of 100-200ms) is used before calculating `getBoundingClientRect()` to ensure the WebView has fully rendered the DOM and applied CSS.
- **Guardrail (WebView Compatibility)**: Use `fill="currentColor"` for SVGs to inherit CSS colors seamlessly.
- **Assumption**: All SVGs are self-contained and require no external network requests (CDN).

## Tasks

### Wave 1: Replace Emojis with SVGs
- **File**: `app/src/main/assets/remote_guide.html`
- **Action**: Replace the following 10 emojis with inline `<svg>` elements inside their respective `<button>` tags:
  1. `⏻` (Power) -> `<svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">...`
  2. `🎤` (Mic)
  3. `▲` (Up)
  4. `▼` (Down)
  5. `◄` (Left)
  6. `►` (Right)
  7. `🏠` (Home)
  8. `↩` (Back x2)
  9. `⚙` (Settings)
  10. `●` (Record dot)
- **QA/Scenario**: Open `remote_guide.html` in a browser. Verify no emojis are visible inside the remote buttons, and SVGs are correctly styled and sized (24x24).

### Wave 2: Dynamic Connector Lines JavaScript
- **File**: `app/src/main/assets/remote_guide.html`
- **Action**: 
  - Remove all hardcoded `top`, `left`, and `width` CSS properties from `#line-info`, `#line-updown`, `#line-red`, and `#line-sub` rules.
  - Add a JavaScript function `positionConnectors()` that:
    - Waits for a small `setTimeout` (e.g., 150ms) to ensure layout completion.
    - Uses `getBoundingClientRect()` on the target buttons (`btn-info`, `btn-up` / `btn-down` average, `btn-red`, `btn-sub`) and their respective legend items to calculate the `top`, `left`, and `width` of the connector lines dynamically.
    - Sets these calculated styles directly on the connector line elements.
    - Handle null checks properly.
- **QA/Scenario**: Verify the `positionConnectors` function accurately sets inline styles for the 4 line elements based on dynamic rects.

### Wave 3: Trigger Positioning from MainActivity
- **File**: `app/src/main/java/com/tvremote/control/MainActivity.kt`
- **Action**: In `onCreate`, inside the `guideWebView.webViewClient`'s `onPageFinished` method, add a second `evaluateJavascript` call to invoke the `positionConnectors()` function.
  - E.g.: `guideWebView.evaluateJavascript("positionConnectors()", null)`
- **QA/Scenario**: Verify compilation succeeds and the Android app automatically triggers connector repositioning upon loading the guide overlay.

## Final Verification Wave
- [ ] Verify all 10 buttons show SVG icons instead of emojis.
- [ ] Verify the 4 connector lines accurately point from the Legend section to the specific buttons.
- [ ] Verify no JavaScript errors occur in the WebView console.
- [ ] Confirm the overlay still displays and hides correctly with the INFO button.

## Task Dependency Graph

| Task | Depends On | Reason |
|------|------------|--------|
| Task 1 (Replace Emojis with SVGs) | None | Starting point, no prerequisites |
| Task 2 (Dynamic Connector Lines JavaScript) | Task 1 | Requires SVG buttons to be in place for proper positioning calculations |
| Task 3 (Trigger Positioning from MainActivity) | Task 2 | Requires the positionConnectors() function to exist in the HTML file |
| Task 4 (Final Verification) | Task 1, Task 2, Task 3 | Needs all implementation tasks completed to verify the full solution |

## Parallel Execution Graph

**Wave 1 (Start immediately):**
├── Task 1: Replace Emojis with SVGs (no dependencies)
└── Task 4: Final Verification Preparation (can begin gathering SVG assets and test cases)

**Wave 2 (After Wave 1 completes):**
├── Task 2: Dynamic Connector Lines JavaScript (depends: Task 1)
└── Task 4: Final Verification Preparation (continues: depends on Task 1)

**Wave 3 (After Wave 2 completes):**
├── Task 3: Trigger Positioning from MainActivity (depends: Task 2)
└── Task 4: Final Verification Execution (depends: Task 2, Task 3)

**Wave 4 (After Wave 3 completes):**
└── Task 4: Final Verification (depends: Task 3)

Critical Path: Task 1 → Task 2 → Task 3 → Task 4
Estimated Parallel Speedup: 33% faster than sequential (Tasks 1 and 4 prep can run in parallel initially)

## Category + Skills Recommendations

### Task 1: Replace Emojis with SVGs

**Delegation Recommendation:**
- Category: `visual-engineering` - Task involves SVG creation and HTML/CSS modifications for visual improvements
- Skills: [`frontend-design`] - Needed for creating high-quality SVG icons that match the existing design

**Skills Evaluation:**
- ✅ INCLUDED `frontend-design`: Essential for creating proper SVG icons that scale correctly and match visual style
- ❌ OMITTED `context7`: Not needed as SVG creation is straightforward and doesn't require external library documentation
- ❌ OMITTED `clean-code`: While good practice, the task is focused on visual implementation rather than complex code structure

**Depends On**: None
**Acceptance Criteria**: All 10 emojis replaced with inline SVGs that are 24x24 pixels and inherit CSS colors properly

### Task 2: Dynamic Connector Lines JavaScript

**Delegation Recommendation:**
- Category: `deep` - Requires understanding of DOM manipulation, getBoundingClientRect(), and timing considerations
- Skills: [`webapp-testing`] - Needed to verify the JavaScript positioning works correctly across different screen sizes

**Skills Evaluation:**
- ✅ INCLUDED `webapp-testing`: Critical for testing the dynamic positioning logic and ensuring it works correctly
- ❌ OMITTED `playwright`: Overkill for this task as we're testing within the existing WebView context
- ❌ OMITTED `context7`: JavaScript DOM APIs are well-known and don't require external documentation lookup

**Depends On**: Task 1
**Acceptance Criteria**: Connector lines dynamically position themselves based on button locations with proper null handling

### Task 3: Trigger Positioning from MainActivity

**Delegation Recommendation:**
- Category: `quick` - Simple Kotlin modification to add an evaluateJavascript call
- Skills: [`git-master`] - Ensures proper atomic commits for this small change

**Skills Evaluation:**
- ✅ INCLUDED `git-master`: Important for making clean, atomic commits of the Kotlin change
- ❌ OMITTED `clean-code`: The change is a single line addition where clean code principles are less critical
- ❌ OMITTED `documentation-lookup`: Android WebView evaluateJavascript is well-known API

**Depends On**: Task 2
**Acceptance Criteria**: MainActivity.kt compiles successfully and calls positionConnectors() after page load

### Task 4: Final Verification

**Delegation Recommendation:**
- Category: `writing` - Focuses on creating verification checklists and test procedures
- Skills: [`webapp-testing`] - Needed to execute the verification steps in actual browser/WebView environments

**Skills Evaluation:**
- ✅ INCLUDED `webapp-testing`: Essential for running the verification scenarios and checking results
- ❌ OMITTED `frontend-design`: Verification doesn't require design skills, just execution of test steps
- ❌ OMITTED `context7`: Verification uses standard web testing approaches

**Depends On**: Task 3
**Acceptance Criteria**: All verification checklist items pass: SVGs visible, connectors point correctly, no JS errors, overlay functions properly

## Commit Strategy

**Atomic Commits:**
1. **Commit 1**: SVG replacement in remote_guide.html
   - Scope: Only changes to replace emojis with SVG icons
   - Message: "feat(remote-guide): replace emojis with inline SVG icons"
   - Files: app/src/main/assets/remote_guide.html

2. **Commit 2**: Dynamic connector positioning JavaScript
   - Scope: Only changes to add positionConnectors() function and remove hardcoded CSS
   - Message: "feat(remote-guide): implement dynamic connector line positioning"
   - Files: app/src/main/assets/remote_guide.html

3. **Commit 3**: MainActivity.kt trigger
   - Scope: Only the evaluateJavascript call addition in MainActivity.kt
   - Message: "feat(remote-guide): trigger connector positioning from MainActivity"
   - Files: app/src/main/java/com/tvremote/control/MainActivity.kt

4. **Commit 4**: Verification and cleanup
   - Scope: Any test files or temporary verification artifacts
   - Message: "chore(remote-guide): add verification steps and cleanup"
   - Files: Any verification/test files created

**Commit Rules:**
- Each commit must be buildable and not break existing functionality
- Commit messages follow conventional commit format (feat/, fix/, chore/)
- No commit should touch more than one logical concern
- All commits must pass basic compilation checks

## Success Criteria

- All 10 remote control buttons display SVG icons instead of text emojis
- Connector lines dynamically adjust their position to point accurately to buttons regardless of screen size or resolution
- No JavaScript errors appear in WebView console during operation
- The remote guide overlay continues to show/hide correctly when the INFO button is pressed
- Implementation works across different Android screen sizes and densities
- Code follows existing project conventions and styling

## TODO List (ADD THESE)

> CALLER: Add these TODOs using TodoWrite/TaskCreate and execute by wave.

### Wave 1 (Start Immediately - No Dependencies)

- [ ] **1. Replace Emojis with SVGs**
  - What: Replace 10 emojis in remote_guide.html with inline SVG icons (Power, Mic, Up, Down, Left, Right, Home, Back x2, Settings, Record dot)
  - Depends: None
  - Blocks: Task 2 (Dynamic Connector Lines JavaScript)
  - Category: `visual-engineering`
  - Skills: [`frontend-design`]
  - QA: Open remote_guide.html in browser and verify no emojis are visible, SVGs are 24x24px and inherit CSS colors

- [ ] **2. Final Verification Preparation**
  - What: Gather SVG assets, create test cases for different screen sizes, prepare verification checklist
  - Depends: None
  - Blocks: Task 4 (Final Verification)
  - Category: `writing`
  - Skills: [`webapp-testing`]
  - QA: Have test cases ready for SVG verification and connector positioning validation

### Wave 2 (After Wave 1 Completes)

- [ ] **3. Dynamic Connector Lines JavaScript**
  - What: Remove hardcoded CSS for connector lines, add positionConnectors() function with setTimeout and getBoundingClientRect() logic
  - Depends: 1
  - Blocks: Task 3 (Trigger Positioning from MainActivity)
  - Category: `deep`
  - Skills: [`webapp-testing`]
  - QA: Verify positionConnectors() function sets correct inline styles for 4 line elements based on dynamic button positions

- [ ] **4. Final Verification Preparation (continued)**
  - What: Continue preparing verification steps, create test harness for different screen densities
  - Depends: 1
  - Blocks: Task 4 (Final Verification)
  - Category: `writing`
  - Skills: [`webapp-testing`]
  - QA: Test harness ready for validating connector positioning across screen sizes

### Wave 3 (After Wave 2 Completes)

- [ ] **5. Trigger Positioning from MainActivity**
  - What: Add guideWebView.evaluateJavascript("positionConnectors()", null) in onPageFinished method of MainActivity.kt
  - Depends: 2
  - Blocks: Task 4 (Final Verification Execution)
  - Category: `quick`
  - Skills: [`git-master`]
  - QA: Verify MainActivity.kt compiles successfully and triggers connector positioning on page load

- [ ] **6. Final Verification Execution**
  - What: Execute verification checklist: check SVGs, connector positioning, JS errors, overlay functionality
  - Depends: 2, 3
  - Blocks: None
  - Category: `writing`
  - Skills: [`webapp-testing`]
  - QA: All verification checklist items pass: SVGs visible, connectors point correctly, no JS errors, overlay functions properly

## Execution Instructions

1. **Wave 1**: Fire these tasks IN PARALLEL (no dependencies)
   ```
   task(category="visual-engineering", load_skills=["frontend-design"], run_in_background=false, prompt="Task 1: Replace Emojis with SVGs")
   task(category="writing", load_skills=["webapp-testing"], run_in_background=false, prompt="Task 2: Final Verification Preparation")
   ```

2. **Wave 2**: After Wave 1 completes, fire next wave IN PARALLEL
   ```
   task(category="deep", load_skills=["webapp-testing"], run_in_background=false, prompt="Task 3: Dynamic Connector Lines JavaScript")
   task(category="writing", load_skills=["webapp-testing"], run_in_background=false, prompt="Task 4: Final Verification Preparation (continued)")
   ```

3. **Wave 3**: After Wave 2 completes, fire next wave IN PARALLEL
   ```
   task(category="quick", load_skills=["git-master"], run_in_background=false, prompt="Task 5: Trigger Positioning from MainActivity")
   task(category="writing", load_skills=["webapp-testing"], run_in_background=false, prompt="Task 6: Final Verification Execution")
   ```

4. **Final QA**: Verify all tasks pass their QA criteria
   - Confirm all 10 buttons show SVG icons instead of emojis
   - Confirm connector lines accurately point from Legend section to specific buttons
   - Confirm no JavaScript errors occur in WebView console
   - Confirm overlay still displays and hides correctly with INFO button
