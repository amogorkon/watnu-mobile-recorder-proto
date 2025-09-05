# Spec 03 — Service Implementation

This spec covers the Android implementation details for `ListenerForegroundService`, including permissions, lifecycle, audio capture configuration, threading model, and robustness considerations.

## Responsibilities

- Host audio capture (`AudioRecord`) and write to `AudioBufferManager`.
- Provide `KnockDetector` with access to trigger buffer data.
- Manage `RecordingSession` lifecycle (create temp file, append live audio, finalize and move to storage).
- Maintain a persistent foreground notification and acquire/release wake locks as needed.

## Permissions

- `RECORD_AUDIO` — runtime permission for microphone access.
- `FOREGROUND_SERVICE` — declare for the foreground service.
- `WRITE_EXTERNAL_STORAGE` / scoped storage — prefer internal app storage; for external storage on older OS versions, request as needed.

## Audio Capture (AudioRecord)

- Sample rate: default 8000 Hz; implement a fallback negotiation (e.g., try 8000, then 11025, then 16000).
- Channel config: `CHANNEL_IN_MONO`.
- Audio format: `ENCODING_PCM_16BIT`.
- Buffer sizes: allocate a circular memory buffer sized for the `ContentBuffer` capacity (sampleRate * preRollSeconds * 2 bytes) plus a small margin for the `TriggerBuffer`.
- Use a dedicated audio thread to read AudioRecord into the buffers and publish slices to `KnockDetector` (e.g., using a lock-free queue or synchronized ring buffer).

## Threading Model

- Audio Thread: reads from `AudioRecord`, writes to both ring buffers at real-time priority where permitted.
- Detection Thread: processes small slices from the `TriggerBuffer` and runs the `KnockDetector` pipeline.
- IO Thread: handles tempfile writes and finalization (encoding to AAC if needed) and file moves.
- Main/UI Thread: handles user interactions and toggling service state.

Communication:

- Use lightweight thread-safe constructs (e.g., `AtomicInteger`/`AtomicBoolean`, `ReentrantLock` only where necessary).

## Foreground Notification

- Show a persistent notification while the service runs: title `Watnu Listener`, text `Audio buffer active`.
- Provide a quick action to open the `MainActivity` and to stop the service.

## Wake Locks and Power Management

- Acquire a partial wake lock (CPU) while service is active to avoid the device sleeping and starving the audio thread.
- Release wake lock when the service is stopped.
- Monitor Doze-mode related restrictions — accept that long-term background running is best-effort for the prototype.

## Recording Lifecycle

1. Start service: acquire wake lock, start audio thread, start detector thread.
2. Single knock: create `RecordingSession`, copy content buffer into a temp file (raw PCM or container), begin appending live frames.
3. Double knock or max duration: signal IO thread to finalize (close file, transcode/encode if needed, write metadata), move to `WatnuRecordings/`.
4. Return to standby with ring buffers maintained.

## File IO & Encoding

- Prefer writing PCM to a temp file and encode to AAC on finalization using `MediaCodec` or `MediaMuxer` to minimise real-time CPU use.
- Keep writes buffered and minimize main-thread IO.

## Error Handling

- If audio capture fails (e.g., permission revoked), stop service and surface an actionable notification.
- If disk write fails, attempt retries and if unrecoverable, delete temp file and log error in a sidecar for diagnostics.

## Diagnostics & Telemetry (local)

- Maintain a local log file or in-memory ring of recent `KnockEvent`s and system events for debugging.
- Optionally surface a small debug screen in `MainActivity` showing detection counts, CPU usage, and buffer fill levels.

## API / Interfaces (Kotlin sketch)

- `AudioBufferManager.start()` / `.stop()`
- `KnockDetector.start()` / `.stop()`
- `ListenerForegroundService` exposes a bind or local broadcast for `MainActivity` to query status.

## Security & Privacy

- Store recordings under internal app storage by default; avoid unnecessary world-readable locations.
- Respect user privacy; keep no automatic uploads in prototype.

---

Reference: aligned with `spec00` and `spec02`.
