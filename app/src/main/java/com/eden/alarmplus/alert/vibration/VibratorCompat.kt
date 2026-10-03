package com.eden.alarmplus.alert.vibration

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.vibrator.VibratorFrequencyProfile
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

    fun areEnvelopEffectsSupported(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            val stateImpl = state as State.Impl26
            stateImpl.vibrator.areEnvelopeEffectsSupported()
        } else false

    val resonantFrequency: Float get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val stateImpl = state as State.Impl26
            stateImpl.vibrator.resonantFrequency
        } else Float.NaN
    
    val frequencyProfile: VibratorFrequencyProfileCompat? get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            val stateImpl = state as State.Impl26
            val value = stateImpl.vibrator.frequencyProfile
            return if (value != null)
                VibratorFrequencyProfileCompat.Impl36(value)
            else null
        } else null

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
                stateImpl.job = coroutineScope.launch {
                    do {
                        for (index in effectImpl.timings.indices) {
                            ensureActive()
                            val active = effectImpl.actives[index]
                            val timing = effectImpl.timings[index]
                            if (active)
                                stateImpl.vibrator.vibrate(timing)
                            delay(timing.milliseconds)
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

abstract class VibratorFrequencyProfileCompat {

    data class Impl36(
        val value: VibratorFrequencyProfile
    ): VibratorFrequencyProfileCompat()

    val minFrequencyHz: Float?
        get() = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.BAKLAVA) {
            val thisImpl = this as Impl36
            thisImpl.value.minFrequencyHz
        } else null

    val maxFrequencyHz: Float?
        get() = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.BAKLAVA) {
            val thisImpl = this as Impl36
            thisImpl.value.maxFrequencyHz
        } else null
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
        val timings: LongArray,
        val actives: BooleanArray,
        val isRepeating: Boolean,
    ): VibrationEffectCompat() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Impl0

            if (isRepeating != other.isRepeating) return false
            if (!timings.contentEquals(other.timings)) return false
            if (!actives.contentEquals(other.actives)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = isRepeating.hashCode()
            result = 31 * result + timings.contentHashCode()
            result = 31 * result + actives.contentHashCode()
            return result
        }
    }

    companion object {
        val DEFAULT_AMPLITUDE =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                VibrationEffect.DEFAULT_AMPLITUDE
            else 255

        fun createOneShot(
            milliseconds: Long,
            amplitude: Int
        ): VibrationEffectCompat =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA)
                Impl36(VibrationEffect.createOneShot(milliseconds, amplitude))
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                Impl26(
                    timings = longArrayOf(milliseconds),
                    amplitudes = intArrayOf(amplitude),
                    isRepeating = false
                )
            else
                Impl0(
                    timings = longArrayOf(milliseconds),
                    actives = booleanArrayOf(true),
                    isRepeating = false
                )

        fun createWaveform(
            timings: LongArray,
            amplitudes: IntArray,
            repeat: Int
        ): VibrationEffectCompat =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                Impl36(
                    value = VibrationEffect.createWaveform(
                        timings,
                        amplitudes,
                        repeat
                    )
                )
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Impl26(
                    timings = if (repeat >= 0) timings.take(repeat + 1).toLongArray() else timings,
                    amplitudes = if (repeat >= 0) amplitudes.take(repeat + 1).toIntArray() else amplitudes,
                    isRepeating = repeat >= 0
                )
            }
            else {
                Impl0(
                    timings = if (repeat >= 0) timings.take(repeat + 1).toLongArray() else timings,
                    actives = if (repeat >= 0)
                        amplitudes.take(repeat + 1).map { it != 0 }.toBooleanArray()
                    else
                        amplitudes.map { it != 0 }.toBooleanArray(),
                    isRepeating = repeat >= 0
                )
            }

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


    class BasicEnvelopBuilder {
        private abstract class State {

            data class Impl36(
                val basicEnvelopBuilder: VibrationEffect.BasicEnvelopeBuilder
            ): State()

            data class Impl26(
                val timings: MutableList<Long>,
                val amplitudes: MutableList<Int>
            ): State()

            data class Impl0(
                val timings: MutableList<Long>,
                val actives: MutableList<Boolean>
            ): State()
        }

        private val state: State =
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA)
             State.Impl36(VibrationEffect.BasicEnvelopeBuilder())
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
             State.Impl26(mutableListOf(), mutableListOf())
        else
             State.Impl0(mutableListOf(), mutableListOf())

        /**
         * Ignored if below [Build.VERSION_CODES.BAKLAVA];
         * Refer to [VibrationEffect.BasicEnvelopeBuilder.setInitialSharpness]
         */
        fun setInitialSharpness(
            @FloatRange(0.0, 1.0)
            initialSharpness: Float,
        ): BasicEnvelopBuilder {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                val builderImpl = state as State.Impl36
                builderImpl.basicEnvelopBuilder.setInitialSharpness(initialSharpness)
            }

            return this
        }

        /**
         * [sharpness] ignored if below [Build.VERSION_CODES.BAKLAVA];
         * Refer to [VibrationEffect.BasicEnvelopeBuilder.addControlPoint]
         */
        fun addControlPoint(
            @FloatRange(0.0, 1.0)
            intensity: Float,
            @FloatRange(0.0, 1.0)
            sharpness: Float,
            durationMillis: Long
        ): BasicEnvelopBuilder {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                 val builderImpl = state as State.Impl36
                 builderImpl.basicEnvelopBuilder.addControlPoint(
                     intensity,
                     sharpness,
                     durationMillis
                 )
             }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 val builderImpl = state as State.Impl26
                 builderImpl.timings.add(durationMillis)
                 builderImpl.amplitudes.add((intensity * 255f).toInt())
             }
            else {
                 val builderImpl = state as State.Impl0
                 builderImpl.timings.add(durationMillis)
                 builderImpl.actives.add(intensity > 0f)
             }

             return this
        }

        fun build(): VibrationEffectCompat =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                val builderImpl = state as State.Impl36
                Impl36(
                    value = builderImpl.basicEnvelopBuilder.build()
                )
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val builderImpl = state as State.Impl26
                Impl26(
                    timings = builderImpl.timings.toLongArray(),
                    amplitudes = builderImpl.amplitudes.toIntArray(),
                    isRepeating = false
                )
            }
            else {
                val builderImpl = state as State.Impl0
                Impl0(
                    timings = builderImpl.timings.toLongArray(),
                    actives = builderImpl.actives.toBooleanArray(),
                    isRepeating = false
                )
            }
    }


    class WaveformEnvelopBuilder {
        private abstract class State {

            data class Impl36(
                val waveformEnvelopBuilder: VibrationEffect.WaveformEnvelopeBuilder
            ): State()

            data class Impl26(
                val timings: MutableList<Long>,
                val amplitudes: MutableList<Int>
            ): State()

            data class Impl0(
                val timings: MutableList<Long>,
                val actives: MutableList<Boolean>
            ): State()
        }

        private val state: State =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA)
                State.Impl36(VibrationEffect.WaveformEnvelopeBuilder())
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                State.Impl26(mutableListOf(), mutableListOf())
            else
                State.Impl0(mutableListOf(), mutableListOf())

        /**
         * Ignored if below [Build.VERSION_CODES.BAKLAVA]
         * Refer to [VibrationEffect.WaveformEnvelopeBuilder.setInitialFrequencyHz]
         */
        fun setInitialFrequencyHz(
            @FloatRange(0.0)
            initialFrequencyHz: Float,
        ): WaveformEnvelopBuilder {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                val builderImpl = state as State.Impl36
                builderImpl.waveformEnvelopBuilder.setInitialFrequencyHz(initialFrequencyHz)
            }

            return this
        }

        /**
         * [frequencyHz] ignored if below [Build.VERSION_CODES.BAKLAVA]
         * Refer to [VibrationEffect.WaveformEnvelopeBuilder.addControlPoint]
         */
        fun addControlPoint(
            @FloatRange(0.0, 1.0)
            amplitude: Float,
            @FloatRange(0.0)
            frequencyHz: Float,
            durationMillis: Long
        ): WaveformEnvelopBuilder {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                val builderImpl = state as State.Impl36
                builderImpl.waveformEnvelopBuilder.addControlPoint(
                    amplitude,
                    frequencyHz,
                    durationMillis
                )
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val builderImpl = state as State.Impl26
                builderImpl.timings.add(durationMillis)
                builderImpl.amplitudes.add((amplitude * 255f).toInt())
            }
            else {
                val builderImpl = state as State.Impl0
                builderImpl.timings.add(durationMillis)
                builderImpl.actives.add(amplitude > 0f)
            }

            return this
        }

        fun build(): VibrationEffectCompat =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                val builderImpl = state as State.Impl36
                Impl36(
                    value = builderImpl.waveformEnvelopBuilder.build()
                )
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val builderImpl = state as State.Impl26
                Impl26(
                    timings = builderImpl.timings.toLongArray(),
                    amplitudes = builderImpl.amplitudes.toIntArray(),
                    isRepeating = false
                )
            }
            else {
                val builderImpl = state as State.Impl0
                Impl0(
                    timings = builderImpl.timings.toLongArray(),
                    actives = builderImpl.actives.toBooleanArray(),
                    isRepeating = false
                )
            }
    }
}