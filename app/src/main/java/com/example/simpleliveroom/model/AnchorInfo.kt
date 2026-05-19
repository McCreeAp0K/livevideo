package com.example.simpleliveroom.model

data class AnchorInfo(
    val created_at: String,
    val name: String,
    val avatar: String,
    val room_name: String,
    val follower_num: Int,
    val id: String
)