package com.frottage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun CurrentWallpaper(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val context = LocalContext.current
    val url = SettingsManager.getLockScreenUrl(context)
    val imageCacheKey = currentImageCacheKey(url)
    AsyncImage(
        model =

            ImageRequest
                .Builder(context)
                .data(url)
                .diskCacheKey(imageCacheKey)
                .memoryCacheKey(imageCacheKey)
                .allowHardware(false) // Disable hardware bitmaps
                .build(),
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
