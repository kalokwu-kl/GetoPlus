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

import com.android.geto.domain.model.AppSettingsMode
import com.android.geto.domain.model.AppSettingsResult

fun AppSettingsResult.messageRes(mode: AppSettingsMode): Int = when (this) {
    AppSettingsResult.DisabledAppSettings -> R.string.app_settings_disabled
    AppSettingsResult.EmptyAppSettings -> R.string.empty_app_settings_list
    AppSettingsResult.Failure -> when (mode) {
        AppSettingsMode.Apply -> R.string.apply_failure
        AppSettingsMode.Revert -> R.string.revert_failure
    }

    AppSettingsResult.NoPermission -> R.string.required_permission_not_granted
    AppSettingsResult.Success -> when (mode) {
        AppSettingsMode.Apply -> R.string.apply_success
        AppSettingsMode.Revert -> R.string.revert_success
    }

    AppSettingsResult.InvalidValues -> R.string.settings_has_invalid_values
}
