package furhatos.app.openaichat.flow.main

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.Mixer
import javax.sound.sampled.TargetDataLine
import kotlin.math.abs

fun findMixerByName(partialName: String): Mixer.Info? =
    AudioSystem.getMixerInfo().firstOrNull {
        it.name.contains(partialName, ignoreCase = true) &&
                it.description.contains("Direct Audio Device")
    }

fun main() {
    val boyaInfo = findMixerByName("Boya") ?: run {
        println("Couldn't find the Boya receiver — check the exact name from the diagnostic above.")
        return
    }

    val sampleRate = 48000f // adjust to whatever the diagnostic actually shows
    val format = AudioFormat(sampleRate, 16, 2, true, false) // stereo, 16-bit
    val mixer = AudioSystem.getMixer(boyaInfo)
    val info = DataLine.Info(TargetDataLine::class.java, format)

    if (!mixer.isLineSupported(info)) {
        println("Format not supported — check the exact sample rate from the diagnostic.")
        return
    }

    val line = mixer.getLine(info) as TargetDataLine
    line.open(format)
    line.start()

    println("Recording 5 seconds... Person A (mic 1) then Person B (mic 2).")

    val bytesPerSecond = (sampleRate * 2 * 2).toInt() // 2 channels * 2 bytes
    val bufferSize = bytesPerSecond * 5
    val buffer = ByteArray(bufferSize)

    var totalRead = 0
    while (totalRead < bufferSize) {
        val read = line.read(buffer, totalRead, bufferSize - totalRead)
        if (read <= 0) break
        totalRead += read
    }
    line.stop()
    line.close()

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

    println("Left (Mic 1) average level: ${leftSum.toDouble() / frameCount}")
    println("Right (Mic 2) average level: ${rightSum.toDouble() / frameCount}")
}