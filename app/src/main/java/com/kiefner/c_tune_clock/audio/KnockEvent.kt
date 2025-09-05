package com.kiefner.c_tune_clock.audio

data class KnockEvent(
    val timestamp: String, // ISO-8601 UTC
    val confidence: Float,
    val type: String // "single" | "double"
)
