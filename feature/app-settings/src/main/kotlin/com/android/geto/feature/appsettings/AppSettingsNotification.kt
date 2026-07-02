/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.android.geto.feature.appsettings

import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.core.app.NotificationCompat
import com.android.geto.broadcastreceiver.RevertSettingsBroadcastReceiver
import com.android.geto.framework.notificationmanager.AndroidNotificationManagerWrapper
import com.android.geto.framework.notificationmanager.AndroidNotificationManagerWrapper.Companion.ACTION_REVERT_SETTINGS
import com.android.geto.framework.notificationmanager.AndroidNotificationManagerWrapper.Companion.NOTIFICATION_EXTRA_COMPONENT_NAME
import com.android.geto.framework.notificationmanager.AndroidNotificationManagerWrapper.Companion.NOTIFICATION_EXTRA_NOTIFICATION_ID

fun getAppSettingsNotification(
    context: Context,
    notificationId: Int,
    componentName: String,
    icon: ByteArray?,
    contentTitle: String,
    contentText: String,
    ongoing: Boolean = false,
): Notification {
    val revertIntent = Intent(context, RevertSettingsBroadcastReceiver::class.java).apply {
        action = ACTION_REVERT_SETTINGS
        putExtra(NOTIFICATION_EXTRA_COMPONENT_NAME, componentName)
        putExtra(NOTIFICATION_EXTRA_NOTIFICATION_ID, notificationId)
    }

    val revertPendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        revertIntent,
        FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
    )

    return NotificationCompat.Builder(
        context,
        AndroidNotificationManagerWrapper.NOTIFICATION_CHANNEL_ID,
    ).apply {
        setSmallIcon(com.android.geto.framework.notificationmanager.R.drawable.baseline_settings_24)

        icon?.let {
            setLargeIcon(Icon.createWithData(icon, 0, it.size))
        }

        setContentTitle(contentTitle)
        setContentText(contentText)
        setPriority(NotificationCompat.PRIORITY_DEFAULT)
        setOngoing(ongoing)
        addAction(
            com.android.geto.framework.notificationmanager.R.drawable.baseline_settings_24,
            context.getString(com.android.geto.framework.notificationmanager.R.string.revert),
            revertPendingIntent,
        )
    }.build()
}
