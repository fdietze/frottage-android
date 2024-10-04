package com.frottage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object PromptFetcher {
    private const val PROMPT_URL = "https://fdietze.github.io/frottage/wallpapers/mobile.json"

    suspend fun fetchPrompt(): String = withContext(Dispatchers.IO) {
        val jsonString = URL(PROMPT_URL).readText()
        val jsonObject = JSONObject(jsonString)
        jsonObject.getJSONObject("template").getString("prompt")
    }
}
