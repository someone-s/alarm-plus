package com.eden.alarmplus.alert.vibration

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.FloatRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@ConsistentCopyVisibility
data class VibratorCompat private constructor(
    private val state: State
) {

    abstract class State {
        data class Impl26(
            val vibrator: Vibrator
        ): State()
        data class Impl0(
            val vibrator: Vibrator,
        ): State() {
            var job: Job? = null
        }
    }

    companion object {

        fun fromContext(context: Context): VibratorCompat? {
            val vibrator =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
                else @Suppress("DEPRECATION")
                (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)

            return if (vibrator != null)
                VibratorCompat(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        State.Impl26(vibrator)
                    else
                        State.Impl0(vibrator)
                )
            else null
        }
    }

    fun vibrate(
        effect: VibrationEffectCompat,
        coroutineScope: CoroutineScope,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            val stateImpl = state as State.Impl26
            val effectImpl = effect as VibrationEffectCompat.Impl36
            stateImpl.vibrator.vibrate(effectImpl.value)
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val stateImpl = state as State.Impl26
            val effectImpl = effect as VibrationEffectCompat.Impl26
            if (effectImpl.timings.isNotEmpty()) {
                stateImpl.vibrator.vibrate(VibrationEffect.createWaveform(
                    effectImpl.timings,
                    effectImpl.amplitudes,
                    if (effectImpl.isRepeating) effectImpl.timings.lastIndex else -1
                ))
            }
        }
        else @Suppress("DEPRECATION") {
            val stateImpl = state as State.Impl0
            val effectImpl = effect as VibrationEffectCompat.Impl0
            if (effectImpl.timings.isNotEmpty()) {
                val points = effectImpl.timings.zip(effectImpl.actives)

                stateImpl.job = coroutineScope.launch {
                    do {
                        for (point in points) {
                            ensureActive()
                            if (point.second)
                                stateImpl.vibrator.vibrate(point.first)
                            delay(point.first.milliseconds)
                        }
                    }
                    while (effectImpl.isRepeating)
                }
            }
        }
    }

    fun cancel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val stateImpl = state as State.Impl26
            stateImpl.vibrator.cancel()
        }
        else {
            val stateImpl = state as State.Impl0
            stateImpl.job?.cancel()
            stateImpl.vibrator.cancel()
        }
    }
}

abstract class VibrationEffectCompat {

    data class Impl36(
        val value: VibrationEffect
    ): VibrationEffectCompat()

    data class Impl26(
        val timings: LongArray,
        val amplitudes: IntArray,
        val isRepeating: Boolean
    ): VibrationEffectCompat() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Impl26

            if (isRepeating != other.isRepeating) return false
            if (!timings.contentEquals(other.timings)) return false
            if (!amplitudes.contentEquals(other.amplitudes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = isRepeating.hashCode()
            result = 31 * result + timings.contentHashCode()
            result = 31 * result + amplitudes.contentHashCode()
            return result
        }
    }

    data class Impl0(
        val timings: List<Long>,
        val actives: List<Boolean>,
        val isRepeating: Boolean,
    ): VibrationEffectCompat()

    companion object {
        fun createRepeatingEffect(effect: VibrationEffectCompat): VibrationEffectCompat =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                val effectImpl = effect as Impl36
                Impl36(
                    VibrationEffect.createRepeatingEffect(effectImpl.value)
                )
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effectImpl = effect as Impl26
                Impl26(
                    timings = effectImpl.timings,
                    amplitudes = effectImpl.amplitudes,
                    isRepeating = true
                )
            }
            else {
                val effectImpl = effect as Impl0
                Impl0(
                    timings = effectImpl.timings,
                    actives = effectImpl.actives,
                    isRepeating = true
                )
            }
    }

    private abstract class BasicEnvelopBuilderState {

        data class Impl36(
            val basicEnvelopBuilder: VibrationEffect.BasicEnvelopeBuilder
        ): BasicEnvelopBuilderState()

        data class Impl26(
            val timings: MutableList<Long>,
            val amplitudes: MutableList<Int>
        ): BasicEnvelopBuilderState()

        data class Impl0(
            val timings: MutableList<Long>,
            val actives: MutableList<Boolean>
        ): BasicEnvelopBuilderState()
    }
    class BasicEnvelopBuilder {

        private val state: BasicEnvelopBuilderState =
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA)
             BasicEnvelopBuilderState.Impl36(VibrationEffect.BasicEnvelopeBuilder())
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
             BasicEnvelopBuilderState.Impl26(mutableListOf(), mutableListOf())
        else
             BasicEnvelopBuilderState.Impl0(mutableListOf(), mutableListOf())

        fun setInitialSharpness(
            @FloatRange(0.0, 1.0)
            initialSharpness: Float,
        ): BasicEnvelopBuilder {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                val builderImpl = state as BasicEnvelopBuilderState.Impl36
                builderImpl.basicEnvelopBuilder.setInitialSharpness(initialSharpness)
            }

            return this
        }
        fun addControlPoint(
            @FloatRange(0.0, 1.0)
            intensity: Float,
            @FloatRange(0.0, 1.0)
            sharpness: Float,
            durationMillis: Long
        ): BasicEnvelopBuilder {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                 val builderImpl = state as BasicEnvelopBuilderState.Impl36
                 builderImpl.basicEnvelopBuilder.addControlPoint(
                     intensity,
                     sharpness,
                     durationMillis
                 )
             }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 val builderImpl = state as BasicEnvelopBuilderState.Impl26
                 builderImpl.timings.add(durationMillis)
                 builderImpl.amplitudes.add((intensity * 255f).toInt())
             }
            else {
                 val builderImpl = state as BasicEnvelopBuilderState.Impl0
                 builderImpl.timings.add(durationMillis)
                 builderImpl.actives.add(intensity > 0f)
             }

             return this
        }

        fun build(): VibrationEffectCompat =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                val builderImpl = state as BasicEnvelopBuilderState.Impl36
                Impl36(
                    value = builderImpl.basicEnvelopBuilder.build()
                )
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val builderImpl = state as BasicEnvelopBuilderState.Impl26
                Impl26(
                    timings = builderImpl.timings.toLongArray(),
                    amplitudes = builderImpl.amplitudes.toIntArray(),
                    isRepeating = false
                )
            }
            else {
                val builderImpl = state as BasicEnvelopBuilderState.Impl0
                Impl0(
                    timings = builderImpl.timings,
                    actives = builderImpl.actives,
                    isRepeating = false
                )
            }
    }
}