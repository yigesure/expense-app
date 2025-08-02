package com.passwordmanager.elite.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.text.Charsets.UTF_8

/**
 * 加密管理器
 * 负责处理应用中的所有加密和解密操作
 * 使用Google Tink库提供企业级安全保障
 */
class CryptoManager(private val context: Context) {
    
    companion object {
        private const val KEYSET_NAME = "password_manager_keyset"
        private const val PREFERENCE_FILE = "password_manager_prefs"
        private const val MASTER_KEY_ALIAS = "PasswordManagerMasterKey"
        private const val SALT_KEY = "encryption_salt"
        private const val KEY_LENGTH = 32 // 256位密钥
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
    
    private var aead: Aead? = null
    private val secureRandom = SecureRandom()
    
    /**
     * 初始化加密系统
     */
    @Throws(GeneralSecurityException::class)
    fun initialize(masterPassword: String? = null) {
        try {
            // 使用Android Keystore管理密钥
            val keysetHandle = AndroidKeysetManager.Builder()
                .withSharedPref(context, KEYSET_NAME, PREFERENCE_FILE)
                .withKeyTemplate(KeyTemplates.AES256_GCM)
                .withMasterKeyUri("android-keystore://$MASTER_KEY_ALIAS")
                .build()
                .keysetHandle
            
            aead = keysetHandle.getPrimitive(Aead::class.java)
        } catch (e: Exception) {
            throw GeneralSecurityException("加密系统初始化失败", e)
        }
    }
    
    /**
     * 加密数据
     */
    @Throws(GeneralSecurityException::class)
    suspend fun encrypt(plaintext: String): String {
        return try {
            val aead = this.aead ?: throw GeneralSecurityException("加密系统未初始化")
            val ciphertext = aead.encrypt(plaintext.toByteArray(UTF_8), null)
            android.util.Base64.encodeToString(ciphertext, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            throw GeneralSecurityException("数据加密失败", e)
        }
    }
    
    /**
     * 解密数据
     */
    @Throws(GeneralSecurityException::class)
    suspend fun decrypt(ciphertext: String): String {
        return try {
            val aead = this.aead ?: throw GeneralSecurityException("加密系统未初始化")
            val encryptedData = android.util.Base64.decode(ciphertext, android.util.Base64.DEFAULT)
            val plaintext = aead.decrypt(encryptedData, null)
            String(plaintext, UTF_8)
        } catch (e: Exception) {
            throw GeneralSecurityException("数据解密失败", e)
        }
    }
    
    /**
     * 生成主密码哈希
     */
    fun hashMasterPassword(password: String, salt: ByteArray? = null): Pair<String, ByteArray> {
        val actualSalt = salt ?: generateSalt()
        
        // 使用PBKDF2进行密码哈希
        val spec = javax.crypto.spec.PBEKeySpec(
            password.toCharArray(),
            actualSalt,
            100000, // 迭代次数
            256 // 密钥长度
        )
        
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        
        return Pair(
            android.util.Base64.encodeToString(hash, android.util.Base64.DEFAULT),
            actualSalt
        )
    }
    
    /**
     * 验证主密码
     */
    fun verifyMasterPassword(password: String, storedHash: String, salt: ByteArray): Boolean {
        val (computedHash, _) = hashMasterPassword(password, salt)
        return computedHash == storedHash
    }
    
    /**
     * 生成随机盐值
     */
    private fun generateSalt(): ByteArray {
        val salt = ByteArray(32)
        secureRandom.nextBytes(salt)
        return salt
    }
    
    /**
     * 生成安全的随机密码
     */
    fun generateSecurePassword(
        length: Int = 16,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true,
        excludeAmbiguous: Boolean = true
    ): String {
        val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercase = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        val ambiguous = "0O1lI|"
        
        var charset = ""
        val requiredChars = mutableListOf<Char>()
        
        if (includeUppercase) {
            charset += uppercase
            requiredChars.add(uppercase.random(secureRandom))
        }
        if (includeLowercase) {
            charset += lowercase
            requiredChars.add(lowercase.random(secureRandom))
        }
        if (includeNumbers) {
            charset += numbers
            requiredChars.add(numbers.random(secureRandom))
        }
        if (includeSymbols) {
            charset += symbols
            requiredChars.add(symbols.random(secureRandom))
        }
        
        if (excludeAmbiguous) {
            charset = charset.filterNot { it in ambiguous }.joinToString("")
        }
        
        if (charset.isEmpty()) {
            throw IllegalArgumentException("至少需要选择一种字符类型")
        }
        
        // 生成剩余的随机字符
        val remainingLength = length - requiredChars.size
        val randomChars = (0 until remainingLength).map {
            charset.random(secureRandom)
        }
        
        // 合并并打乱字符顺序
        val allChars = (requiredChars + randomChars).toMutableList()
        allChars.shuffle(secureRandom)
        
        return allChars.joinToString("")
    }
    
    /**
     * 计算密码强度分数
     */
    fun calculatePasswordStrength(password: String): Int {
        var score = 0
        
        // 长度评分
        score += when {
            password.length >= 12 -> 25
            password.length >= 8 -> 15
            password.length >= 6 -> 10
            else -> 0
        }
        
        // 字符类型评分
        if (password.any { it.isUpperCase() }) score += 15
        if (password.any { it.isLowerCase() }) score += 15
        if (password.any { it.isDigit() }) score += 15
        if (password.any { !it.isLetterOrDigit() }) score += 15
        
        // 复杂度评分
        val uniqueChars = password.toSet().size
        score += when {
            uniqueChars >= password.length * 0.8 -> 15
            uniqueChars >= password.length * 0.6 -> 10
            uniqueChars >= password.length * 0.4 -> 5
            else -> 0
        }
        
        return score.coerceIn(0, 100)
    }
    
    /**
     * 生成设备唯一标识符
     */
    fun generateDeviceId(): String {
        val deviceId = sharedPreferences.getString("device_id", null)
        if (deviceId != null) {
            return deviceId
        }
        
        val newDeviceId = generateSecurePassword(32, excludeAmbiguous = false)
        sharedPreferences.edit().putString("device_id", newDeviceId).apply()
        return newDeviceId
    }
    
    /**
     * 清除所有加密数据
     */
    fun clearAllData() {
        try {
            // 清除SharedPreferences中的加密数据
            sharedPreferences.edit().clear().apply()
            
            // 清除Android Keystore中的密钥
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                keyStore.deleteEntry(MASTER_KEY_ALIAS)
            }
            
            aead = null
        } catch (e: Exception) {
            // 记录错误但不抛出异常
            e.printStackTrace()
        }
    }
    
    /**
     * 检查加密系统是否已初始化
     */
    fun isInitialized(): Boolean {
        return aead != null
    }
}

/**
 * 扩展函数：为SecureRandom添加随机选择功能
 */
private fun String.random(random: SecureRandom): Char {
    return this[random.nextInt(this.length)]
}