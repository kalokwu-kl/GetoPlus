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
package com.android.geto.feature.appsettings.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.android.geto.designsystem.component.DialogContainer
import com.android.geto.domain.model.AppSetting
import com.android.geto.domain.model.SecureSetting
import com.android.geto.domain.model.SettingType
import com.android.geto.feature.appsettings.R
import com.android.geto.feature.appsettings.getSettingTypeTitle
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(FlowPreview::class)
@Composable
internal fun AppSettingDialog(
    modifier: Modifier = Modifier,
    componentName: String,
    secureSettings: List<SecureSetting>,
    appSetting: AppSetting? = null,
    onAddAppSetting: (AppSetting) -> Unit,
    onUpdateAppSetting: ((AppSetting) -> Unit)? = null,
    onDismissRequest: () -> Unit,
    onGetSecureSettingsByName: (
        settingType: SettingType,
        text: String,
    ) -> Unit,
) {
    val isEditing = appSetting != null

    var selectedRadioOptionIndex by remember(appSetting) { mutableIntStateOf(appSetting?.settingType?.ordinal ?: 0) }

    var label by remember(appSetting) { mutableStateOf(appSetting?.label ?: "") }

    var pickedKey by remember(appSetting) { mutableStateOf(appSetting?.key) }

    var pickerExpanded by remember { mutableStateOf(false) }

    var query by remember(appSetting) { mutableStateOf("") }

    var launchText by remember(appSetting) { mutableStateOf(appSetting?.valueOnLaunch ?: "") }

    var launchEntry by remember(appSetting) {
        mutableStateOf(
            if (appSetting == null) ValueEntry.VALUE else appSetting.valueOnLaunch.toValueEntry(),
        )
    }

    var revertText by remember(appSetting) { mutableStateOf(appSetting?.valueOnRevert ?: "") }

    var revertEntry by remember(appSetting) {
        mutableStateOf(
            if (appSetting == null) ValueEntry.VALUE else appSetting.valueOnRevert.toValueEntry(),
        )
    }

    var showLabelError by remember { mutableStateOf(false) }

    var showKeyError by remember { mutableStateOf(false) }

    var showLaunchError by remember { mutableStateOf(false) }

    var showRevertError by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        snapshotFlow { query }.debounce(300).distinctUntilChanged().collect {
            onGetSecureSettingsByName(
                SettingType.entries[selectedRadioOptionIndex],
                it,
            )
        }
    }

    LaunchedEffect(key1 = Unit) {
        snapshotFlow { selectedRadioOptionIndex }.distinctUntilChanged().collect {
            onGetSecureSettingsByName(
                SettingType.entries[it],
                query,
            )
        }
    }

    DialogContainer(
        modifier = modifier,
        dismissOnClickOutside = false,
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                text = if (isEditing) {
                    stringResource(R.string.edit_setting)
                } else {
                    stringResource(R.string.add_app_setting)
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = label,
                onValueChange = {
                    label = it
                    showLabelError = false
                },
                label = {
                    Text(text = stringResource(R.string.setting_label))
                },
                isError = showLabelError,
                supportingText = if (showLabelError) {
                    {
                        Text(text = stringResource(R.string.setting_label_is_blank))
                    }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.setting_type),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            AppSettingDialogRadioButtonGroup(
                selected = selectedRadioOptionIndex,
                onSelect = {
                    selectedRadioOptionIndex = it
                    pickedKey = null
                    showKeyError = false
                    query = ""
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingKeyPicker(
                pickedKey = pickedKey,
                results = secureSettings,
                query = query,
                expanded = pickerExpanded,
                isError = showKeyError,
                onExpandedChange = { pickerExpanded = it },
                onQueryChange = { query = it },
                onPick = { secureSetting ->
                    val name = secureSetting.name
                    if (name != null) {
                        pickedKey = name
                        showKeyError = false

                        val value = secureSetting.value ?: ""
                        revertText = value
                        revertEntry = value.toValueEntry()
                        showRevertError = false
                    }
                    pickerExpanded = false
                },
                onClearSelection = { pickedKey = null },
            )

            Spacer(modifier = Modifier.height(16.dp))

            ValueEntryField(
                label = stringResource(R.string.setting_value_on_launch),
                isError = showLaunchError,
                errorText = stringResource(R.string.setting_value_on_launch_is_blank),
                text = launchText,
                entry = launchEntry,
                onTextChange = {
                    launchText = it
                    showLaunchError = false
                },
                onEntryChange = {
                    launchEntry = it
                    if (it != ValueEntry.VALUE) {
                        launchText = ""
                    }
                    showLaunchError = false
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            ValueEntryField(
                label = stringResource(R.string.setting_value_on_revert),
                isError = showRevertError,
                errorText = stringResource(R.string.setting_value_on_revert_is_blank),
                text = revertText,
                entry = revertEntry,
                onTextChange = {
                    revertText = it
                    showRevertError = false
                },
                onEntryChange = {
                    revertEntry = it
                    if (it != ValueEntry.VALUE) {
                        revertText = ""
                    }
                    showRevertError = false
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppSettingDialogButtons(
                isEditing = isEditing,
                onCancelClick = onDismissRequest,
                onSaveClick = {
                    showLabelError = label.isBlank()
                    showKeyError = pickedKey == null
                    showLaunchError = launchEntry == ValueEntry.VALUE && launchText.isBlank()
                    showRevertError = revertEntry == ValueEntry.VALUE && revertText.isBlank()

                    val key = pickedKey

                    if (!showLabelError && key != null && !showLaunchError && !showRevertError) {
                        val newAppSetting = AppSetting(
                            id = appSetting?.id ?: 0,
                            enabled = appSetting?.enabled ?: true,
                            settingType = SettingType.entries[selectedRadioOptionIndex],
                            componentName = componentName,
                            label = label,
                            key = key,
                            valueOnLaunch = when (launchEntry) {
                                ValueEntry.VALUE -> launchText
                                ValueEntry.EMPTY -> ""
                            },
                            valueOnRevert = when (revertEntry) {
                                ValueEntry.VALUE -> revertText
                                ValueEntry.EMPTY -> ""
                            },
                        )

                        if (isEditing) {
                            onUpdateAppSetting?.invoke(newAppSetting)
                        } else {
                            onAddAppSetting(newAppSetting)
                        }

                        onDismissRequest()
                    }
                },
            )
        }
    }
}

@Composable
private fun AppSettingDialogRadioButtonGroup(
    modifier: Modifier = Modifier,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth(),
    ) {
        SettingType.entries.forEachIndexed { index, settingType ->
            SegmentedButton(
                selected = index == selected,
                onClick = { onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = SettingType.entries.size,
                ),
            ) {
                Text(text = settingType.getSettingTypeTitle())
            }
        }
    }
}

@Composable
private fun AppSettingDialogButtons(
    modifier: Modifier = Modifier,
    isEditing: Boolean = false,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
    ) {
        TextButton(onClick = onCancelClick) {
            Text(text = stringResource(R.string.cancel))
        }
        Button(onClick = onSaveClick) {
            Text(text = stringResource(if (isEditing) R.string.save else R.string.add))
        }
    }
}
