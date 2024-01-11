package hr.foi.sis.passwordium.managers

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.*
import java.security.spec.X509EncodedKeySpec
import java.util.*

object BiometricManager {

    private const val KEYSTORE_ALIAS = "SecureKeyStore"
    private const val PROVIDER_BC = "BC"

    init {
        if (Security.getProvider(PROVIDER_BC) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun getKeyPair(): KeyPair? {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val aliases: Enumeration<String> = ks.aliases()
        val keyPair: KeyPair?

        if (aliases.toList().firstOrNull { it == KEYSTORE_ALIAS } == null) {
            val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                PROVIDER_BC
            )
            val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).run {
                setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                build()
            }
            kpg.initialize(parameterSpec)

            keyPair = kpg.generateKeyPair()
        } else {
            val entry = ks.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.PrivateKeyEntry
            keyPair = KeyPair(entry?.certificate?.publicKey, entry?.privateKey)
        }
        return keyPair
    }

    fun getPublicKey(): String? {
        val keyPair = getKeyPair()
        val publicKey = keyPair?.public ?: return null
        val publicKeyBytes = publicKey.encoded
        val encodedPublicKey = Base64.encodeToString(publicKeyBytes, Base64.DEFAULT)
        return encodedPublicKey
    }

    fun getPrivateKey(): PrivateKey? {
        val keyPair = getKeyPair()
        return keyPair?.private
    }

    fun signDataWithPrivateKey(data: String): String? {
        val privateKey = getPrivateKey()
        return if (privateKey != null) {
            try {
                val signature = Signature.getInstance("SHA256withECDSA")
                signature.initSign(privateKey)
                signature.update(data.toByteArray())
                val signatureBytes = signature.sign()
                Base64.encodeToString(signatureBytes, Base64.DEFAULT)
            } catch (e: Exception) {
                Log.e("SigningError", "Error signing data with private key", e)
                null
            }
        } else {
            null
        }
    }

    fun verifySignatureWithPublicKey(data: String, signature: String): Boolean {
        val publicKey = getPublicKey()
        return if (publicKey != null) {
            try {
                val keyFactory = KeyFactory.getInstance("EC")
                val keySpec = X509EncodedKeySpec(Base64.decode(publicKey, Base64.DEFAULT))
                val decodedPublicKey = keyFactory.generatePublic(keySpec)
                val verifySignature = Signature.getInstance("SHA256withECDSA")
                verifySignature.initVerify(decodedPublicKey)
                verifySignature.update(data.toByteArray())
                verifySignature.verify(Base64.decode(signature, Base64.DEFAULT))
            } catch (e: Exception) {
                Log.e("VerificationError", "Error verifying signature with public key", e)
                false
            }
        } else {
            false
        }
    }
}
