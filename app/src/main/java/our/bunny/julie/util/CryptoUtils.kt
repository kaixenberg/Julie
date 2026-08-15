package our.bunny.julie.util

import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.AEADBadTagException

object CryptoUtils {

    private const val MAGIC_HEADER = "JULIE_ENC1"
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 12
    private const val TAG_LENGTH_BIT = 128
    private const val ITERATION_COUNT = 100000
    private const val KEY_LENGTH = 256
    private const val ENCRYPTION_ALGO = "AES/GCM/NoPadding"

    fun encryptBackup(payload: String, passphrase: CharArray, outputStream: OutputStream) {
        val secureRandom = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)

        val iv = ByteArray(IV_LENGTH)
        secureRandom.nextBytes(iv)

        val secretKey = deriveKey(passphrase, salt)
        val cipher = Cipher.getInstance(ENCRYPTION_ALGO)
        val gcmParameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)

        // Write header
        outputStream.write(MAGIC_HEADER.toByteArray(Charsets.UTF_8))
        outputStream.write(salt)
        outputStream.write(iv)

        // Write encrypted payload
        CipherOutputStream(outputStream, cipher).use { cipherOutputStream ->
            cipherOutputStream.write(payload.toByteArray(Charsets.UTF_8))
            cipherOutputStream.flush()
        }
    }

    @Throws(Exception::class)
    fun decryptBackup(inputStream: InputStream, passphrase: CharArray): String {
        val dataInputStream = java.io.DataInputStream(inputStream)
        
        // Read header
        val magicBytes = ByteArray(MAGIC_HEADER.length)
        try {
            dataInputStream.readFully(magicBytes)
        } catch (e: Exception) {
            throw IllegalArgumentException("Not an encrypted Julie backup file or corrupted header")
        }
        if (String(magicBytes, Charsets.UTF_8) != MAGIC_HEADER) {
            throw IllegalArgumentException("Not an encrypted Julie backup file or corrupted header")
        }

        val salt = ByteArray(SALT_LENGTH)
        try {
            dataInputStream.readFully(salt)
        } catch (e: Exception) {
            throw IllegalArgumentException("Corrupted backup file: missing salt")
        }

        val iv = ByteArray(IV_LENGTH)
        try {
            dataInputStream.readFully(iv)
        } catch (e: Exception) {
            throw IllegalArgumentException("Corrupted backup file: missing IV")
        }

        val secretKey = deriveKey(passphrase, salt)
        val cipher = Cipher.getInstance(ENCRYPTION_ALGO)
        val gcmParameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
        } catch (e: Exception) {
            throw Exception("Failed to initialize decryption: ${e.message}", e)
        }

        // We read all remaining bytes into memory and decrypt at once, 
        // since GCM auth tag check requires the whole stream before yielding valid data.
        val encryptedData = inputStream.readBytes()
        
        return try {
            val decryptedBytes = cipher.doFinal(encryptedData)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: AEADBadTagException) {
            throw Exception("Incorrect passphrase or corrupted backup file", e)
        } catch (e: Exception) {
            throw Exception("Decryption failed: ${e.message}", e)
        }
    }

    fun isEncryptedBackup(inputStream: InputStream): Boolean {
        if (!inputStream.markSupported()) {
            throw IllegalArgumentException("InputStream must support mark/reset")
        }
        
        // We only need to read the header
        inputStream.mark(MAGIC_HEADER.length + 1)
        val dataInputStream = java.io.DataInputStream(inputStream)
        val magicBytes = ByteArray(MAGIC_HEADER.length)
        val isEncrypted = try {
            dataInputStream.readFully(magicBytes)
            String(magicBytes, Charsets.UTF_8) == MAGIC_HEADER
        } catch (e: Exception) {
            false
        }
        inputStream.reset()

        return isEncrypted
    }

    private fun deriveKey(passphrase: CharArray, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase, salt, ITERATION_COUNT, KEY_LENGTH)
        val secretKey = factory.generateSecret(spec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }
}
