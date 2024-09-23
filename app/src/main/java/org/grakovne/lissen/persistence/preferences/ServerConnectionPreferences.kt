import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import org.grakovne.lissen.LissenApplication
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

        private const val PREF_PASSWORD = "password"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

    fun hasCredentials(): Boolean {
        val host = getHost()
        val username = getUsername()

        return host != null && username != null
    }

    fun saveHost(host: String) {
        sharedPreferences.edit().putString(KEY_HOST, host).apply()
    }

    fun getHost(): String? {
        return sharedPreferences.getString(KEY_HOST, null)
    }

    fun saveUsername(username: String) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    fun saveToken(password: String) {
        val encryptedPassword = encrypt(password)
        sharedPreferences.edit().putString(PREF_PASSWORD, encryptedPassword).apply()
    }

    fun getToken(): String? {
        val encryptedPassword = sharedPreferences.getString(KEY_TOKEN, null) ?: return null
        return decrypt(encryptedPassword)
    }

    fun savePassword(password: String) {
        val encryptedPassword = encrypt(password)
        sharedPreferences.edit().putString(KEY_TOKEN, encryptedPassword).apply()
    }

    fun getPassword(): String? {
        val encryptedPassword = sharedPreferences.getString(PREF_PASSWORD, null) ?: return null
        return decrypt(encryptedPassword)
    }

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
