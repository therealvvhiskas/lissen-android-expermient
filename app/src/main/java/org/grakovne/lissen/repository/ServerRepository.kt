package org.grakovne.lissen.repository

import javax.inject.Inject

class ServerRepository @Inject constructor() {

    suspend fun fetchToken(
        host: String?,
        username: String?,
        password: String?
    ): Result<String> {


        return Result.success("token")
    }
}