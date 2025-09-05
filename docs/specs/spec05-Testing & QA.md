# Spec 05 — Testing & QA

This spec outlines tests and validation strategies for the prototype, covering unit tests, integration tests, manual QA procedures, and metrics to measure detection performance and power impact.

## Unit Tests

- `AudioBufferManager`:
  - Test write/read correctness with and without wrap-around.
  - Verify capacity calculations and thread-safety under simulated concurrent writes.

- `KnockDetector` pipeline components:
  - Frames/hop extraction.
  - Pre-emphasis and normalisation.
  - Baseline tracking and adaptive threshold behaviour.
  - Detection score computation with synthetic impulse inputs.

- `RecordingSession`:
  - Ensure temp file creation and append/close behaviour.
  - Simulate IO errors and verify cleanup and diagnostic sidecars.

## Integration Tests

- Run the detector on a set of labelled audio files (knocks, double knocks, speech, door slams, background noise) to compute precision/recall.
- Test full-service flow on an emulator or test device: start service, inject audio (or perform physical knocks), ensure session files are created and finalized.

## Manual QA Procedures

- Quiet-room test: deliberate single knock to start and double knock to stop. Verify pre-roll content contains prior audio.
- Noisy-room test: evaluate false positive rate in a busier environment.
- Door slam test: attempt to reproduce door knocks and measure false positives.

## Metrics to Collect

- True positives, false positives, false negatives for single and double knocks.
- Time from knock detection to session creation (latency).
- Average CPU usage and battery drain while service runs for a prolonged period (e.g., 8 hours).
- Disk space usage per minute of recording (AAC vs WAV).

## Test Data & Harness

- Capture a library of test WAV files with labelled knock events to evaluate detector offline.
- Provide a small test harness app or instrumentation in `MainActivity` to play test files into the detector pipeline.

## Regression & Automation

- Add unit tests under `app/src/test/java` and run with Gradle test tasks.
- For detector regressions, include nightly runs of detection benchmarks if CI is available.

## Acceptance Criteria

- Detector precision > 0.8 and recall > 0.7 in quiet conditions (prototype targets; tuneable).
- Service runs for at least 8 hours on a typical device without being killed in prototype mode.

---

Reference: testing requirements derived from `spec00` success criteria.
