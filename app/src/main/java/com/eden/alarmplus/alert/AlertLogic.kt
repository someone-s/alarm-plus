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
import com.eden.alarmplus.BuildConfig
import com.eden.alarmplus.alert.notification.NotificationScope
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.logic.StateProcessor
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

class AlertLogic(
    private val service: AlertService,
    private val stateProcessor: StateProcessor,
    private val notificationScope: NotificationScope,
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

        const val EXTRA_NOTIFICATION_ID = "${BuildConfig.APPLICATION_ID}.EXTRA_NOTIFICATION_ID"
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
     *      - [EXTRA_NOTIFICATION_ID]
     *  - [ACTION_DISMISS]
     *      - [EXTRA_NOTIFICATION_ID]
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
                notificationId = intent.getIntExtra(
                    EXTRA_NOTIFICATION_ID, -1
                ).also { if (it < 0) throw IllegalArgumentException("Intent missing notification id.") }
            )
            ACTION_DISMISS -> processDismiss(
                serviceStartId = startId,
                notificationId = intent.getIntExtra(
                    EXTRA_NOTIFICATION_ID, -1
                ).also { if (it < 0) throw IllegalArgumentException("Intent missing notification id.") },
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

        val notification = with(notificationManager) {
            notificationScope.Builder(groupInfo).apply {
                addTimeout(10.minutes.inWholeMilliseconds)

                if (!isLastInGroup)
                    addSnoozeIntent(PendingIntent.getService(
                        service,
                        notificationId,
                        Intent(service, AlertService::class.java).apply {
                            action = ACTION_SNOOZE
                            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                        },
                        PendingIntent.FLAG_IMMUTABLE
                    ))

                addDismissIntent(PendingIntent.getService(
                    service,
                    notificationId,
                    Intent(service, AlertService::class.java).apply {
                        action = ACTION_DISMISS
                        putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                        putExtra(EXTRA_GROUP_UUID, groupInfo.uuid)
                    },
                    PendingIntent.FLAG_IMMUTABLE
                ))

                if (canUseFullScreenIntent())
                    addFullScreen(notificationId)

            }.build(this@with)
        }


        synchronized(this@AlertLogic) {

            if (activeQueue.isNotEmpty())
                ServiceCompat.stopForeground(
                    service,
                    ServiceCompat.STOP_FOREGROUND_DETACH
                )

            ServiceCompat.startForeground(
                service,
                notificationId,
                notification,
                FOREGROUND_SERVICE_TYPE
            )

            activeQueue.addLast(AlertState(
                groupUuid = groupInfo.uuid,
                notificationId = notificationId,
                notification = notification,
                /** If full screen, set by [processFullScreenResume] */
                isFullScreen = false,
            ))

            // Cancel previous notifications from the same group
            val currentCopy = activeQueue
                .filter { it.groupUuid == groupInfo.uuid }
                .filter { it.notificationId != notificationId }
                .toList()

            currentCopy.forEach { cancelNotification(serviceStartId, it.notificationId, notificationManager) }
        }
    }

    private fun cancelNotification(
        serviceStartId: Int,
        notificationId: Int,
        notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(service),
    ) {
        val isLastNotification = notificationId == (activeQueue.lastOrNull()?.notificationId)

        // Remove notification from list
        activeQueue.removeIf { it.notificationId == notificationId }

        if (activeQueue.isEmpty()) {
            // Stop foreground also cancel notification
            ServiceCompat.stopForeground(
                service,
                ServiceCompat.STOP_FOREGROUND_REMOVE
            )

            service.stopSelf(serviceStartId)
        }
        else {
            if (isLastNotification)
                // Bind new last notification, notification pushed again
                ServiceCompat.startForeground(
                    service,
                    activeQueue.last().notificationId,
                    activeQueue.last().notification,
                    FOREGROUND_SERVICE_TYPE
                )

            // Cancel notification
            with(notificationManager) {
                cancel(notificationId)
            }
        }
    }

    private fun processSnooze(
        serviceStartId: Int,
        notificationId: Int
    ) {
        synchronized(this@AlertLogic) {
            cancelNotification(serviceStartId, notificationId)
        }
    }

    private fun processDismiss(
        serviceStartId: Int,
        notificationId: Int,
        groupUuid: Uuid
    ) {
        runBlocking {
            stateProcessor.dismissGroup(groupUuid)
        }
        synchronized(this@AlertLogic) {
            cancelNotification(serviceStartId, notificationId)
        }
    }
}