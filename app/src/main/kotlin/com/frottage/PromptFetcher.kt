package com.frottage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

suspend fun fetchPrompt(): String =
    withContext(Dispatchers.IO) {
        val jsonString = URL(Constants.PROMPT_URL).readText()
        val jsonObject = JSONObject(jsonString)
        jsonObject.getJSONObject("template").getString("prompt")
    }
