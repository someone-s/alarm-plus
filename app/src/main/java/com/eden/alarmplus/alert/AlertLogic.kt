package com.eden.alarmplus.alert

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Parcelable
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import com.eden.alarmplus.BuildConfig
import com.eden.alarmplus.alert.hapticaudio.HapticAudioScope
import com.eden.alarmplus.alert.notification.NotificationScope
import com.eden.alarmplus.alert.vibration.VibrationScope
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.alarm.feature.AudioFeature
import com.eden.alarmplus.data.alarms.structure.alarm.feature.VibrateFeature
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.logic.access.DismissReceiver
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

class AlertLogic(
    private val service: AlertService,
    private val notificationScope: NotificationScope,
    private val hapticAudioScope: HapticAudioScope,
    private val vibrationScope: VibrationScope,
) {

    companion object {

        const val ACTION_ALERT = "${BuildConfig.APPLICATION_ID}.ACTION_ALERT"

        /**
         * Extra of type [Parcelable] that is [com.eden.alarmplus.data.alarms.structure.group.GroupInfo]
         */
        const val EXTRA_GROUP_INFO = "${BuildConfig.APPLICATION_ID}.EXTRA_GROUP_INFO"

        /**
         * Extra of type [Parcelable] that is [com.eden.alarmplus.data.alarms.structure.alarm.AlarmData]
         */
        const val EXTRA_ALARM_DATA = "${BuildConfig.APPLICATION_ID}.EXTRA_ALARM_DATA"

        /**
         * Extra of type [Boolean]
         */
        const val EXTRA_IS_LAST_IN_GROUP = "${BuildConfig.APPLICATION_ID}.EXTRA_IS_LAST_IN_GROUP"


        const val ACTION_SNOOZE = "${BuildConfig.APPLICATION_ID}.ACTION_SNOOZE"
        const val ACTION_DISMISS = "${BuildConfig.APPLICATION_ID}.ACTION_DISMISS"
        const val EXTRA_GROUP_UUID = "${BuildConfig.APPLICATION_ID}.EXTRA_GROUP_UUID"

        private val FOREGROUND_SERVICE_TYPE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
        else 0

    }


    /**
     * Last element is the currently playing alert,
     * the service is always bound to the last notification
     */
    private val activeQueue = ArrayDeque<AlertState>()

    /**
     * @param startId identifies request
     * @param intent have the following types:
     *  - [ACTION_ALERT]
     *      - [EXTRA_GROUP_INFO]
     *      - [EXTRA_ALARM_DATA]
     *      - [EXTRA_IS_LAST_IN_GROUP]
     *  - [ACTION_SNOOZE]
     *      - [EXTRA_GROUP_UUID]
     *  - [ACTION_DISMISS]
     *      - [EXTRA_GROUP_UUID]
     */
    fun processRequest(startId: Int, intent: Intent) {
        intent.dataString
        when (intent.action) {
            ACTION_ALERT -> processAlert(
                serviceStartId = startId,
                notificationId = startId,
                groupInfo = IntentCompat.getParcelableExtra(
                    intent, EXTRA_GROUP_INFO, GroupInfo::class.java)
                    ?: throw IllegalArgumentException("Intent missing group info"),
                alarmData = IntentCompat.getParcelableExtra(
                    intent, EXTRA_ALARM_DATA, AlarmData::class.java
                ) ?: throw IllegalArgumentException("Intent missing alarm data."),
                isLastInGroup = intent.getBooleanExtra(
                    EXTRA_IS_LAST_IN_GROUP, false
                )
            )
            ACTION_SNOOZE -> processSnooze(
                serviceStartId = startId,
                groupUuid = IntentCompat.getSerializableExtra(
                    intent, EXTRA_GROUP_UUID, Uuid::class.java
                ) ?: throw IllegalArgumentException("Intent missing group uuid."),
            )
            ACTION_DISMISS -> processDismiss(
                serviceStartId = startId,
                groupUuid = IntentCompat.getSerializableExtra(
                    intent, EXTRA_GROUP_UUID, Uuid::class.java
                ) ?: throw IllegalArgumentException("Intent missing group uuid."),
            )
            else -> throw IllegalArgumentException("Intent action not expected values.")
        }
    }

    private fun processAlert(
        serviceStartId: Int,
        notificationId: Int,
        groupInfo: GroupInfo,
        alarmData: AlarmData,
        isLastInGroup: Boolean,
    ) {

        // Exit if no permission to send notifications
        if (ContextCompat.checkSelfPermission(
                service,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
            Log.w("$javaClass", "Missing POST_NOTIFICATIONS permission")
            return
        }

        val notificationManager = NotificationManagerCompat.from(service)

        synchronized(this@AlertLogic) {

            startNotification(notificationId, groupInfo, alarmData, isLastInGroup, notificationManager)

            cancelNotification(serviceStartId, groupInfo.uuid, notificationId, notificationManager)
        }
    }

    private fun startNotification(
        notificationId: Int,
        groupInfo: GroupInfo,
        alarmData: AlarmData,
        isLastInGroup: Boolean,
        notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(service),
    ) {
        if (activeQueue.isNotEmpty())
            ServiceCompat.stopForeground(
                service,
                ServiceCompat.STOP_FOREGROUND_DETACH
            )

        val notification = with(notificationManager) {
            notificationScope.Builder(groupInfo).apply {
                addTimeout(10.minutes.inWholeMilliseconds)

                if (!isLastInGroup)
                    addSnoozeIntent(
                        PendingIntent.getService(
                            service,
                            notificationId,
                            Intent(service, AlertService::class.java).apply {
                                action = ACTION_SNOOZE
                                putExtra(EXTRA_GROUP_UUID, groupInfo.uuid)
                            },
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )

                addDismissIntent(
                    PendingIntent.getBroadcast(
                        service,
                        notificationId,
                        Intent(service, DismissReceiver::class.java).apply {
                            putExtra(DismissReceiver.EXTRA_GROUP_UUID, groupInfo.uuid)
                        },
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )

                if (canUseFullScreenIntent())
                    addFullScreen(notificationId)

            }.build(this@with)
        }

        val state = AlertState(
            groupUuid = groupInfo.uuid,
            notificationId = notificationId,
            notification = notification,
            audioData = alarmData.getFeature<AudioFeature>()?.data,
            vibrateEnabled = alarmData.getFeature<VibrateFeature>() != null
        )

        activeQueue.addLast(state)

        playNotification(state)
    }

    private fun playNotification(alertState: AlertState) {
        ServiceCompat.startForeground(
            service,
            alertState.notificationId,
            alertState.notification,
            FOREGROUND_SERVICE_TYPE
        )

        if (alertState.audioData != null)
            hapticAudioScope.start(alertState.audioData, alertState.vibrateEnabled)

        if (alertState.vibrateEnabled)
            vibrationScope.start(service.lifecycleScope)
    }

    private fun cancelNotification(
        serviceStartId: Int,
        groupUuid: Uuid,
        exceptionNotificationId: Int? = null,
        notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(service),
    ) {
        val statesToCancel = activeQueue
            .filter { it.groupUuid == groupUuid }
            .filter { it.notificationId != exceptionNotificationId }
            .toList()

        // last notification is most recent notification
        val anyLastNotification = statesToCancel.any { it.notificationId == (activeQueue.lastOrNull()?.notificationId) }

        if (anyLastNotification) {
            hapticAudioScope.stopIfExist()

            vibrationScope.stopIfExist()
        }

        // Remove notification from list
        activeQueue.removeAll(statesToCancel)

        if (activeQueue.isEmpty()) {

            // Stop foreground also cancel notification
            ServiceCompat.stopForeground(
                service,
                ServiceCompat.STOP_FOREGROUND_REMOVE
            )

            service.stopSelf(serviceStartId)
        }
        else {
            if (anyLastNotification)
                // Bind new last notification, notification pushed again
                playNotification(activeQueue.last())

            // Cancel notification
            with(notificationManager) {
                statesToCancel.forEach { cancel(it.notificationId) }
            }
        }
    }

    private fun processSnooze(
        serviceStartId: Int,
        groupUuid: Uuid
    ) {
        synchronized(this@AlertLogic) {
            cancelNotification(serviceStartId, groupUuid)
        }
    }

    private fun processDismiss(
        serviceStartId: Int,
        groupUuid: Uuid
    ) {
        synchronized(this@AlertLogic) {
            cancelNotification(serviceStartId, groupUuid)
        }
    }
}