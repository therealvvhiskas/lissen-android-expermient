package org.grakovne.lissen.persistence.preferences

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import org.grakovne.lissen.domain.Library
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LissenSharedPreferences @Inject constructor(@ApplicationContext context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)

    fun hasCredentials(): Boolean {
        val host = getHost()
        val username = getUsername()
        val token = getToken()

        return host != null && username != null && token != null
    }

    fun clearCredentials() {
        sharedPreferences.edit().apply {
            putString(KEY_HOST, null)
            putString(KEY_USERNAME, null)
            putString(KEY_TOKEN, null)
        }.apply()
    }

    fun saveHost(host: String) = sharedPreferences.edit().putString(KEY_HOST, host).apply()
    fun getHost(): String? = sharedPreferences.getString(KEY_HOST, null)


    fun getPreferredLibrary(): Library? {
        val id = getPreferredLibraryId() ?: return null
        val name = getPreferredLibraryName() ?: return null

        return Library(id, name)
    }

    fun savePreferredLibrary(library: Library) {
        saveActiveLibraryId(library.id)
        saveActiveLibraryName(library.title)
    }

    private fun saveActiveLibraryId(host: String) =
        sharedPreferences.edit().putString(KEY_PREFERRED_LIBRARY_ID, host).apply()

    private fun getPreferredLibraryId(): String? =
        sharedPreferences.getString(KEY_PREFERRED_LIBRARY_ID, null)

    private fun saveActiveLibraryName(host: String) =
        sharedPreferences.edit().putString(KEY_PREFERRED_LIBRARY_NAME, host).apply()

    private fun getPreferredLibraryName(): String? =
        sharedPreferences.getString(KEY_PREFERRED_LIBRARY_NAME, null)


    fun saveUsername(username: String) =
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply()

    fun getUsername(): String? = sharedPreferences.getString(KEY_USERNAME, null)

    fun saveToken(password: String) {
        val encrypted = encrypt(password)
        sharedPreferences.edit().putString(KEY_TOKEN, encrypted).apply()
    }

    fun getToken(): String? {
        val encrypted = sharedPreferences.getString(KEY_TOKEN, null) ?: return null
        return decrypt(encrypted)
    }

    companion object {

        private const val KEY_ALIAS = "secure_key_alias"
        private const val KEY_HOST = "host"
        private const val KEY_USERNAME = "username"
        private const val KEY_TOKEN = "token"

        private const val KEY_PREFERRED_LIBRARY_ID = "preferred_library_id"
        private const val KEY_PREFERRED_LIBRARY_NAME = "preferred_library_name"

        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        private fun getSecretKey(): SecretKey {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            keyStore.getKey(KEY_ALIAS, null)?.let {
                return it as SecretKey
            }

            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            keyGenerator.init(keyGenParameterSpec)
            return keyGenerator.generateKey()
        }

        private fun encrypt(data: String): String {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

            val cipherText = cipher.doFinal(data.toByteArray())
            val ivAndCipherText = cipher.iv + cipherText

            return Base64.encodeToString(ivAndCipherText, Base64.DEFAULT)
        }

        private fun decrypt(data: String): String {
            val decodedData = Base64.decode(data, Base64.DEFAULT)
            val iv = decodedData.sliceArray(0 until 12)
            val cipherText = decodedData.sliceArray(12 until decodedData.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            return String(cipher.doFinal(cipherText))
        }

    }
}
