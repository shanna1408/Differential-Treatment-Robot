package furhatos.app.openaichat.flow.chatbot

import furhatos.app.openaichat.flow.*
import furhatos.app.openaichat.flow.main.LastSpokenTracker
import furhatos.app.openaichat.flow.main.SpeakerSilentEvent
import furhatos.flow.kotlin.*
import furhatos.app.openaichat.flow.main.SpeakerTracker
import furhatos.app.openaichat.flow.main.Tone
import furhatos.app.openaichat.flow.main.respondBasedOnSpeaker
import furhatos.app.openaichat.flow.main.sayWithTone
import furhatos.flow.kotlin.furhat
import furhatos.app.openaichat.setting.hostRobot

val Taboo = state(Parent) {

    onEntry {
        delay(2000)
        Furhat.dialogHistory.clear()
        furhat.sayWithTone(Tone.ENTHUSIASTIC, "Hey, I'm ${hostRobot.name}.")
//        furhat.sayWithTone(Tone.ENTHUSIASTIC, currentTask.introduction)

        LastSpokenTracker.start(
            silentEvent = { person, options ->
                raise(SpeakerSilentEvent(person, options))
            },
        )

        LastSpokenTracker.recordSpoke("Person A")
        LastSpokenTracker.recordSpoke("Person B")
        reentry()
    }

    onEvent<SpeakerSilentEvent> { event ->
        furhat.attend(event.person.location)
        furhat.sayWithTone(
            Tone.HAPPY,
            *event.options.toTypedArray()
        )
        reentry()
    }

    onReentry {
        furhat.listen()
    }

    // Keywords that indicate a proper response without backchannels is likely to be needed.
    onResponse("Who","What","Where","When","Why","How","Eve","Yes", "That's right", "You got it", "That's it", "Correct", "Yup", "Yep") {
        furhat.gesture(GazeAversion(2.0))

        var speaker = SpeakerTracker.getDominantSpeaker(5000)
        LastSpokenTracker.recordSpoke(speaker)
        val text = it.text

        println("\n\n*******\nHeard Speech - No Backchannel")
        println("[Recognized Speech] Speaker: $speaker | Said: \"$text\"")
        respondBasedOnSpeaker(text, speaker)

        reentry()
    }

    onResponse {
        furhat.gesture(GazeAversion(2.0))
        var speaker = SpeakerTracker.getDominantSpeaker(5000)
        LastSpokenTracker.recordSpoke(speaker)
        val text = it.text

        println("\n\n*******\nHeard Speech")
        println("[Recognized Speech] Speaker: $speaker | Said: \"$text\"")
        respondBasedOnSpeaker(text, speaker, true)
        reentry()
    }

    onNoResponse {
        reentry()
    }
}