package com.github.l34130.mise.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.messages.Topic

@State(name = "com.github.l34130.mise.settings.MiseSettings", storages = [Storage("mise.xml")])
class MiseSettings private constructor() : PersistentStateComponent<MiseSettings.State> {
    override fun getState() = STATE

    override fun loadState(state: State) {
        val oldState = STATE.copy()
        STATE.isMiseEnabled = state.isMiseEnabled
        STATE.miseProfile = state.miseProfile
        ApplicationManager.getApplication().messageBus
            .syncPublisher(MISE_SETTINGS_TOPIC)
            .settingsChanged(oldState, state)
    }

    companion object {
        val MISE_SETTINGS_TOPIC = Topic.create("Mise Settings", SettingsChangeListener::class.java)
        private val STATE = State()
        val instance: MiseSettings = ApplicationManager.getApplication().getService(MiseSettings::class.java)
    }

    data class State(
        var isMiseEnabled: Boolean = true,
        var miseProfile: String = ""
    )

    interface SettingsChangeListener {
        fun settingsChanged(oldState: State, newState: State)
    }
}
