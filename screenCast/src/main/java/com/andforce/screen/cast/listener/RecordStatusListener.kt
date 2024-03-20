package com.andforce.screen.cast.listener

interface RecordStatusListener {
    fun onStatus(status: RecordState)
}

sealed class RecordState {
    data object Recording : RecordState()
    data object Stopped : RecordState()
}