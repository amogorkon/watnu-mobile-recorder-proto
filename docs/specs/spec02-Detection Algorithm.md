# Spec 02 — Detection Algorithm

This spec describes the signal-processing and detection pipeline for knuckle-knock recognition. The design choices prioritise low CPU/battery usage while providing detectability of sharp impulse events.

## Goals

- Detect single knuckle knocks (start recording) and double knocks (stop recording) with acceptable true-positive rates in quiet environments.
- Minimise false positives from ambient sounds (doors, table taps) where feasible for a prototype.
- Run entirely on-device with modest CPU usage (suitable for foreground service use).

## Input

- PCM mono audio stream from `AudioRecord`.
- Preferred sample rate: 8000 Hz (configurable). 16-bit signed PCM.

## Processing Pipeline

1. Frame the audio stream into overlapping frames.

- Frame length: 256 samples (~32 ms @ 8 kHz).
- Hop size: 64 samples (~8 ms) for good temporal resolution.

2. Pre-emphasis & normalisation.

- Apply a small pre-emphasis filter (e.g., y[n] = x[n] - 0.97 * x[n-1]) to emphasise transient energy.
- Normalise frame energy to reduce sensitivity to absolute volume changes.

3. Continuous Wavelet Transform (CWT)

- Compute a short-time CWT for each frame or small block. Use a complex Morlet wavelet tuned to high-frequency bands where knocks have energy.
- Scale selection: focus on scales translating to ~1–4 kHz band at 8 kHz sampling.
- Compute magnitude (energy) across selected scales to produce a time-frequency energy vector.

4. Feature extraction

- Compute per-frame features: spectral energy in targeted band, spectral flatness, short-term energy, and kurtosis.
- Maintain a running baseline energy estimate (exponential moving average) to normalise features and adapt to slow background changes.

5. Impulse detection (thresholding + hysteresis)

- For each frame, compute a detection score = weighted sum of normalised spectral energy and kurtosis.
- Trigger when detection score exceeds an adaptive threshold: threshold = baseline * thresholdFactor (e.g., 4x). Use hysteresis to avoid rapid toggling.

6. Temporal filtering & debouncing

- Require the detection score to exceed threshold for at least N consecutive frames (e.g., 2 frames ~16 ms) to consider a candidate impulse.
- Enforce a minimum inter-impulse quiet window (e.g., 50 ms) to avoid detecting the same impulse multiple times.

7. Double-knock detection

- Keep a rolling buffer of detected single-knock timestamps.
- If two knocks occur within `doubleWindow` (e.g., 0.8 seconds), interpret as a double-knock and emit a `double` event.

## Calibration & Tuning

- Provide configuration for `sampleRate`, `frameLength`, `hopSize`, `thresholdFactor`, `doubleWindow`, and `preRollSeconds`.
- Optionally provide a short calibration mode where the user produces a few knocks and the detector sets an initial baseline and threshold multiplier.

## Performance & Complexity

- Use lightweight CWT approximations or bandpass filters if full CWT is too costly; a bank of 3–4 narrow bandpass IIR filters can approximate the same detection behaviour with lower CPU.
- Keep per-frame work minimal; aim for <10% CPU on a mid-range device while running as a foreground service.

## Outputs

- `KnockEvent` objects with `timestamp` and `confidence` for each detected single knock.
- Higher-level `single` and `double` events emitted to the service.

## Testing

- Unit tests for: frame extraction, pre-emphasis, baseline tracking, and detection score computation.
- Integration tests: run detector on recorded sample files containing knocks, door slams, speech, and background noise to measure precision/recall.

---

Reference: derived from `docs/spec proposal.md` and augmented with concrete parameter suggestions.
