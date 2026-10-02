package com.guitartuner.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guitartuner.app.audio.AudioEngine
import com.guitartuner.app.audio.TunerState
import com.guitartuner.app.model.GuitarString
import com.guitartuner.app.model.TuningPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TunerViewModel : ViewModel() {

    private val audioEngine = AudioEngine(viewModelScope)
    val tunerState: StateFlow<TunerState> = audioEngine.tunerState

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        _hasPermission.value = granted
        if (granted) {
            audioEngine.startListening()
        } else {
            audioEngine.stopListening()
        }
    }

    fun startListening() {
        if (_hasPermission.value) {
            audioEngine.startListening()
        }
    }

    fun stopListening() {
        audioEngine.stopListening()
    }

    fun setTuning(tuning: TuningPreset) {
        audioEngine.setTuning(tuning)
    }

    fun selectString(guitarString: GuitarString?) {
        audioEngine.setTargetString(guitarString)
    }

    fun setReferencePitch(pitch: Float) {
        audioEngine.setReferencePitch(pitch)
    }

    fun playStringTone(guitarString: GuitarString) {
        audioEngine.playReferenceTone(guitarString.targetFreq)
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
