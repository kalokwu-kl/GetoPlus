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

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.geto.designsystem.component.DialogContainer
import com.android.geto.designsystem.icon.GetoIcons
import com.android.geto.domain.common.GLOBAL_CONFIG_UID
import com.android.geto.domain.model.AddAppSettingResult
import com.android.geto.domain.model.AppSetting
import com.android.geto.domain.model.AppSettingTemplate
import com.android.geto.domain.model.AppSettingsMode
import com.android.geto.domain.model.AppSettingsResult
import com.android.geto.domain.model.SecureSetting
import com.android.geto.domain.model.SettingType
import com.android.geto.feature.appsettings.dialog.AppSettingDialog
import com.android.geto.feature.appsettings.dialog.TemplateDialog
import com.android.geto.designsystem.local.LocalNotificationManager

@Composable
internal fun AppSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: AppSettingsViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit,
) {
    val appSettingsUiState by viewModel.appSettingsUiState.collectAsStateWithLifecycle()

    val secureSettings by viewModel.secureSettings.collectAsStateWithLifecycle()

    val applyAppSettingsResult by viewModel.applyAppSettingsResult.collectAsStateWithLifecycle()

    val revertAppSettingsResult by viewModel.revertAppSettingsResult.collectAsStateWithLifecycle()

    val addAppSettingResult by viewModel.addAppSettingsResult.collectAsStateWithLifecycle()

    val appSettingTemplates by viewModel.appSettingTemplates.collectAsStateWithLifecycle()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    AppSettingsScreen(
        modifier = modifier,
        appSettingsUiState = appSettingsUiState,
        snackbarHostState = snackbarHostState,
        secureSettings = secureSettings,
        addAppSettingResult = addAppSettingResult,
        applyAppSettingsResult = applyAppSettingsResult,
        revertAppSettingsResult = revertAppSettingsResult,
        appSettingTemplates = appSettingTemplates,
        onToggleConfig = viewModel::toggleConfig,
        onCheckAppSetting = viewModel::checkAppSetting,
        onDeleteAppSetting = viewModel::deleteAppSetting,
        onAddAppSetting = viewModel::addAppSetting,
        onUpdateAppSetting = viewModel::updateAppSetting,
        onGetSecureSettingsByName = viewModel::getSecureSettingsByName,
        onResetApplyAppSettingsResult = viewModel::resetApplyAppSettingsResult,
        onResetRevertAppSettingsResult = viewModel::resetRevertAppSettingsResult,
        onResetAddAppSettingResult = viewModel::resetAddAppSettingResult,
        onSettingsClick = onSettingsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppSettingsScreen(
    modifier: Modifier = Modifier,
    appSettingsUiState: AppSettingsUiState,
    snackbarHostState: SnackbarHostState,
    secureSettings: List<SecureSetting>,
    addAppSettingResult: AddAppSettingResult?,
    applyAppSettingsResult: AppSettingsResult?,
    revertAppSettingsResult: AppSettingsResult?,
    appSettingTemplates: List<AppSettingTemplate>,
    onToggleConfig: (Boolean) -> Unit,
    onCheckAppSetting: (appSetting: AppSetting) -> Unit,
    onDeleteAppSetting: (appSetting: AppSetting) -> Unit,
    onAddAppSetting: (AppSetting) -> Unit,
    onUpdateAppSetting: (appSetting: AppSetting) -> Unit,
    onGetSecureSettingsByName: (settingType: SettingType, text: String) -> Unit,
    onResetApplyAppSettingsResult: () -> Unit,
    onResetRevertAppSettingsResult: () -> Unit,
    onResetAddAppSettingResult: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    var showAppSettingDialog by remember { mutableStateOf(false) }

    var editingAppSetting by remember { mutableStateOf<AppSetting?>(null) }

    var showTemplateDialog by remember { mutableStateOf(false) }

    var showQuickSettingsTipDialog by remember { mutableStateOf(false) }

    AppSettingsLaunchedEffects(
        snackbarHostState = snackbarHostState,
        addAppSettingResult = addAppSettingResult,
        applyAppSettingsResult = applyAppSettingsResult,
        revertAppSettingsResult = revertAppSettingsResult,
        onResetApplyAppSettingsResult = onResetApplyAppSettingsResult,
        onResetRevertAppSettingsResult = onResetRevertAppSettingsResult,
        onResetAddAppSettingResult = onResetAddAppSettingResult,
        onNoPermission = onSettingsClick,
    )

    AppSettingsDialogs(
        secureSettings = secureSettings,
        appSettingTemplates = appSettingTemplates,
        showAppSettingDialog = showAppSettingDialog || editingAppSetting != null,
        editingAppSetting = editingAppSetting,
        showTemplateDialog = showTemplateDialog,
        onAddAppSetting = onAddAppSetting,
        onUpdateAppSetting = onUpdateAppSetting,
        onDismissAppSettingDialog = {
            showAppSettingDialog = false
            editingAppSetting = null
        },
        onDismissTemplateDialog = {
            showTemplateDialog = false
        },
        onGetSecureSettingsByName = onGetSecureSettingsByName,
    )

    if (showQuickSettingsTipDialog) {
        QuickSettingsTipDialog(onDismissRequest = { showQuickSettingsTipDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(com.android.geto.designsystem.R.drawable.geto_logo),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(text = stringResource(id = R.string.geto_title))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = GetoIcons.Menu,
                            contentDescription = null,
                        )
                    }
                }
            )
        },
        bottomBar = {
            AppSettingsBottomAppBar(
                onAddConfigIconClick = {
                    showAppSettingDialog = true
                },
                onSettingsSuggestIconClick = {
                    showTemplateDialog = true
                },
                onQuickSettingsTipClick = {
                    showQuickSettingsTipDialog = true
                },
                isConfigApplied = if (appSettingsUiState is AppSettingsUiState.Success) appSettingsUiState.isConfigApplied else false,
                onFloatingActionButtonClick = {
                    if (appSettingsUiState is AppSettingsUiState.Success) {
                        onToggleConfig(appSettingsUiState.isConfigApplied)
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .consumeWindowInsets(innerPadding),
        ) {
            when (appSettingsUiState) {
                AppSettingsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is AppSettingsUiState.Success -> {
                    if (appSettingsUiState.appSettings.isNotEmpty()) {
                        SuccessState(
                            isConfigApplied = appSettingsUiState.isConfigApplied,
                            appSettingsUiState = appSettingsUiState,
                            onCheckAppSetting = onCheckAppSetting,
                            onDeleteAppSettingsItem = onDeleteAppSetting,
                            onEditAppSettingItem = {
                                editingAppSetting = it
                            },
                        )
                    } else {
                        EmptyState(
                            title = stringResource(R.string.no_settings_found),
                            subtitle = stringResource(R.string.add_your_first_settings),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppSettingsLaunchedEffects(
    snackbarHostState: SnackbarHostState,
    addAppSettingResult: AddAppSettingResult?,
    applyAppSettingsResult: AppSettingsResult?,
    revertAppSettingsResult: AppSettingsResult?,
    onResetApplyAppSettingsResult: () -> Unit,
    onResetRevertAppSettingsResult: () -> Unit,
    onResetAddAppSettingResult: () -> Unit,
    onNoPermission: () -> Unit,
) {
    val context = LocalContext.current

    val androidNotificationManagerWrapper = LocalNotificationManager.current

    val appSettingAddSuccess = stringResource(id = R.string.app_setting_added_successfully)

    val appSettingAddFailed = stringResource(id = R.string.app_setting_already_exists)

    val appliedNotificationTitle = stringResource(R.string.applied_notification_title)

    val appliedNotificationText = stringResource(R.string.applied_notification_text)

    suspend fun handleAppSettingsResult(
        result: AppSettingsResult,
        mode: AppSettingsMode,
        onSuccess: suspend () -> Unit,
    ) {
        when (result) {
            AppSettingsResult.NoPermission -> {
                Toast.makeText(
                    context,
                    context.getString(result.messageRes(mode)),
                    Toast.LENGTH_SHORT,
                ).show()
                onNoPermission()
            }

            AppSettingsResult.Success -> {
                Toast.makeText(
                    context,
                    context.getString(result.messageRes(mode)),
                    Toast.LENGTH_SHORT,
                ).show()
                onSuccess()
            }

            else -> {
                snackbarHostState.showSnackbar(context.getString(result.messageRes(mode)))
            }
        }
    }

    LaunchedEffect(key1 = applyAppSettingsResult) {
        applyAppSettingsResult?.let { result ->
            handleAppSettingsResult(result, AppSettingsMode.Apply) {
                val notificationId = GLOBAL_CONFIG_UID.hashCode()

                androidNotificationManagerWrapper.notify(
                    id = notificationId,
                    notification = getAppSettingsNotification(
                        context = context,
                        notificationId = notificationId,
                        componentName = GLOBAL_CONFIG_UID,
                        contentTitle = appliedNotificationTitle,
                        contentText = appliedNotificationText,
                        ongoing = true,
                    ),
                )
            }
        }

        onResetApplyAppSettingsResult()
    }

    LaunchedEffect(key1 = revertAppSettingsResult) {
        revertAppSettingsResult?.let { result ->
            handleAppSettingsResult(result, AppSettingsMode.Revert) {
                androidNotificationManagerWrapper.cancel(GLOBAL_CONFIG_UID.hashCode())
            }
        }

        onResetRevertAppSettingsResult()
    }

    LaunchedEffect(key1 = addAppSettingResult) {
        when (addAppSettingResult) {
            AddAppSettingResult.SUCCESS -> {
                snackbarHostState.showSnackbar(message = appSettingAddSuccess)
            }

            AddAppSettingResult.FAILED -> {
                snackbarHostState.showSnackbar(message = appSettingAddFailed)
            }

            null -> Unit
        }

        onResetAddAppSettingResult()
    }
}

@Composable
private fun AppSettingsDialogs(
    secureSettings: List<SecureSetting>,
    appSettingTemplates: List<AppSettingTemplate>,
    showAppSettingDialog: Boolean,
    editingAppSetting: AppSetting? = null,
    showTemplateDialog: Boolean,
    onAddAppSetting: (AppSetting) -> Unit,
    onUpdateAppSetting: ((AppSetting) -> Unit)? = null,
    onDismissAppSettingDialog: () -> Unit,
    onDismissTemplateDialog: () -> Unit,
    onGetSecureSettingsByName: (
        settingType: SettingType,
        text: String,
    ) -> Unit,
) {
    if (showAppSettingDialog) {
        AppSettingDialog(
            componentName = GLOBAL_CONFIG_UID,
            secureSettings = secureSettings,
            appSetting = editingAppSetting,
            onAddAppSetting = onAddAppSetting,
            onUpdateAppSetting = onUpdateAppSetting,
            onDismissRequest = onDismissAppSettingDialog,
            onGetSecureSettingsByName = onGetSecureSettingsByName,
        )
    }

    if (showTemplateDialog) {
        TemplateDialog(
            appSettingTemplates = appSettingTemplates,
            componentName = GLOBAL_CONFIG_UID,
            onAddAppSetting = onAddAppSetting,
            onDismissRequest = onDismissTemplateDialog,
        )
    }
}

@Composable
private fun AppSettingsBottomAppBar(
    onAddConfigIconClick: () -> Unit,
    onSettingsSuggestIconClick: () -> Unit,
    onQuickSettingsTipClick: () -> Unit,
    isConfigApplied: Boolean,
    onFloatingActionButtonClick: () -> Unit,
) {
    BottomAppBar(
        actions = {
            AppSettingsBottomAppBarActions(
                onAddConfigIconClick = onAddConfigIconClick,
                onSettingsSuggestIconClick = onSettingsSuggestIconClick,
                onQuickSettingsTipClick = onQuickSettingsTipClick,
            )
        },
        floatingActionButton = {
            AppSettingsFloatingActionButton(
                isConfigApplied = isConfigApplied,
                onClick = onFloatingActionButtonClick,
            )
        },
    )
}

@Composable
private fun AppSettingsFloatingActionButton(
    isConfigApplied: Boolean,
    onClick: () -> Unit,
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = if (isConfigApplied) MaterialTheme.colorScheme.primary else BottomAppBarDefaults.bottomAppBarFabColor,
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
    ) {
        Icon(
            imageVector = if (isConfigApplied) GetoIcons.Refresh else GetoIcons.PlayArrow,
            contentDescription = null,
        )
    }
}

@Composable
private fun AppSettingsBottomAppBarActions(
    onAddConfigIconClick: () -> Unit,
    onSettingsSuggestIconClick: () -> Unit,
    onQuickSettingsTipClick: () -> Unit,
) {
    IconButton(onClick = onAddConfigIconClick) {
        Icon(
            GetoIcons.Add,
            contentDescription = null,
        )
    }

    IconButton(
        onClick = onSettingsSuggestIconClick,
    ) {
        Icon(
            imageVector = GetoIcons.SettingsSuggest,
            contentDescription = null,
        )
    }

    IconButton(onClick = onQuickSettingsTipClick) {
        Icon(
            imageVector = GetoIcons.Info,
            contentDescription = null,
        )
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier.size(100.dp),
            imageVector = GetoIcons.Android,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = title, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = subtitle, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SuccessState(
    modifier: Modifier = Modifier,
    isConfigApplied: Boolean,
    appSettingsUiState: AppSettingsUiState.Success,
    onCheckAppSetting: (AppSetting) -> Unit,
    onDeleteAppSettingsItem: (AppSetting) -> Unit,
    onEditAppSettingItem: (AppSetting) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        items(items = appSettingsUiState.appSettings, key = { it.id }) { appSettings ->
            AppSettingItem(
                isConfigApplied = isConfigApplied,
                appSetting = appSettings,
                onCheckedChange = { check ->
                    onCheckAppSetting(
                        appSettings.copy(enabled = check),
                    )
                },
                onDeleteClick = {
                    onDeleteAppSettingsItem(appSettings)
                },
                onEditClick = {
                    onEditAppSettingItem(appSettings)
                },
            )
        }
    }
}

@Composable
private fun LazyItemScope.AppSettingItem(
    modifier: Modifier = Modifier,
    isConfigApplied: Boolean,
    appSetting: AppSetting,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    val enabled = !isConfigApplied

    ListItem(
        modifier = modifier
            .animateItem()
            .alpha(if (isConfigApplied) 0.5f else 1f),
        headlineContent = {
            Text(
                text = appSetting.label,
            )
        },
        supportingContent = {
            Column(modifier = Modifier.padding(top = 1.dp)) {
                Text(
                    text = appSetting.settingType.getSettingTypeTitle(),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 15.sp),
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = appSetting.key,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        leadingContent = {
            Checkbox(
                checked = appSetting.enabled,
                enabled = enabled,
                onCheckedChange = onCheckedChange,
            )
        },
        trailingContent = {
            Row {
                IconButton(
                    onClick = onEditClick,
                    enabled = enabled,
                ) {
                    Icon(
                        imageVector = GetoIcons.Edit,
                        contentDescription = null,
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    enabled = enabled,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
    )
}

@Composable
private fun QuickSettingsTipDialog(onDismissRequest: () -> Unit) {
    DialogContainer(
        onDismissRequest = onDismissRequest,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.quick_settings_tip_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(12.dp))

                val tipMessage = stringResource(R.string.quick_settings_tip_message)

                val tipBold = stringResource(R.string.quick_settings_tip_bold)

                val tipText = remember(tipMessage, tipBold) {
                    buildAnnotatedString {
                        val start = tipMessage.indexOf(tipBold)
                        if (start >= 0) {
                            append(tipMessage.take(start))
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(tipBold)
                            }
                            append(tipMessage.substring(start + tipBold.length))
                        } else {
                            append(tipMessage)
                        }
                    }
                }

                Text(
                    text = tipText,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    modifier = Modifier.align(Alignment.End),
                    onClick = onDismissRequest,
                ) {
                    Text(text = stringResource(R.string.got_it))
                }
            }
        },
    )
}

@Composable
internal fun SettingType.getSettingTypeTitle() = when (this) {
    SettingType.SYSTEM -> stringResource(R.string.system)
    SettingType.SECURE -> stringResource(R.string.secure)
    SettingType.GLOBAL -> stringResource(R.string.global)
}
