package furhatos.app.openaichat.flow.chatbot

import furhatos.app.openaichat.flow.*
import furhatos.app.openaichat.flow.main.LastSpokenTracker
import furhatos.app.openaichat.flow.main.SpeakerSilentEvent
import furhatos.app.openaichat.flow.main.SpeakerTracker
import furhatos.app.openaichat.flow.main.Tone
import furhatos.app.openaichat.flow.main.respondBasedOnSpeaker
import furhatos.app.openaichat.flow.main.sayWithTone
import furhatos.app.openaichat.setting.hostRobot
import furhatos.flow.kotlin.*

val Island: State = state(Parent) {

//    var task = tasks["Island"]!!
//
//    onEntry {
//        delay(2000)
//        Furhat.dialogHistory.clear()
//        furhat.sayWithTone(Tone.ENTHUSIASTIC, "Hey, I'm ${hostRobot.name}.")
//        furhat.sayWithTone(Tone.ENTHUSIASTIC, task.introduction)
//
//        LastSpokenTracker.start(
//            silentEvent = { person, options ->
//                raise(SpeakerSilentEvent(person, options))
//            },
//        )
//
//        LastSpokenTracker.recordSpoke("Person A")
//        LastSpokenTracker.recordSpoke("Person B")
//        reentry()
//    }
//
//    onReentry {
//        furhat.listen(endSil = 2500)
//    }
//
//    onResponse {
//        furhat.gesture(GazeAversion(2.0))
//        var speaker = SpeakerTracker.getDominantSpeaker(5000)
//        LastSpokenTracker.recordSpoke(speaker)
//        val text = it.text
//
//        println("\n\n*******\nHeard Speech")
//        println("[Recognized Speech] Speaker: $speaker | Said: \"$text\"")
//        respondBasedOnSpeaker(text, speaker, true)
//        reentry()
//    }
//
//    onNoResponse {
//        reentry()
//    }
}