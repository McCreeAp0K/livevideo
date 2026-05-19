package com.example.simpleliveroom.model

import com.google.gson.JsonObject

data class BaseWsMessage(
    val type: String,
    val data: JsonObject
)