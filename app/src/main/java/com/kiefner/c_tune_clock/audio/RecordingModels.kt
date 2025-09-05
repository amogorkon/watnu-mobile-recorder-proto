package com.kiefner.c_tune_clock.audio

data class RecordingArtifact(
    val fileName: String,
    val filePath: String,
    val createdAt: String,
    val durationSeconds: Int,
    val preRollSeconds: Int,
    val sampleRate: Int,
    val channels: Int,
    val bitDepth: Int,
    val detectedKnocks: List<KnockEvent>
)

data class RecordingSession(
    val sessionId: String,
    val startTimestamp: String,
    val tempFilePath: String,
    val format: String,
    val maxDurationSeconds: Int = 300,
    var isFinalized: Boolean = false
)

/**
 * Simple ring buffer for 16-bit PCM samples (signed shorts).
 * Not heavily optimized; intended for correctness and unit testing.
 */
class AudioBuffer(
    val id: String,
    val sampleRate: Int = 8000,
    val channels: Int = 1,
    val bitDepth: Int = 16,
    val capacitySeconds: Int
) {
    val capacitySamples: Int = sampleRate * capacitySeconds
    private val buffer: ShortArray = ShortArray(capacitySamples)
    var writeIndex: Int = 0

    @Synchronized
    fun write(data: ShortArray) {
        var offset = 0
        var remaining = data.size
        while (remaining > 0) {
            val toCopy = minOf(remaining, capacitySamples - writeIndex)
            System.arraycopy(data, offset, buffer, writeIndex, toCopy)
            writeIndex = (writeIndex + toCopy) % capacitySamples
            offset += toCopy
            remaining -= toCopy
        }
    }

    /**
     * Read samples from [startSec] (inclusive) to [endSec] (exclusive).
     * Returns a newly allocated ShortArray with the samples in chronological order.
     */
    @Synchronized
    fun readRange(startSec: Int, endSec: Int): ShortArray {
        val startSample = ((writeIndex - (capacitySeconds - startSec) * sampleRate) % capacitySamples + capacitySamples) % capacitySamples
        val endSample = ((writeIndex - (capacitySeconds - endSec) * sampleRate) % capacitySamples + capacitySamples) % capacitySamples
        val length = ((endSec - startSec) * sampleRate)
        val out = ShortArray(length)
        if (length == 0) return out
        if (startSample < endSample) {
            System.arraycopy(buffer, startSample, out, 0, length)
        } else {
            val tailLen = capacitySamples - startSample
            if (tailLen >= length) {
                System.arraycopy(buffer, startSample, out, 0, length)
            } else {
                System.arraycopy(buffer, startSample, out, 0, tailLen)
                System.arraycopy(buffer, 0, out, tailLen, length - tailLen)
            }
        }
        return out
    }
}
