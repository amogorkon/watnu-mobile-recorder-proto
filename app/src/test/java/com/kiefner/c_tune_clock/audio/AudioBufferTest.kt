package com.kiefner.c_tune_clock.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class AudioBufferTest {
    @Test
    fun testWriteAndRead_NoWrap() {
        val buf = AudioBuffer("trigger", sampleRate = 8, capacitySeconds = 2)
        // capacitySamples = 16
        val data = ShortArray(8) { i -> (i + 1).toShort() }
        buf.write(data)
        // read last 1 second (8 samples) from startSec=1 to endSec=2
        val out = buf.readRange(1,2)
        assertArrayEquals(data, out)
    }

    @Test
    fun testWriteAndRead_WrapAround() {
        val buf = AudioBuffer("trigger", sampleRate = 8, capacitySeconds = 2)
        val first = ShortArray(12) { i -> (i + 1).toShort() }
        buf.write(first)
        val second = ShortArray(8) { i -> (i + 101).toShort() }
        buf.write(second)
        // After writes, writeIndex = (0 + 12 + 8) % 16 = 4
        // The buffer contains the last 16 samples: [ (5..16), (101..108) ]
        // Reading last 2 seconds (full buffer): startSec=0,endSec=2
        val out = buf.readRange(0,2)
    val expected = ShortArray(16)
    // chronological order from oldest index (writeIndex=4): [5..12, 101..108]
    var idx = 0
    for (v in 5..12) expected[idx++] = v.toShort()
    for (v in 101..108) expected[idx++] = v.toShort()
        assertArrayEquals(expected, out)
    }
}
