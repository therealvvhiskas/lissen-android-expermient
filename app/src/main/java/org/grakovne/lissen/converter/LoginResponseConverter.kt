package org.grakovne.lissen.converter

import org.grakovne.lissen.client.audiobookshelf.model.LoginResponse
import org.grakovne.lissen.domain.UserAccount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginResponseConverter @Inject constructor() {

    fun apply(response: LoginResponse): UserAccount = UserAccount(
        token = response.user.token,
        preferredLibraryId = response.userDefaultLibraryId
    )
}