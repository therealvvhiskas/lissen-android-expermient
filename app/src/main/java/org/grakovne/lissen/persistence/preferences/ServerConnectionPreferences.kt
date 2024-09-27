import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import org.grakovne.lissen.LissenApplication
import org.grakovne.lissen.domain.Library
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerConnectionPreferences @Inject constructor(@ApplicationContext context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)

    fun hasCredentials(): Boolean {
        val host = getHost()
        val username = getUsername()

        return host != null && username != null
    }

    fun clearCredentials() {
        sharedPreferences.edit().putString(KEY_HOST, null).apply()
        sharedPreferences.edit().putString(KEY_USERNAME, null).apply()
        sharedPreferences.edit().putString(KEY_TOKEN, null).apply()
    }

    fun saveHost(host: String) = sharedPreferences.edit().putString(KEY_HOST, host).apply()
    fun getHost(): String? = sharedPreferences.getString(KEY_HOST, null)


    fun getActiveLibrary(): Library? {
        val id = getActiveLibraryId() ?: return null
        val name = getActiveLibraryName() ?: return null

        return Library(id, name)
    }

    fun saveActiveLibrary(id: String, name: String) {
        saveActiveLibraryId(id)
        saveActiveLibraryName(name)
    }

    private fun saveActiveLibraryId(host: String) = sharedPreferences.edit().putString(KEY_ACTIVE_LIBRARY_ID, host).apply()
    private fun getActiveLibraryId(): String? = sharedPreferences.getString(KEY_ACTIVE_LIBRARY_ID, null)

    private fun saveActiveLibraryName(host: String) = sharedPreferences.edit().putString(KEY_ACTIVE_LIBRARY_NAME, host).apply()
    private fun getActiveLibraryName(): String? = sharedPreferences.getString(KEY_ACTIVE_LIBRARY_NAME, null)


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
        @Volatile
        private var instance: ServerConnectionPreferences? = null

        fun getInstance(): ServerConnectionPreferences {
            return instance ?: synchronized(this) {
                instance ?: ServerConnectionPreferences(LissenApplication.appContext).also {
                    instance = it
                }
            }
        }

        private const val KEY_ALIAS = "secure_key_alias"
        private const val KEY_HOST = "host"
        private const val KEY_USERNAME = "username"
        private const val KEY_TOKEN = "token"

        private const val KEY_ACTIVE_LIBRARY_ID = "active_library_id"
        private const val KEY_ACTIVE_LIBRARY_NAME = "active_library_name"

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
