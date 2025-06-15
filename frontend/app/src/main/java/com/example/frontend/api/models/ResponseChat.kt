package com.example.frontend.api.models

data class ResponseChat(
    val content: List<ChatContent>,
    val page: PageInfo
)

data class ChatContent(
    val message: String,
    val id: String,
    val type: String,
    val createdAt: String
)

data class PageInfo(
    val size: Int,
    val number: Int,
    val totalElements: Int,
    val totalPages: Int
) 