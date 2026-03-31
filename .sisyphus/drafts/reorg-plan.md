# Draft: MovieON Reorganization to embedCast Project

## Requirements (confirmed)
- Rename root directory `/home/fullmetal/Descargas/MovieON` to `embedCast Project` (or similar).
- Organize internal folders.
- Analyze folder purposes before final reorganization.

## Current Structure Analysis
- `tv-app/`: Android TV application (EmbedCast).
- `linux-host/`: Host-side controller (CLI and Web GUI) for Linux.
- `dev-updater/`: Development utility for deploying updates (`server.py`, `deploy_update.sh`).
- `android-sdk/`: Local Android SDK installation (build-tools, platform-tools, etc.).
- `session-ses_303e.md`: Session log from previous development work.
- `.sisyphus/`: Project management and plans.

## Technical Decisions
- Use `mv` for renaming (not a git repo).
- Proposed new name: `embedCast Project`.
- Proposed structure:
    - `apps/`: `tv-app`, `linux-host`.
    - `tools/`: `dev-updater`.
    - `sdk/`: `android-sdk`.
    - `docs/`: `session-ses_303e.md`.

## Research Findings
- The project was recently renamed from "TV Remote Control" to "EmbedCast" in the Android app code.
- The app uses WebSockets for remote control/video playback.

## Open Questions
- What is the specific purpose of `dev-updater`?
- Is `android-sdk` a full SDK or just specific tools?
- Should we use a specific naming convention (kebab-case, PascalCase with spaces)?
- Is this a git repository? (Need to check to use `git mv` if so).
