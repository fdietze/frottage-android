package com.frottage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

@Composable
fun WallpaperScreen() {
    var isFullScreen by remember { mutableStateOf(false) }

    if (isFullScreen) {
        FullScreenImage(onDismiss = { isFullScreen = false })
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.7f)
                    .aspectRatio(9f / 16f)
                    .heightIn(max = 200.dp)
            ) {
                CurrentWallpaper(
                    modifier = Modifier.fillMaxSize(),
                    onClick = { isFullScreen = true },
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WallpaperScheduleSwitch()
                NextUpdateTime()
            }
        }
    }
}

