package com.frottage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage

@Composable
fun CurrentWallpaper(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val context = LocalContext.current
    AsyncImage(
        model = SettingsManager.getLockScreenUrl(context),
        contentDescription = "Current Wallpaper",
        modifier =
            modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
        contentScale = contentScale,
    )
}

@Composable
fun FullScreenImage(onDismiss: () -> Unit) {
    CurrentWallpaper(
        modifier = Modifier.fillMaxSize(),
        onClick = onDismiss,
        contentScale = ContentScale.Crop,
    )
}
