package com.example.simpleliveroom.model

data class CommentMessage(
    val created_at: String,
    val name: String,
    val avatar: String,
    val comment: String,
    val id: String
)