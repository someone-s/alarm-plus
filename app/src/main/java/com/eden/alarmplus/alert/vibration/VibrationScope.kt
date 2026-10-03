package com.eden.alarmplus.alert.vibration

import android.content.Context
import kotlinx.coroutines.CoroutineScope

class VibrationScope(context: Context) {

    private val vibrator: VibratorCompat? by lazy {
        VibratorCompat.fromContext(context)
    }

    private val effect: VibrationEffectCompat by lazy {
        val frequencyHz = vibrator?.frequencyProfile?.minFrequencyHz
        VibrationEffectCompat.createRepeatingEffect(
            if ((vibrator?.areEnvelopEffectsSupported() ?: false) && frequencyHz != null)
                VibrationEffectCompat.WaveformEnvelopBuilder()
                    .setInitialFrequencyHz(frequencyHz)
                    .addControlPoint(1f, frequencyHz, 1900)
                    .addControlPoint(1f, frequencyHz, 100)
                    .addControlPoint(0f, frequencyHz, 900)
                    .addControlPoint(0f, frequencyHz, 100)
                    .build()
            else
                VibrationEffectCompat.createWaveform(
                    longArrayOf(2000L, 400L),
                    intArrayOf(VibrationEffectCompat.DEFAULT_AMPLITUDE, 0),
                    -1
                )
        )
    }

    fun start(coroutineScope: CoroutineScope) {
        vibrator?.vibrate(effect, coroutineScope)
    }

    fun stopIfExist() {
        vibrator?.cancel()
    }
}