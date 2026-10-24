package com.eden.alarmplus.ui.main.repeats.edit.alarm

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract

class RingtonePickerContract: ActivityResultContract<Uri?, Uri?>() {
    override fun createIntent(
        context: Context,
        input: Uri?
    ): Intent {
        return Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_ALARM_ALERT_URI)
            if (input != null)
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, input)
        }
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): Uri? {
        if (resultCode != Activity.RESULT_OK)
            return null

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
        else
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)

    }
}