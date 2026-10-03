package com.eden.alarmplus.alert.vibration

import android.content.Context
import kotlinx.coroutines.CoroutineScope

class VibrationScope(context: Context) {

    private val vibrator: VibratorCompat? by lazy {
        VibratorCompat.fromContext(context)
    }

    private val effect: VibrationEffectCompat by lazy {
        VibrationEffectCompat.createRepeatingEffect(
            VibrationEffectCompat.BasicEnvelopBuilder()
                .setInitialSharpness(0.5f)
                .addControlPoint(1f, 0.5f, 1900)
                .addControlPoint(1f, 0.5f, 100)
                .addControlPoint(0f, 0.5f, 900)
                .addControlPoint(0f, 0.5f, 100)
                .build()
        )
    }

    fun start(coroutineScope: CoroutineScope) {
        vibrator?.vibrate(effect, coroutineScope)
    }

    fun stopIfExist() {
        vibrator?.cancel()
    }
}