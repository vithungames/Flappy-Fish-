package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.PI
import kotlin.math.sin

/**
 * Ultra-low-latency 8-bit sound synthesizer and haptic engine for Flappy Bird.
 * Uses MODE_STATIC AudioTracks with pre-generated PCM waveforms for zero-lag instant playback.
 */
class SoundManager(private val context: Context) {

    var soundEnabled: Boolean = true
    var vibrateEnabled: Boolean = true

    private val sampleRate = 44100
    private var trackFlap: AudioTrack? = null
    private var trackPoint: AudioTrack? = null
    private var trackHit: AudioTrack? = null
    private var trackDie: AudioTrack? = null
    private var trackCoin: AudioTrack? = null
    private var trackMedal: AudioTrack? = null

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        try {
            trackFlap = createStaticTrack(generateFlapPcm())
            trackPoint = createStaticTrack(generatePointPcm())
            trackHit = createStaticTrack(generateHitPcm())
            trackDie = createStaticTrack(generateDiePcm())
            trackCoin = createStaticTrack(generateCoinPcm())
            trackMedal = createStaticTrack(generateMedalPcm())
        } catch (_: Exception) {
            // AudioTrack init fallback
        }
    }

    private fun createStaticTrack(pcmData: ShortArray): AudioTrack? {
        return try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build()

            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(pcmData.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcmData, 0, pcmData.size)
            track
        } catch (_: Exception) {
            null
        }
    }

    fun playFlap() {
        if (soundEnabled) playTrack(trackFlap)
        vibrate(18, 90)
    }

    fun playPoint() {
        if (soundEnabled) playTrack(trackPoint)
        vibrate(35, 160)
    }

    fun playHit() {
        if (soundEnabled) playTrack(trackHit)
        vibrate(120, 255)
    }

    fun playDie() {
        if (soundEnabled) playTrack(trackDie)
        vibrate(80, 180)
    }

    fun playCoin() {
        if (soundEnabled) playTrack(trackCoin)
        vibrate(25, 140)
    }

    fun playMedal() {
        if (soundEnabled) playTrack(trackMedal)
        vibrate(60, 200)
    }

    private fun playTrack(track: AudioTrack?) {
        try {
            track?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.reloadStaticData()
                it.play()
            }
        } catch (_: Exception) {}
    }

    private fun vibrate(durationMs: Long, amplitude: Int = 120) {
        if (!vibrateEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        durationMs,
                        amplitude.coerceIn(1, 255)
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    // --- Procedural PCM Waveform Generators ---

    private fun generateFlapPcm(): ShortArray {
        // Quick upward chirp swoosh: 400Hz up to 750Hz in 80ms
        val duration = 0.08
        val numSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 400.0 + progress * 350.0
            phase += 2.0 * PI * freq / sampleRate
            val envelope = (1.0 - progress) * (1.0 - (1.0 - progress) * (1.0 - progress))
            val wave = sin(phase) * 0.75 + (if (sin(phase) > 0) 0.15 else -0.15)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.7).toInt().toShort()
        }
        return samples
    }

    private fun generatePointPcm(): ShortArray {
        // Iconic 2-tone arcade score chime: 1318 Hz (E6) then 1760 Hz (A6)
        val duration = 0.16
        val numSamples = (sampleRate * duration).toInt()
        val half = numSamples / 2
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val freq = if (i < half) 1318.51 else 1760.0
            phase += 2.0 * PI * freq / sampleRate
            val localProgress = if (i < half) i.toDouble() / half else (i - half).toDouble() / half
            val envelope = (1.0 - localProgress * 0.7)
            val wave = sin(phase)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.65).toInt().toShort()
        }
        return samples
    }

    private fun generateHitPcm(): ShortArray {
        // Retro crunchy impact sound: noise burst + 180Hz square wave
        val duration = 0.12
        val numSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            phase += 2.0 * PI * 180.0 / sampleRate
            val noise = (Math.random() * 2.0 - 1.0) * 0.6
            val tone = if (sin(phase) > 0) 0.4 else -0.4
            val envelope = (1.0 - progress) * (1.0 - progress)
            samples[i] = ((noise + tone) * envelope * Short.MAX_VALUE * 0.85).toInt().toShort()
        }
        return samples
    }

    private fun generateDiePcm(): ShortArray {
        // Falling slide tone: 550Hz down to 180Hz in 250ms
        val duration = 0.25
        val numSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 550.0 - progress * 370.0
            phase += 2.0 * PI * freq / sampleRate
            val envelope = 1.0 - progress
            val wave = if (sin(phase) > 0) 0.5 else -0.5
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.6).toInt().toShort()
        }
        return samples
    }

    private fun generateCoinPcm(): ShortArray {
        // High sparkling ping: 2093 Hz (C7) with rapid vibrato
        val duration = 0.14
        val numSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 2093.0 + sin(i.toDouble() * 0.05) * 80.0
            phase += 2.0 * PI * freq / sampleRate
            val envelope = (1.0 - progress) * (1.0 - progress * 0.5)
            val wave = sin(phase) * 0.7 + sin(phase * 2) * 0.3
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.65).toInt().toShort()
        }
        return samples
    }

    private fun generateMedalPcm(): ShortArray {
        // 4-note victory flourish arpeggio (C5 -> E5 -> G5 -> C6)
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        val noteDuration = 0.07
        val totalDuration = noteDuration * notes.size
        val numSamples = (sampleRate * totalDuration).toInt()
        val samples = ShortArray(numSamples)
        var currentPhase = 0.0
        for (i in 0 until numSamples) {
            val noteIndex = ((i.toDouble() / numSamples) * notes.size).toInt().coerceIn(0, notes.size - 1)
            val freq = notes[noteIndex]
            currentPhase += 2.0 * PI * freq / sampleRate
            val localProgress = ((i % (numSamples / notes.size)).toDouble()) / (numSamples / notes.size)
            val envelope = 1.0 - localProgress * 0.4
            val wave = sin(currentPhase) * 0.75 + (if (sin(currentPhase) > 0) 0.15 else -0.15)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.7).toInt().toShort()
        }
        return samples
    }

    fun release() {
        listOf(trackFlap, trackPoint, trackHit, trackDie, trackCoin, trackMedal).forEach {
            try {
                it?.stop()
                it?.release()
            } catch (_: Exception) {}
        }
    }
}
