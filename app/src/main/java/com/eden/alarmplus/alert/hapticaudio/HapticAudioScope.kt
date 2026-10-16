package com.eden.alarmplus.alert.hapticaudio

import android.content.Context
import android.media.audiofx.AudioEffect
import androidx.media3.common.AudioAttributes
import android.media.audiofx.HapticGenerator
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.OptIn
import androidx.media3.common.C.AUDIO_CONTENT_TYPE_UNKNOWN
import androidx.media3.common.C.USAGE_ALARM
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer

class HapticAudioScope(
    private val attributionContext: Context
) {

    private var player: ExoPlayer? = null

    /**
     * Cancel previous audio and play new audio
     * @param audioUri audio to start
     * @param tryIncludeHaptic whether to try to include haptic
     * @return if haptics are played with this audio
     */
    @OptIn(UnstableApi::class)
    @MainThread
    fun start(
        audioUri: Uri,
        tryIncludeHaptic: Boolean
    ): Boolean = synchronized(this) {

        /** [AudioAttributes.Builder.setHapticChannelsMuted] is available from [Build.VERSION_CODES.Q],
         * However, it is difficult to check if the audio actually have an Android haptic channel;
         * Instead, we only play audio tied haptics when [HapticGenerator.isAvailable],
         * as it will generate haptics based on audio regardless if haptic channel exists
         */
        val shouldVibrate =
            tryIncludeHaptic &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            HapticGenerator.isAvailable()

        val vibrationEnabled: Boolean

        player?.release()
        player = ExoPlayer.Builder(attributionContext).build().apply {

            audioSessionId = Util.generateAudioSessionId(attributionContext)
            setMediaItem(MediaItem.fromUri(audioUri))
            prepare()

            repeatMode = Player.REPEAT_MODE_ONE
            setAudioAttributes(
                AudioAttributes.Builder().apply {
                    setUsage(USAGE_ALARM)
                    setContentType(AUDIO_CONTENT_TYPE_UNKNOWN)
                    if (shouldVibrate)
                        setHapticChannelsMuted(false)
                }.build(),
                false
            )

            vibrationEnabled = if (shouldVibrate)
                when(HapticGenerator.create(audioSessionId).setEnabled(true)) {
                    AudioEffect.SUCCESS -> true
                    AudioEffect.ERROR_INVALID_OPERATION -> {
                        Log.w("$javaClass", "Failed to enable haptic generator, invalid operation.")
                        false
                    }
                    AudioEffect.ERROR_DEAD_OBJECT -> {
                        Log.w("$javaClass", "Failed to enable haptic generator, dead object.")
                        false
                    }
                    else -> {
                        Log.w("$javaClass", "Failed to enable haptic generator, unknown error.")
                        false
                    }
                }
            else
                false

            play()
        }

        return@synchronized vibrationEnabled
    }

    @MainThread
    fun stopIfExist() = synchronized(this) {
        player?.release()
    }
}