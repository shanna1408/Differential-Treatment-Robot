package furhatos.app.openaichat.flow.main

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.Mixer
import javax.sound.sampled.TargetDataLine
import kotlin.concurrent.thread
import kotlin.math.abs

object SpeakerTracker {
    data class Sample(val timestamp: Long, val leftAvg: Double, val rightAvg: Double)

    @Volatile var isSomeoneSpeaking: Boolean = false


    private var previouslySpeaking: String? = null // null = silence
    private val history = mutableListOf<Sample>()
    private val historyLock = Object()
    private val threshold = 18.0
    @Volatile private var running = false

    var onSpeechStart: ((String) -> Unit)? = null

    fun start() {
        if (running) return
        running = true
        thread(isDaemon = true) { captureLoop() }
    }

    fun stop() { running = false }

    // Looks back over the last `windowMillis` to find who was dominant during that stretch
    fun getDominantSpeaker(windowMillis: Long = 5000): String {
        val cutoff = System.currentTimeMillis() - windowMillis
        val relevant = synchronized(historyLock) { history.filter { it.timestamp >= cutoff } }

        var aCount = 0
        var bCount = 0
        for (s in relevant) {
            if (s.leftAvg > threshold && s.leftAvg > s.rightAvg) aCount++
            else if (s.rightAvg > threshold && s.rightAvg > s.leftAvg) bCount++
        }

        println("[SpeakerTracker] Lookback window: ${relevant.size} samples | A votes: $aCount | B votes: $bCount")


        return when {
            aCount > bCount -> "Person A"
            bCount > aCount -> "Person B"
            else -> "Center"
        }
    }

    private fun findMixerByName(partialName: String): Mixer.Info? =
        AudioSystem.getMixerInfo().firstOrNull {
            it.name.contains(partialName, ignoreCase = true) &&
                    it.description.contains("Direct Audio Device")
        }

    private fun captureLoop() {
        val boyaInfo = findMixerByName("Boya") ?: run {
            println("Not found")
            return
        }

        val sampleRate = 48000f
        val format = AudioFormat(sampleRate, 16, 2, true, false)
        val mixer = AudioSystem.getMixer(boyaInfo)
        val info = DataLine.Info(TargetDataLine::class.java, format)
        if (!mixer.isLineSupported(info)) {
            println("[SpeakerTracker] Format not supported.")
            return
        }

        val line = mixer.getLine(info) as TargetDataLine
        line.open(format)
        line.start()

        val chunkMillis = 200
        val bytesPerChunk = (sampleRate * 2 * 2 * (chunkMillis / 1000.0)).toInt()
        val buffer = ByteArray(bytesPerChunk)

        while (running) {
            var totalRead = 0
            while (totalRead < buffer.size) {
                val read = line.read(buffer, totalRead, buffer.size - totalRead)
                if (read <= 0) break
                totalRead += read
            }
            if (totalRead <= 0) continue

            var leftSum = 0L; var rightSum = 0L; var frameCount = 0
            var i = 0
            while (i + 3 < totalRead) {
                val left = ((buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)).toShort()
                val right = ((buffer[i + 3].toInt() shl 8) or (buffer[i + 2].toInt() and 0xFF)).toShort()
                leftSum += abs(left.toInt())
                rightSum += abs(right.toInt())
                frameCount++
                i += 4
            }
            if (frameCount == 0) continue

            val leftAvg = leftSum.toDouble() / frameCount
            val rightAvg = rightSum.toDouble() / frameCount
            val now = System.currentTimeMillis()

            synchronized(historyLock) {
                history.add(Sample(now, leftAvg, rightAvg))
                history.removeAll { now - it.timestamp > 10000 } // keep last 10s only
            }

            val currentSpeaker = when {
                leftAvg > threshold && leftAvg > rightAvg -> "Person A"
                rightAvg > threshold && rightAvg > leftAvg -> "Person B"
                else -> null
            }

            isSomeoneSpeaking = currentSpeaker != null

            // Detect a transition from silence (or the other person) into this speaker
            if (currentSpeaker != null && currentSpeaker != previouslySpeaking) {
                onSpeechStart?.invoke(currentSpeaker)
            }
            previouslySpeaking = currentSpeaker

//            aCount > bCount -> "Person A"
//            bCount > aCount -> "Person B"
//            else -> "Center"
//            println("[SpeakerTracker] Left: %.2f | Right: %.2f".format(leftAvg, rightAvg))
        }

        line.stop()
        line.close()
    }
}