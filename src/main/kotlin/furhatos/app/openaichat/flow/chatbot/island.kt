package furhatos.app.openaichat.flow.chatbot

import furhatos.app.openaichat.flow.*
import furhatos.app.openaichat.setting.activate
import furhatos.app.openaichat.setting.Task
import furhatos.app.openaichat.setting.tasks
import furhatos.flow.kotlin.*

val Island = state(Parent) {

//    var currentTask: Task = tasks[0]

    onEntry {
        delay(2000)
        Furhat.dialogHistory.clear()
        furhat.say("Give me a hint!")
        reentry()
    }

    onReentry {
        furhat.listen()
    }

//    onResponse(currentPersona.name) {
//        furhat.gesture(GazeAversion(2.0))
//        val response = call {
//            currentPersona.chatbot.getResponse()
//        } as String
//        furhat.say(response)
//        reentry()
//    }

    onResponse {
//        furhat.gesture(GazeAversion(2.0))
//        val response = call {
//            currentPersona.chatbot.getResponse()
//        } as String
//        furhat.say(response)
        reentry()
    }

    onNoResponse {
        reentry()
    }
}