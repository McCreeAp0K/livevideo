package com.example.simpleliveroom.model

data class CommentBatchData(
    val room_id: String,
    val messages: List<CommentMessage>,
    val updated_at: String
)