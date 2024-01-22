package hr.foi.sis.passwordium.managers

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import hr.foi.sis.passwordium.models.Account
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptingManager {
    @SuppressLint("GetInstance")
    @Throws(Exception::class)
    fun decryptor(context: Context, account: Account): String {
        val key = SecretKeySpec(generateKey(context, account).encoded, "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decryptedPassword = cipher.doFinal(Base64.decode(account.password, Base64.DEFAULT))
        return String(decryptedPassword, StandardCharsets.UTF_8)
    }

    @SuppressLint("GetInstance")
    @Throws(Exception::class)
    fun encryptor(context: Context, account: Account): String {
        val key = SecretKeySpec(generateKey(context, account).encoded, "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedPassword = cipher.doFinal(account.password.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(encryptedPassword, Base64.DEFAULT)
    }

    @Throws(Exception::class)
    private fun generateKey(context: Context, account: Account): SecretKey {
        val salt = account.name + getUsername() + "AJCKECHJEKJSJDSJRTZH"
        val spec = PBEKeySpec(getPassword(context)?.toCharArray(), salt.toByteArray(StandardCharsets.UTF_8), 10000, 128)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

     private fun getPassword(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("password", Context.MODE_PRIVATE)
        return sharedPreferences.getString("password", null)
    }

    private fun getUsername(): String {
        return JwtManager.giveUserName();
    }

}