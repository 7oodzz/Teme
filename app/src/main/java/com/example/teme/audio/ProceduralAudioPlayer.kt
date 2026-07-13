package com.example.teme.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class ProceduralAudioPlayer {
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var job: Job? = null
    private val sampleRate = 44100

    fun startPlaying() {
        if (isPlaying) return
        isPlaying = true

        val minSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minSize,
            AudioTrack.MODE_STREAM
        ).apply { play() }

        job = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ShortArray(minSize)
            var phase = 0.0
            
            // Lo-fi Hum / Rain simulation parameters
            val humFrequency = 60.0 // Low hum
            val volume = 2000 // Subtle volume

            while (isActive && isPlaying) {
                for (i in buffer.indices) {
                    // Combine low frequency hum with brown noise for "rain" texture
                    val hum = Math.sin(phase) * volume
                    val noise = (Random.nextFloat() * 2 - 1) * (volume / 2) // Brownish noise approximation
                    
                    buffer[i] = (hum + noise).toInt().toShort()
                    
                    phase += 2 * Math.PI * humFrequency / sampleRate
                    if (phase > 2 * Math.PI) {
                        phase -= 2 * Math.PI
                    }
                }
                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stopPlaying() {
        isPlaying = false
        job?.cancel()
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
}
