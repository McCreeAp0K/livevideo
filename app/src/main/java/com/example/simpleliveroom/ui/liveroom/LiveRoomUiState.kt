package com.example.simpleliveroom.ui.liveroom

import com.example.simpleliveroom.model.CommentMessage

data class LiveRoomUiState(
    val anchorName: String = "",
    val anchorAvatarUrl: String = "",
    val followCountText: String = "",
    val viewerCountText: String = "0 在线",
    val comments: List<CommentMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)