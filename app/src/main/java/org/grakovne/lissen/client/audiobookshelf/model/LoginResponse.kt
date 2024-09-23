package org.grakovne.lissen.client.audiobookshelf.model

data class LoginResponse(
    val user: User
)

data class User(
    val id: String,
    val username: String,
    val email: String?,
    val type: String,
    val token: String,
    val isActive: Boolean,
    val isLocked: Boolean,

    )