package com.artmondo.boxters.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

class GameAudioManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private val failSoundIds = mutableListOf<Int>()
    private val clapSoundIds = mutableListOf<Int>()
    private var victorySoundId: Int = 0
    private var initialized = false
    var enabled = true
        private set

    private val letterFreqs: Map<Char, Float> = buildFrequencyMap()

    // Pre-generated tone buffers
    private val toneSoundIds = mutableMapOf<Char, Int>()
    private var errorSoundId: Int = 0

    private val sampleRate = 22050

    suspend fun init() = withContext(Dispatchers.IO) {
        if (initialized) return@withContext

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(attrs)
            .build()

        // Load SFX files
        val sp = soundPool ?: return@withContext
        try {
            val sfxFiles = listOf("sfx/fails01.mp3", "sfx/fails02.mp3", "sfx/fails03.mp3")
            for (file in sfxFiles) {
                val fd = context.assets.openFd(file)
                failSoundIds.add(sp.load(fd, 1))
                fd.close()
            }
            val clapFiles = listOf("sfx/clapping01.mp3", "sfx/clapping02.mp3", "sfx/clapping03.mp3")
            for (file in clapFiles) {
                val fd = context.assets.openFd(file)
                clapSoundIds.add(sp.load(fd, 1))
                fd.close()
            }
            val victoryFd = context.assets.openFd("sfx/victory.mp3")
            victorySoundId = sp.load(victoryFd, 1)
            victoryFd.close()
        } catch (e: Exception) {
            // SFX loading failed, continue without
        }

        // Pre-generate tone buffers
        for (letter in 'A'..'Z') {
            val freq = letterFreqs[letter] ?: 440f
            val buffer = generateToneBuffer(freq, 0.12f, WaveType.SINE)
            toneSoundIds[letter] = loadPcmToSoundPool(sp, buffer)
        }

        // Error tone
        val errorBuffer = generateToneBuffer(150f, 0.2f, WaveType.SQUARE)
        errorSoundId = loadPcmToSoundPool(sp, errorBuffer)

        initialized = true
    }

    private fun loadPcmToSoundPool(sp: SoundPool, pcm: ShortArray): Int {
        // Write PCM to a temporary WAV file for SoundPool
        val byteBuffer = pcmToWavBytes(pcm, sampleRate)
        val tempFile = java.io.File.createTempFile("tone_", ".wav", context.cacheDir)
        tempFile.writeBytes(byteBuffer)
        val id = sp.load(tempFile.absolutePath, 1)
        tempFile.deleteOnExit()
        return id
    }

    fun playTone(letter: Char) {
        if (!enabled || !initialized) return
        val volume = 0.3f
        val id = toneSoundIds[letter.uppercaseChar()] ?: return
        soundPool?.play(id, volume, volume, 1, 0, 1.0f)
    }

    fun playWordChord(word: String) {
        if (!enabled || !initialized) return
        val uniqueLetters = word.uppercase().toSet().take(4)
        val volume = 0.2f
        for (letter in uniqueLetters) {
            val freq = letterFreqs[letter] ?: continue
            val buffer = generateToneBuffer(freq, 0.4f, WaveType.TRIANGLE)
            playPcmBuffer(buffer, volume)
        }
    }

    fun playError() {
        if (!enabled || !initialized) return
        soundPool?.play(errorSoundId, 0.3f, 0.3f, 1, 0, 1.0f)
    }

    fun playFail() {
        if (!enabled || !initialized || failSoundIds.isEmpty()) return
        val id = failSoundIds.random()
        soundPool?.play(id, 0.3f, 0.3f, 1, 0, 1.0f)
    }

    fun playVictory() {
        if (!enabled || !initialized) return
        // Play clapping
        if (clapSoundIds.isNotEmpty()) {
            val id = clapSoundIds.random()
            soundPool?.play(id, 0.3f, 0.3f, 1, 0, 1.0f)
        }
        // Play synthesized fanfare
        playVictoryFanfare()
    }

    fun playUltimateVictory() {
        playVictory()
        if (victorySoundId != 0) {
            soundPool?.play(victorySoundId, 0.3f, 0.3f, 1, 0, 1.0f)
        }
    }

    fun toggle() {
        enabled = !enabled
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        initialized = false
    }

    private fun buildFrequencyMap(): Map<Char, Float> {
        val pentatonic = floatArrayOf(261.63f, 293.66f, 329.63f, 392.00f, 440.00f)
        val freqs = mutableMapOf<Char, Float>()
        for (i in 0 until 26) {
            val octave = i / 5
            val note = i % 5
            freqs['A' + i] = pentatonic[note] * 2f.pow(octave * 0.5f)
        }
        return freqs
    }

    private fun generateToneBuffer(freq: Float, durationSec: Float, waveType: WaveType): ShortArray {
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val envelope = (0.2f * exp(-t * 10f)).coerceAtLeast(0f)
            val wave = when (waveType) {
                WaveType.SINE -> sin(2.0 * PI * freq * t).toFloat()
                WaveType.TRIANGLE -> {
                    val phase = (freq * t) % 1.0f
                    if (phase < 0.5f) 4f * phase - 1f else 3f - 4f * phase
                }
                WaveType.SQUARE -> if (sin(2.0 * PI * freq * t) >= 0) 1f else -1f
            }
            samples[i] = (wave * envelope * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun playPcmBuffer(samples: ShortArray, volume: Float) {
        Thread {
            try {
                val bufferSize = samples.size * 2
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(sampleRate)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                audioTrack.write(samples, 0, samples.size)
                audioTrack.setVolume(volume)
                audioTrack.play()
                Thread.sleep((samples.size * 1000L / sampleRate) + 50)
                audioTrack.release()
            } catch (e: Exception) {
                // Ignore audio errors
            }
        }.start()
    }

    private fun playVictoryFanfare() {
        Thread {
            try {
                val durationSec = 5.5f
                val numSamples = (sampleRate * durationSec).toInt()
                val mixBuffer = FloatArray(numSamples)

                data class Note(val freq: Float, val time: Float, val dur: Float, val volume: Float = 0.2f)

                val fanfare = listOf(
                    Note(523.25f, 0f, 0.20f), Note(659.25f, 0.20f, 0.20f),
                    Note(783.99f, 0.40f, 0.20f), Note(1046.50f, 0.60f, 0.50f)
                )
                val melody = listOf(
                    Note(783.99f, 1.3f, 0.20f), Note(880.00f, 1.5f, 0.20f),
                    Note(1046.50f, 1.7f, 0.30f), Note(987.77f, 2.1f, 0.20f),
                    Note(1046.50f, 2.3f, 0.20f), Note(1174.66f, 2.5f, 0.20f),
                    Note(1318.51f, 2.8f, 0.70f)
                )
                val flourish = listOf(
                    Note(1318.51f, 3.8f, 0.15f), Note(1174.66f, 3.95f, 0.15f),
                    Note(1318.51f, 4.1f, 0.15f), Note(1567.98f, 4.3f, 0.90f)
                )

                for (note in fanfare + melody + flourish) {
                    val startSample = (note.time * sampleRate).toInt()
                    val endSample = minOf(((note.time + note.dur + 0.4f) * sampleRate).toInt(), numSamples)
                    for (i in startSample until endSample) {
                        val t = (i - startSample).toFloat() / sampleRate
                        val attack = if (t < 0.03f) t / 0.03f else 1f
                        val decay = exp(-t * 3f).coerceAtLeast(0.001f)
                        val envelope = attack * decay * note.volume
                        mixBuffer[i] += (sin(2.0 * PI * note.freq * t).toFloat() * envelope)
                    }
                }

                // Harmony pad
                val chords = listOf(
                    Triple(listOf(261.63f, 329.63f, 392.00f), 0f, 2.0f),
                    Triple(listOf(349.23f, 440.00f, 523.25f), 2.0f, 2.0f),
                    Triple(listOf(261.63f, 329.63f, 392.00f), 4.0f, 2.0f)
                )
                for ((notes, time, dur) in chords) {
                    val startSample = (time * sampleRate).toInt()
                    val endSample = minOf(((time + dur) * sampleRate).toInt(), numSamples)
                    for (freq in notes) {
                        for (i in startSample until endSample) {
                            val t = (i - startSample).toFloat() / sampleRate
                            val tNorm = t / dur
                            val envelope = when {
                                tNorm < 0.05f -> tNorm / 0.05f
                                tNorm > 0.8f -> (1f - tNorm) / 0.2f
                                else -> 1f
                            } * 0.07f
                            val phase = (freq * t) % 1.0f
                            val wave = if (phase < 0.5f) 4f * phase - 1f else 3f - 4f * phase
                            mixBuffer[i] += wave * envelope
                        }
                    }
                }

                // Convert to PCM
                val pcm = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val clamped = mixBuffer[i].coerceIn(-1f, 1f)
                    pcm[i] = (clamped * Short.MAX_VALUE * 0.5f).toInt().toShort()
                }

                playPcmBuffer(pcm, 0.3f)
            } catch (e: Exception) {
                // Ignore
            }
        }.start()
    }

    private enum class WaveType { SINE, TRIANGLE, SQUARE }

    companion object {
        fun pcmToWavBytes(pcm: ShortArray, sampleRate: Int): ByteArray {
            val dataSize = pcm.size * 2
            val fileSize = 44 + dataSize
            val header = ByteArray(44)

            // RIFF header
            header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
            writeInt(header, 4, fileSize - 8)
            header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()

            // fmt chunk
            header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
            writeInt(header, 16, 16) // chunk size
            writeShort(header, 20, 1) // PCM format
            writeShort(header, 22, 1) // mono
            writeInt(header, 24, sampleRate)
            writeInt(header, 28, sampleRate * 2) // byte rate
            writeShort(header, 32, 2) // block align
            writeShort(header, 34, 16) // bits per sample

            // data chunk
            header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
            writeInt(header, 40, dataSize)

            val result = ByteArray(fileSize)
            header.copyInto(result)
            for (i in pcm.indices) {
                result[44 + i * 2] = (pcm[i].toInt() and 0xFF).toByte()
                result[44 + i * 2 + 1] = (pcm[i].toInt() shr 8 and 0xFF).toByte()
            }
            return result
        }

        private fun writeInt(buf: ByteArray, offset: Int, value: Int) {
            buf[offset] = (value and 0xFF).toByte()
            buf[offset + 1] = (value shr 8 and 0xFF).toByte()
            buf[offset + 2] = (value shr 16 and 0xFF).toByte()
            buf[offset + 3] = (value shr 24 and 0xFF).toByte()
        }

        private fun writeShort(buf: ByteArray, offset: Int, value: Int) {
            buf[offset] = (value and 0xFF).toByte()
            buf[offset + 1] = (value shr 8 and 0xFF).toByte()
        }
    }
}
