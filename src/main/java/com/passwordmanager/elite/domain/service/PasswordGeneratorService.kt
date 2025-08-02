package com.passwordmanager.elite.domain.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import kotlin.math.ln
import kotlin.math.pow

/**
 * 密码生成器服务（辅助功能）
 * 为用户提供可选的密码生成建议
 */
class PasswordGeneratorService {
    
    companion object {
        private const val LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz"
        private const val UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val DIGIT_CHARS = "0123456789"
        private const val SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        
        val PRESET_CONFIGS = mapOf(
            "强密码" to PasswordConfig(16, true, true, true, true),
            "中等密码" to PasswordConfig(12, true, true, true, false),
            "简单密码" to PasswordConfig(8, true, true, true, false)
        )
    }
    
    private val secureRandom = SecureRandom()
    
    data class PasswordConfig(
        val length: Int = 12,
        val includeLowercase: Boolean = true,
        val includeUppercase: Boolean = true,
        val includeDigits: Boolean = true,
        val includeSpecialChars: Boolean = false
    )
    
    data class GenerationResult(
        val password: String,
        val strength: Int,
        val config: PasswordConfig
    )
    
    suspend fun generatePassword(config: PasswordConfig): GenerationResult = withContext(Dispatchers.Default) {
        val charset = buildCharset(config)
        val password = generateRandomPassword(config.length, charset)
        val strength = calculatePasswordStrength(password)
        
        GenerationResult(password, strength, config)
    }
    
    private fun buildCharset(config: PasswordConfig): String {
        val charset = StringBuilder()
        if (config.includeLowercase) charset.append(LOWERCASE_CHARS)
        if (config.includeUppercase) charset.append(UPPERCASE_CHARS)
        if (config.includeDigits) charset.append(DIGIT_CHARS)
        if (config.includeSpecialChars) charset.append(SPECIAL_CHARS)
        return charset.toString()
    }
    
    private fun generateRandomPassword(length: Int, charset: String): String {
        return (1..length)
            .map { charset[secureRandom.nextInt(charset.length)] }
            .joinToString("")
    }
    
    private fun calculatePasswordStrength(password: String): Int {
        var score = 0
        if (password.length >= 8) score += 25
        if (password.any { it.isLowerCase() }) score += 25
        if (password.any { it.isUpperCase() }) score += 25
        if (password.any { it.isDigit() }) score += 25
        if (password.any { !it.isLetterOrDigit() }) score += 25
        return score.coerceIn(0, 100)
    }
}