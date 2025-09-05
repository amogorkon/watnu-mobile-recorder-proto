# Spec 00 — Introduction

## Project

Watnu Listener — Vertical prototype for hands-free audio capture using knuckle-knock gestures and ring buffers to provide pre-roll audio capture.

## Objective

Create a functional proof-of-concept Android application that demonstrates the core innovation: reliable, hands-free audio recording triggered and stopped by knuckle-knock gestures on the device, utilizing a ring buffer for pre-roll capture.

## Scope (MVP)

Included:

- Foreground service with a persistent notification ("Watnu Listener - Audio buffer active").
- Dual ring buffers: a short trigger buffer (10s) for knock detection and a longer content buffer (default 120s) for pre-roll capture.
- On-device knuckle-knock gesture recognition (single knock to start, double knock to stop).
- Saving finalized recordings (including pre-roll) to device internal storage in a standard format (e.g., AAC or WAV).
- Minimal main activity to start/stop the background service.

Excluded (explicitly out-of-scope):

- Integration with the main Watnu app (tasks, sync, etc.).
- Transcription or NLP of the audio.
- Complex UI beyond the basic toggle.
- iOS implementation.

## High-level Architecture

Components:

- `MainActivity` — minimal UI with a single toggle button: "Start Listening" / "Stop Listening".
- `ListenerForegroundService` — foreground service that manages audio capture, buffering, detection, and file finalization.
- `AudioBufferManager` — manages two circular buffers in memory for trigger and content audio.
- `KnockDetector` — analyzes audio from the trigger buffer (CWT + simple classifier) to detect knocks.

Data flow (summary):

1. User starts the service via `MainActivity`.
2. `ListenerForegroundService` starts, acquires a wake lock, and displays a notification.
3. `AudioBufferManager` records from the microphone, writing to both ring buffers.
4. `KnockDetector` analyzes the trigger buffer and signals single/double knock events.
5. On single knock: copy content buffer to a temporary file and begin appending live audio.
6. On double knock: finalize and move the temporary file to `WatnuRecordings/` in internal storage; return service to standby.

## Success Criteria

- Background operation without OS termination for 8 hours (prototype-level expectation).
- Correct detection of deliberate knuckle-knock in a quiet environment.
- Saved audio file contains audio from before the trigger (pre-roll).
- Double-knock stops recording reliably; recordings auto-stop after a maximum duration (e.g., 5 minutes) if no double-knock is received.
- Vibration feedback for start/stop actions.

## Deliverables

1. APK of the functional prototype.
2. Source code.
3. Short test report documenting detection performance, battery impact, and common false positives/negatives.