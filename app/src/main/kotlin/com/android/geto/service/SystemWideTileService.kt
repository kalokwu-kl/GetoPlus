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
import com.android.geto.domain.model.AppSettingsResult
import com.android.geto.domain.repository.UserDataRepository
import com.android.geto.domain.usecase.ApplyAppSettingsUseCase
import com.android.geto.domain.usecase.RevertAppSettingsUseCase
import com.android.geto.feature.appsettings.getAppSettingsNotification
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
    lateinit var applyAppSettingsUseCase: ApplyAppSettingsUseCase

    @Inject
    lateinit var revertAppSettingsUseCase: RevertAppSettingsUseCase

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
            val isApplied = userData.isConfigApplied
            
            if (isApplied) {
                val result = revertAppSettingsUseCase()
                if (result == AppSettingsResult.Success) {
                    userDataRepository.updateConfigApplied(false)
                    notificationManagerWrapper.cancel(GLOBAL_CONFIG_UID.hashCode())
                    Toast.makeText(
                        this@SystemWideTileService,
                        com.android.geto.feature.appsettings.R.string.revert_success,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val result = applyAppSettingsUseCase()
                if (result == AppSettingsResult.Success) {
                    userDataRepository.updateConfigApplied(true)
                    val notificationId = GLOBAL_CONFIG_UID.hashCode()
                    notificationManagerWrapper.notify(
                        id = notificationId,
                        notification = getAppSettingsNotification(
                            context = this@SystemWideTileService,
                            notificationId = notificationId,
                            componentName = GLOBAL_CONFIG_UID,
                            icon = null,
                            contentTitle = getString(com.android.geto.feature.appsettings.R.string.geto_settings),
                            contentText = getString(com.android.geto.feature.appsettings.R.string.apply_success),
                            ongoing = true,
                        )
                    )
                    Toast.makeText(
                        this@SystemWideTileService,
                        com.android.geto.feature.appsettings.R.string.apply_success,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
