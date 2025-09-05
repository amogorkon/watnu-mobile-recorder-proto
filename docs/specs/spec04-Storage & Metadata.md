# Spec 04 — Storage & Metadata

This spec defines how recordings and metadata are persisted on the device.

## Storage Location

- Default directory: internal app storage under `WatnuRecordings/` (app-specific files directory).
- For debugging builds, optionally expose an external folder under `Documents/WatnuRecordings/` with user consent.

## File Naming

- Use timestamp-based filenames to avoid collisions and for easy sorting:

  `recording-YYYYMMDD-HHMMSS.<ext>` e.g., `recording-20250904-123045.aac`

- Temporary files while recording: `recording-SESSIONID.tmp` (store in app cache or temp dir).

## Formats

- Preferred: `aac` with `ADTS` container for space-efficient storage.
- For debugging: raw `wav` with PCM16 payload.

## Sidecar Metadata

- For each finalized recording, create a JSON sidecar with the same basename and `.json` extension containing `RecordingArtifact` fields.

Example schema (sidecar):

```json
{
  "fileName": "recording-20250904-123045.aac",
  "filePath": "/data/data/.../WatnuRecordings/recording-20250904-123045.aac",
  "createdAt": "2025-09-04T12:30:45Z",
  "durationSeconds": 45,
  "preRollSeconds": 120,
  "sampleRate": 8000,
  "channels": 1,
  "detectedKnocks": [
    {"timestamp": "2025-09-04T12:29:05Z", "confidence": 0.87, "type": "single"},
    {"timestamp": "2025-09-04T12:30:30Z", "confidence": 0.92, "type": "double"}
  ]
}
```

## Retention & Cleanup

- Keep recordings indefinitely by default; provide a manual cleanup option in `MainActivity` during prototype.
- For automated cleanup during experiments, allow a TTL-based pruning (e.g., delete recordings older than N days) controlled by a setting.

## Atomicity & Crash Recovery

- Write to temp file first (`.tmp`). On successful finalization, move/rename atomically to final filename and write sidecar.
- On startup, scan for `.tmp` files and either resume finalization if possible or delete truncated files and log an incident.

## Indexing

- Maintain an in-memory index of recordings loaded from sidecars for quick listing in the UI.
- Optionally persist a compact index file (e.g., `index.json`) to speed startup for large numbers of recordings.

## Permissions & Privacy

- Store under internal app storage; no external uploads by default.

## Diagnostics

- For failed writes, create a diagnostic log entry; optionally write a `.error.json` sidecar explaining failure.

---

Reference: derived from `spec00` and `spec01` descriptions.
