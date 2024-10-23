package com.frottage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onSettingsSaved: () -> Unit) {
    val context = LocalContext.current
    var lockScreenUrl by remember {
        mutableStateOf(SettingsManager.getLockScreenUrl(context))
    }
    var homeScreenUrl by remember {
        mutableStateOf(SettingsManager.getHomeScreenUrl(context))
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Custom Wallpaper URLs", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = lockScreenUrl,
            onValueChange = { lockScreenUrl = it },
            label = { Text("Lock Screen URL") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = homeScreenUrl,
            onValueChange = { homeScreenUrl = it },
            label = { Text("Home Screen URL") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OutlinedButton(
                onClick = {
                    lockScreenUrl = Constants.DEFAULT_LOCK_SCREEN_WALLPAPER_URL
                    homeScreenUrl = Constants.DEFAULT_HOME_SCREEN_WALLPAPER_URL
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Reset to Defaults")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    SettingsManager.setLockScreenUrl(context, lockScreenUrl.trim())
                    SettingsManager.setHomeScreenUrl(context, homeScreenUrl.trim())
                    onSettingsSaved()
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Save")
            }
        }
    }
}
