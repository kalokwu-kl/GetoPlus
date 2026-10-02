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
package com.android.geto.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.android.geto.domain.common.GLOBAL_CONFIG_UID
import com.android.geto.domain.model.AppSettingsMode
import com.android.geto.domain.model.AppSettingsResult
import com.android.geto.domain.repository.UserDataRepository
import com.android.geto.domain.usecase.ToggleAppSettingsUseCase
import com.android.geto.feature.appsettings.getAppSettingsNotification
import com.android.geto.feature.appsettings.messageRes
import com.android.geto.framework.notificationmanager.AndroidNotificationManagerWrapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SystemWideTileService : TileService() {

    @Inject
    lateinit var toggleAppSettingsUseCase: ToggleAppSettingsUseCase

    @Inject
    lateinit var userDataRepository: UserDataRepository

    @Inject
    lateinit var notificationManagerWrapper: AndroidNotificationManagerWrapper

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onStartListening() {
        super.onStartListening()
        serviceScope.launch {
            userDataRepository.userData.collectLatest { userData ->
                qsTile.state = if (userData.isConfigApplied) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                qsTile.updateTile()
            }
        }
    }

    override fun onClick() {
        super.onClick()
        serviceScope.launch {
            val userData = userDataRepository.userData.first()
            val mode = if (userData.isConfigApplied) AppSettingsMode.Revert else AppSettingsMode.Apply

            val result = toggleAppSettingsUseCase(mode)

            when {
                result == AppSettingsResult.Success && mode == AppSettingsMode.Revert -> {
                    userDataRepository.updateConfigApplied(false)
                    notificationManagerWrapper.cancel(GLOBAL_CONFIG_UID.hashCode())
                    showToast(result, mode)
                }

                result == AppSettingsResult.Success && mode == AppSettingsMode.Apply -> {
                    userDataRepository.updateConfigApplied(true)
                    val notificationId = GLOBAL_CONFIG_UID.hashCode()
                    notificationManagerWrapper.notify(
                        id = notificationId,
                        notification = getAppSettingsNotification(
                            context = this@SystemWideTileService,
                            notificationId = notificationId,
                            componentName = GLOBAL_CONFIG_UID,
                            contentTitle = getString(com.android.geto.feature.appsettings.R.string.applied_notification_title),
                            contentText = getString(com.android.geto.feature.appsettings.R.string.applied_notification_text),
                            ongoing = true,
                        )
                    )
                    showToast(result, mode)
                }

                result == AppSettingsResult.NoPermission -> showToast(result, mode)

                result == AppSettingsResult.EmptyAppSettings || result == AppSettingsResult.DisabledAppSettings -> showToast(result, mode)
            }
        }
    }

    private fun showToast(result: AppSettingsResult, mode: AppSettingsMode) {
        Toast.makeText(
            this,
            getString(result.messageRes(mode)),
            Toast.LENGTH_SHORT,
        ).show()
    }
}
