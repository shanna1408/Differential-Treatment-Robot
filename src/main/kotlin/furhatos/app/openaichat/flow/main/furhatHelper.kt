package furhatos.app.openaichat.flow.main
import furhatos.app.openaichat.flow.chatbot
import furhatos.app.openaichat.flow.persons
import furhatos.app.openaichat.flow.preferredPerson
import furhatos.app.openaichat.setting.hostRobot
import furhatos.flow.kotlin.*        // gives you say, gesture, state, and the Flow DSL
import furhatos.flow.kotlin.voice.AzureVoice
import furhatos.gestures.Gestures    // built-in gestures: Gestures.Smile, Gestures.BigSmile, Gestures.BrowFrown, etc.
import furhatos.gestures.Gesture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import kotlin.random.Random

enum class Tone {
    HAPPY,
    ENTHUSIASTIC,
    SAD,
    ANNOYED,
    CONCERNED,
    NEUTRAL
}

data class ToneSettings(
    val gesture: Gesture,
    val azureStyle: String,
    val styleDegree: Double,
)

const val gestureStrength = 2.0
const val gestureDuration = 2.5

val toneSettings = mapOf(
    Tone.HAPPY        to ToneSettings(Gestures.Smile(gestureStrength, gestureDuration),     AzureVoice.Style.CHEERFUL,   styleDegree = 1.0),
    Tone.ENTHUSIASTIC to ToneSettings(Gestures.BigSmile(gestureStrength, gestureDuration),  AzureVoice.Style.EXCITED,    styleDegree = 1.0),
    Tone.SAD          to ToneSettings(Gestures.BrowFrown(gestureStrength, gestureDuration), AzureVoice.Style.SAD,        styleDegree = 1.0),
    Tone.ANNOYED      to ToneSettings(Gestures.ExpressDisgust(gestureStrength, gestureDuration), AzureVoice.Style.UNFRIENDLY, styleDegree = 2.0),
    Tone.CONCERNED    to ToneSettings(Gestures.BrowFrown(gestureStrength, gestureDuration), AzureVoice.Style.EMPATHETIC, styleDegree = 1.0),
    Tone.NEUTRAL      to ToneSettings(Gestures.Blink(gestureStrength, gestureDuration),     AzureVoice.Style.CALM,    styleDegree = 1.0)
)

fun Furhat.sayWithTone(tone: Tone, vararg options: String) {
    println(tone)
    val settings = toneSettings[tone] ?: toneSettings[Tone.NEUTRAL]!!
    this.gesture(settings.gesture, async = true)

    val azureVoice = this.voice as? AzureVoice

    println("Options: ${options.contentToString()}")

    this.say {
        random {
            options.forEach { option ->
                // Apply style first, then wrap with prosody
                val styled = azureVoice?.style(option, settings.azureStyle) ?: option
                val voiced = azureVoice?.prosody(styled, rate=1.2) ?: styled
                +voiced
            }
        }
    }

}

fun Furhat.backchannel(tone: Tone, speaker: String){
    if (Random.nextDouble() < (persons[speaker]?.condition?.backchannelProb ?: 0.5)) {
        println("Backchanneling")

        this.gesture(Gestures.Nod, async = true)
        val settings = toneSettings[tone] ?: toneSettings[Tone.NEUTRAL]!!
        this.gesture(settings.gesture, async = true)
        val azureVoice = this.voice as? AzureVoice

        val backchannels = listOf<String>("yeah","uh-huh","hmm","mm-hmm","huh")
        this.say {
            random {
                backchannels.forEach { backchannel ->
                    // Apply style first, then wrap with prosody
                    val styled = azureVoice?.style(backchannel, settings.azureStyle) ?: backchannel
                    val voiced = azureVoice?.prosody(styled) ?: styled
                    +voiced
                }
            }
        }
    }
}

fun FlowControlRunner.respondBasedOnSpeaker(text: String, speaker: String, sayBackchannel: Boolean = false)
{
    // Async gaze handling
    CoroutineScope(Dispatchers.Default).launch {
        setGaze(speaker)
    }

    if (sayBackchannel) {
        // Possibly say only "Thanks" or "Okay" if speaker is non-preferred.
        if (Random.nextDouble() < (persons[speaker]?.condition?.nonResponseProb ?: 0.0)) {
            furhat.sayWithTone(persons[speaker]?.condition!!.tone, "Okay", "Thanks")
            return
        }
        furhat.backchannel(persons[speaker]?.condition!!.tone, speaker = speaker)
    }

    val response = call {
        chatbot.getResponse(text, persons[speaker]?.condition!!.status,
            persons[speaker]!!.condition!!.personality)
    } as String

    println("Response: $response")
    furhat.sayWithTone(persons[speaker]?.condition!!.tone, response)
}

fun FlowControlRunner.setGaze(speaker: String)
{
    var now = System.currentTimeMillis()

    //If the robot has been staring at the same person for 20 seconds or more
    // Look at the center
    if ((now-hostRobot.lastHeadTurn) >= 20*1000 && (speaker == hostRobot.gazeTarget)) {
            furhat.attend(persons["Center"]!!.location)
        hostRobot.lastHeadTurn = System.currentTimeMillis()
        hostRobot.gazeTarget = "Center"
        Thread.sleep(2000)
        return
    }

    // Everytime someone speaks, update their counter
    println(speaker)
    persons[speaker]!!.gazeCounter++
    println("${persons[speaker]?.status} Speaker $speaker Speech Count: ${persons[speaker]?.gazeCounter}/${persons[speaker]?.condition?.gazeMax}")

    // If the speaker counter == condition.gazeCount for the speaker, gaze at the speaker
    // Otherwise: If already gazing at preferred person, continue to do so
    // If gazing at non preferred speaker, wait 3 seconds then gaze at preferred person.
    if (persons[speaker]!!.gazeCounter == (persons[speaker]?.condition?.gazeMax)) {
        if (speaker != hostRobot.gazeTarget) {
            hostRobot.lastHeadTurn = System.currentTimeMillis()
            hostRobot.gazeTarget = speaker
        }
        println("Turn to look at ${speaker}")
        furhat.attend(persons[speaker]!!.location)
        persons[speaker]!!.gazeCounter = 0
    } else if (persons[speaker]!!.gazeCounter < (persons[speaker]!!.condition!!.gazeMax)) {
        thread(isDaemon = true) {
            Thread.sleep(3000)
            println("Turn to look at ${preferredPerson}")
            furhat.attend(persons[preferredPerson]!!.location)
            hostRobot.gazeTarget = preferredPerson
            hostRobot.lastHeadTurn = System.currentTimeMillis()
        }
    }
    Thread.sleep(2000)
}