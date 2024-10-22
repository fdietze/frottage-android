package com.frottage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun WallpaperScreen(
    prompt: String?,
    key: Any? = null,
) {
    var isFullScreen by remember { mutableStateOf(false) }

    if (isFullScreen) {
        FullScreenImage(onDismiss = { isFullScreen = false })
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(0.7f)
                        .aspectRatio(9f / 16f)
                        .heightIn(max = 200.dp),
            ) {
                key(key) {
                    CurrentWallpaper(
                        modifier = Modifier.fillMaxSize(),
                        onClick = { isFullScreen = true },
                        contentScale = ContentScale.Fit,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            prompt?.let {
                Text(
                    text = "Prompt: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val isScheduleEnabled = WallpaperScheduleSwitch()
                if (isScheduleEnabled) {
                    NextUpdateTime(key = key)
                }
            }
        }
    }
}
