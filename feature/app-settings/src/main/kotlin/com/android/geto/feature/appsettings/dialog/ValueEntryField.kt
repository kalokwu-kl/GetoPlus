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

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.android.geto.designsystem.icon.GetoIcons
import com.android.geto.feature.appsettings.R

internal enum class ValueEntry {
    VALUE,
    EMPTY,
}

internal fun String.toValueEntry(): ValueEntry = if (isEmpty()) ValueEntry.EMPTY else ValueEntry.VALUE

@Composable
internal fun ValueEntryField(
    modifier: Modifier = Modifier,
    label: String,
    isError: Boolean,
    errorText: String,
    text: String,
    entry: ValueEntry,
    onTextChange: (String) -> Unit,
    onEntryChange: (ValueEntry) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    var focused by remember { mutableStateOf(false) }

    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        focused -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, borderColor, RoundedCornerShape(4.dp)),
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { menuExpanded = true }
                        .padding(start = 16.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = when (entry) {
                            ValueEntry.VALUE -> stringResource(R.string.mode_value)
                            ValueEntry.EMPTY -> stringResource(R.string.empty_value)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = GetoIcons.ArrowDropDown,
                        contentDescription = null,
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    ValueEntry.entries.forEach { valueEntry ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = when (valueEntry) {
                                            ValueEntry.VALUE -> stringResource(R.string.mode_value)
                                            ValueEntry.EMPTY -> stringResource(R.string.empty_value)
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text = when (valueEntry) {
                                            ValueEntry.VALUE -> stringResource(R.string.mode_value_hint)
                                            ValueEntry.EMPTY -> stringResource(R.string.empty_value_hint)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                            trailingIcon = {
                                if (entry == valueEntry) {
                                    Icon(
                                        imageVector = GetoIcons.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            },
                            onClick = {
                                onEntryChange(valueEntry)
                                menuExpanded = false
                            },
                        )
                    }
                }
            }

            HorizontalDivider()

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focused = it.isFocused },
                value = text,
                onValueChange = onTextChange,
                enabled = entry == ValueEntry.VALUE,
                minLines = 1,
                placeholder = {
                    if (entry == ValueEntry.EMPTY) {
                        Text(text = "\"\"")
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
            )
        }

        if (isError) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
