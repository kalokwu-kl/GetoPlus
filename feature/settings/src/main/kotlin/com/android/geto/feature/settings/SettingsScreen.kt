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
package com.android.geto.feature.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.geto.designsystem.icon.GetoIcons
import com.android.geto.domain.model.Theme
import com.android.geto.domain.model.UserData
import com.android.geto.feature.appsettings.dialog.WriteSecureSettingsDialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val settingsUiState by viewModel.settingsUiState.collectAsStateWithLifecycle()

    SettingsScreen(
        modifier = modifier,
        settingsUiState = settingsUiState,
        onUpdateTheme = viewModel::updateTheme,
        onUpdateDynamicTheme = viewModel::updateDynamicTheme,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@VisibleForTesting
@Composable
internal fun SettingsScreen(
    modifier: Modifier = Modifier,
    settingsUiState: SettingsUiState,
    onUpdateTheme: (Theme) -> Unit,
    onUpdateDynamicTheme: (Boolean) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.settings))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = GetoIcons.Back,
                            contentDescription = null,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            when (settingsUiState) {
                SettingsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is SettingsUiState.Success -> {
                    SuccessState(
                        userData = settingsUiState.userData,
                        onUpdateDynamicTheme = onUpdateDynamicTheme,
                        onUpdateTheme = onUpdateTheme,
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessState(
    modifier: Modifier = Modifier,
    userData: UserData,
    onUpdateDynamicTheme: (Boolean) -> Unit,
    onUpdateTheme: (Theme) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        CategoryTitle(title = stringResource(R.string.theme))

        ThemeSetting(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            selectedTheme = userData.theme,
            onUpdateTheme = onUpdateTheme,
        )

        Spacer(modifier = Modifier.height(8.dp))

        CategoryTitle(title = stringResource(R.string.permissions))

        Spacer(modifier = Modifier.height(4.dp))

        WriteSecurePermissionSetting()

        NotificationPermissionSetting()
    }
}

@Composable
private fun CategoryTitle(title: String) {
    Text(
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSetting(
    modifier: Modifier = Modifier,
    selectedTheme: Theme,
    onUpdateTheme: (Theme) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth(),
    ) {
        Theme.entries.forEachIndexed { index, theme ->
            SegmentedButton(
                selected = theme == selectedTheme,
                onClick = { onUpdateTheme(theme) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = Theme.entries.size,
                ),
            ) {
                Text(text = theme.getTitle())
            }
        }
    }
}

@Composable
private fun PermissionTile(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    statusIcon: @Composable () -> Unit,
    statusText: String,
    statusColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        statusIcon()

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall,
            color = statusColor,
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun NotificationPermissionSetting() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current

    val notificationsPermissionState = rememberPermissionState(
        Manifest.permission.POST_NOTIFICATIONS,
    )

    val status = notificationsPermissionState.status
    val isGranted = status is PermissionStatus.Granted

    PermissionTile(
        icon = {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = GetoIcons.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        title = stringResource(R.string.notification_permission),
        description = stringResource(R.string.notification_permission_desc),
        statusIcon = {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = if (isGranted) GetoIcons.CheckCircle else GetoIcons.Error,
                contentDescription = null,
                tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
        },
        statusText = if (isGranted) stringResource(R.string.notification_allowed) else stringResource(R.string.notification_not_allowed),
        statusColor = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        onClick = {
            if (isGranted) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            } else {
                notificationsPermissionState.launchPermissionRequest()
            }
        },
    )
}

@Composable
private fun WriteSecurePermissionSetting() {
    val context = LocalContext.current

    var isGranted by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        isGranted = withContext(Dispatchers.IO) {
            try {
                Settings.Global.putString(
                    context.contentResolver,
                    "geto_perm_check_temp",
                    null,
                )
                true
            } catch (_: SecurityException) {
                false
            }
        }
    }

    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        WriteSecureSettingsDialog(
            onDismissRequest = { showInfoDialog = false },
        )
    }

    when (isGranted) {
        true -> {
            PermissionTile(
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = GetoIcons.Android,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                title = stringResource(R.string.secure_settings_permission),
                description = stringResource(R.string.secure_settings_desc),
                statusIcon = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = GetoIcons.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                statusText = stringResource(R.string.secure_settings_granted),
                statusColor = MaterialTheme.colorScheme.primary,
                onClick = { showInfoDialog = true },
            )
        }
        false -> {
            PermissionTile(
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = GetoIcons.Android,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                title = stringResource(R.string.secure_settings_permission),
                description = stringResource(R.string.secure_settings_desc),
                statusIcon = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = GetoIcons.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                statusText = stringResource(R.string.secure_settings_not_granted),
                statusColor = MaterialTheme.colorScheme.error,
                onClick = { showInfoDialog = true },
            )
        }
        null -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.secure_settings_permission),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Text(
                        text = stringResource(R.string.secure_settings_desc),
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@Composable
internal fun Theme.getTitle() = when (this) {
    Theme.FOLLOW_SYSTEM -> stringResource(R.string.follow_system)
    Theme.LIGHT -> stringResource(R.string.light)
    Theme.DARK -> stringResource(R.string.dark)
}
