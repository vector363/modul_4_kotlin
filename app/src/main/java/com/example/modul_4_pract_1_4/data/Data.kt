package com.example.modul_4_pract_1_4.data

data class SocialPost(
    val id: Int,
    val userid: Int,
    val title: String,
    val body: String,
    val avatarurl: String
)

data class Comment(
    val postid: Int,
    val id: Int,
    val name: String,
    val body: String
)

data class PostWithData(
    val post: SocialPost,
    val avatarColor: String? = null,
    val comments: List<Comment> = emptyList(),
    val avatarState: LoadState = LoadState.LOADING,
    val commentsState: LoadState = LoadState.LOADING
)

enum class LoadState {
    LOADING, SUCCESS, ERROR
}