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
package com.android.geto.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun GetoSettingsDrawer(
    panel: @Composable (close: () -> Unit) -> Unit,
    content: @Composable (open: () -> Unit) -> Unit,
) {
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val panelWidth = (maxWidth * 0.95f).coerceAtMost(450.dp)
        val targetOffset = with(LocalDensity.current) { panelWidth.toPx() }

        val openOffset = remember { Animatable(0f) }

        LaunchedEffect(targetOffset) {
            if (openOffset.value > targetOffset) {
                openOffset.snapTo(targetOffset)
            }
        }

        val open: () -> Unit = {
            scope.launch { openOffset.animateTo(targetOffset, tween(300)) }
        }

        val close: () -> Unit = {
            scope.launch { openOffset.animateTo(0f, tween(300)) }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            content(open)
        }

        if (openOffset.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.5f * (openOffset.value / targetOffset) }
                    .background(Color.Black)
                    .clickable(onClick = close),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(panelWidth)
                .graphicsLayer { translationX = targetOffset - openOffset.value }
                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
        ) {
            panel(close)
        }

        BackHandler(enabled = openOffset.value > 0f) {
            close()
        }
    }
}
