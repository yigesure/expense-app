package com.passwordmanager.elite.domain.service

import com.passwordmanager.elite.data.model.PasswordAnalysis
import com.passwordmanager.elite.data.model.PasswordEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import kotlin.math.ln
import kotlin.math.pow

/**
 * 密码分析服务
 * 负责分析密码强度、检测重复使用、评估安全风险等
 */
class PasswordAnalysisService {
    
    companion object {
        // 常见密码列表（部分示例）
        private val COMMON_PASSWORDS = setOf(
            "123456", "password", "123456789", "12345678", "12345",
            "1234567", "1234567890", "qwerty", "abc123", "111111",
            "123123", "admin", "letmein", "welcome", "monkey",
            "password123", "qwerty123", "123qwe", "000000", "iloveyou"
        )
        
        // 键盘模式
        private val KEYBOARD_PATTERNS = listOf(
            "qwerty", "asdf", "zxcv", "1234", "abcd"
        )
        
        // 常见替换字符
        private val COMMON_SUBSTITUTIONS = mapOf(
            'a' to '4', 'e' to '3', 'i' to '1', 'o' to '0', 's' to '5', 't' to '7'
        )
    }
    
    /**
     * 分析单个密码
     */
    suspend fun analyzePassword(
        password: String,
        allPasswords: List<PasswordEntry> = emptyList()
    ): PasswordAnalysis = withContext(Dispatchers.Default) {
        
        val strengthScore = calculateStrengthScore(password)
        val isCommon = isCommonPassword(password)
        val isReused = checkPasswordReuse(password, allPasswords)
        val patterns = detectPatterns(password)
        val entropy = calculateEntropy(password)
        val estimatedCrackTime = estimateCrackTime(password)
        val breachCount = checkDataBreaches(password)
        
        PasswordAnalysis(
            passwordHash = hashPassword(password),
            strengthScore = strengthScore,
            entropy = entropy,
            estimatedCrackTimeSeconds = estimatedCrackTime,
            isCommon = isCommon,
            isReused = isReused,
            breachCount = breachCount,
            detectedPatterns = patterns,
            suggestions = generateSuggestions(password, patterns, isCommon, isReused),
            analyzedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * 计算密码强度评分 (0-100)
     */
    private fun calculateStrengthScore(password: String): Int {
        var score = 0
        
        // 长度评分 (最多30分)
        score += when {
            password.length >= 12 -> 30
            password.length >= 8 -> 20
            password.length >= 6 -> 10
            else -> 0
        }
        
        // 字符类型多样性 (最多40分)
        var typeCount = 0
        if (password.any { it.isLowerCase() }) typeCount++
        if (password.any { it.isUpperCase() }) typeCount++
        if (password.any { it.isDigit() }) typeCount++
        if (password.any { !it.isLetterOrDigit() }) typeCount++
        
        score += typeCount * 10
        
        // 复杂度评分 (最多30分)
        val uniqueChars = password.toSet().size
        val uniqueRatio = uniqueChars.toDouble() / password.length
        score += (uniqueRatio * 30).toInt()
        
        // 扣分项
        if (isCommonPassword(password)) score -= 20
        if (hasRepeatingChars(password)) score -= 10
        if (hasSequentialChars(password)) score -= 10
        if (hasKeyboardPattern(password)) score -= 15
        
        return score.coerceIn(0, 100)
    }
    
    /**
     * 计算密码熵值
     */
    private fun calculateEntropy(password: String): Double {
        val charsetSize = getCharsetSize(password)
        return password.length * ln(charsetSize.toDouble()) / ln(2.0)
    }
    
    /**
     * 获取字符集大小
     */
    private fun getCharsetSize(password: String): Int {
        var size = 0
        if (password.any { it.isLowerCase() }) size += 26
        if (password.any { it.isUpperCase() }) size += 26
        if (password.any { it.isDigit() }) size += 10
        if (password.any { !it.isLetterOrDigit() }) size += 32
        return size
    }
    
    /**
     * 估算破解时间（秒）
     */
    private fun estimateCrackTime(password: String): Long {
        val entropy = calculateEntropy(password)
        val guessesPerSecond = 1_000_000_000.0 // 假设每秒10亿次尝试
        val totalGuesses = 2.0.pow(entropy - 1) // 平均需要尝试一半的组合
        return (totalGuesses / guessesPerSecond).toLong()
    }
    
    /**
     * 检查是否为常见密码
     */
    private fun isCommonPassword(password: String): Boolean {
        val lowerPassword = password.lowercase()
        
        // 直接匹配
        if (COMMON_PASSWORDS.contains(lowerPassword)) return true
        
        // 检查常见变形
        val withoutNumbers = lowerPassword.filter { !it.isDigit() }
        if (COMMON_PASSWORDS.contains(withoutNumbers)) return true
        
        // 检查常见替换
        val withoutSubstitutions = lowerPassword.map { char ->
            COMMON_SUBSTITUTIONS.entries.find { it.value == char }?.key ?: char
        }.joinToString("")
        
        return COMMON_PASSWORDS.contains(withoutSubstitutions)
    }
    
    /**
     * 检查密码重复使用
     */
    private fun checkPasswordReuse(password: String, allPasswords: List<PasswordEntry>): Boolean {
        val currentHash = hashPassword(password)
        return allPasswords.count { hashPassword(it.password) == currentHash } > 1
    }
    
    /**
     * 检测密码模式
     */
    private fun detectPatterns(password: String): List<String> {
        val patterns = mutableListOf<String>()
        
        if (hasRepeatingChars(password)) {
            patterns.add("重复字符")
        }
        
        if (hasSequentialChars(password)) {
            patterns.add("连续字符")
        }
        
        if (hasKeyboardPattern(password)) {
            patterns.add("键盘模式")
        }
        
        if (hasDatePattern(password)) {
            patterns.add("日期模式")
        }
        
        if (hasNamePattern(password)) {
            patterns.add("姓名模式")
        }
        
        return patterns
    }
    
    /**
     * 检查重复字符
     */
    private fun hasRepeatingChars(password: String): Boolean {
        for (i in 0 until password.length - 2) {
            if (password[i] == password[i + 1] && password[i + 1] == password[i + 2]) {
                return true
            }
        }
        return false
    }
    
    /**
     * 检查连续字符
     */
    private fun hasSequentialChars(password: String): Boolean {
        for (i in 0 until password.length - 2) {
            val char1 = password[i].code
            val char2 = password[i + 1].code
            val char3 = password[i + 2].code
            
            if ((char2 == char1 + 1 && char3 == char2 + 1) ||
                (char2 == char1 - 1 && char3 == char2 - 1)) {
                return true
            }
        }
        return false
    }
    
    /**
     * 检查键盘模式
     */
    private fun hasKeyboardPattern(password: String): Boolean {
        val lowerPassword = password.lowercase()
        return KEYBOARD_PATTERNS.any { pattern ->
            lowerPassword.contains(pattern) || lowerPassword.contains(pattern.reversed())
        }
    }
    
    /**
     * 检查日期模式
     */
    private fun hasDatePattern(password: String): Boolean {
        val datePatterns = listOf(
            Regex("\\d{4}"), // 年份
            Regex("\\d{2}/\\d{2}"), // 月/日
            Regex("\\d{2}-\\d{2}"), // 月-日
            Regex("\\d{8}") // YYYYMMDD
        )
        
        return datePatterns.any { it.containsMatchIn(password) }
    }
    
    /**
     * 检查姓名模式
     */
    private fun hasNamePattern(password: String): Boolean {
        // 简单检查：连续的字母后跟数字
        val namePattern = Regex("[a-zA-Z]{3,}\\d+")
        return namePattern.containsMatchIn(password)
    }
    
    /**
     * 检查数据泄露（模拟）
     */
    private suspend fun checkDataBreaches(password: String): Int = withContext(Dispatchers.IO) {
        // 这里应该调用真实的数据泄露检查API，如HaveIBeenPwned
        // 现在返回模拟数据
        val hash = hashPassword(password)
        
        // 模拟一些已知泄露的密码
        val knownBreachedHashes = setOf(
            hashPassword("123456"),
            hashPassword("password"),
            hashPassword("123456789")
        )
        
        if (knownBreachedHashes.contains(hash)) {
            return@withContext (1..5).random() // 模拟泄露次数
        }
        
        return@withContext 0
    }
    
    /**
     * 生成改进建议
     */
    private fun generateSuggestions(
        password: String,
        patterns: List<String>,
        isCommon: Boolean,
        isReused: Boolean
    ): List<String> {
        val suggestions = mutableListOf<String>()
        
        if (password.length < 8) {
            suggestions.add("增加密码长度至少8位")
        }
        
        if (password.length < 12) {
            suggestions.add("建议使用12位或更长的密码")
        }
        
        if (!password.any { it.isUpperCase() }) {
            suggestions.add("添加大写字母")
        }
        
        if (!password.any { it.isLowerCase() }) {
            suggestions.add("添加小写字母")
        }
        
        if (!password.any { it.isDigit() }) {
            suggestions.add("添加数字")
        }
        
        if (!password.any { !it.isLetterOrDigit() }) {
            suggestions.add("添加特殊字符")
        }
        
        if (isCommon) {
            suggestions.add("避免使用常见密码")
        }
        
        if (isReused) {
            suggestions.add("为每个账户使用唯一密码")
        }
        
        if (patterns.contains("重复字符")) {
            suggestions.add("避免重复字符")
        }
        
        if (patterns.contains("连续字符")) {
            suggestions.add("避免连续字符序列")
        }
        
        if (patterns.contains("键盘模式")) {
            suggestions.add("避免键盘上的连续按键")
        }
        
        if (patterns.contains("日期模式")) {
            suggestions.add("避免使用生日或其他重要日期")
        }
        
        if (patterns.contains("姓名模式")) {
            suggestions.add("避免使用姓名加数字的组合")
        }
        
        return suggestions
    }
    
    /**
     * 批量分析密码
     */
    suspend fun analyzeAllPasswords(passwords: List<PasswordEntry>): Map<Long, PasswordAnalysis> {
        return withContext(Dispatchers.Default) {
            passwords.associate { entry ->
                entry.id to analyzePassword(entry.password, passwords)
            }
        }
    }
    
    /**
     * 获取安全统计信息
     */
    suspend fun getSecurityStatistics(passwords: List<PasswordEntry>): SecurityStatistics {
        val analyses = analyzeAllPasswords(passwords)
        
        val weakPasswords = analyses.values.count { it.strengthScore < 50 }
        val reusedPasswords = analyses.values.count { it.isReused }
        val commonPasswords = analyses.values.count { it.isCommon }
        val breachedPasswords = analyses.values.count { it.breachCount > 0 }
        
        val averageStrength = if (analyses.isNotEmpty()) {
            analyses.values.map { it.strengthScore }.average()
        } else 0.0
        
        return SecurityStatistics(
            totalPasswords = passwords.size,
            weakPasswords = weakPasswords,
            reusedPasswords = reusedPasswords,
            commonPasswords = commonPasswords,
            breachedPasswords = breachedPasswords,
            averageStrength = averageStrength,
            securityScore = calculateOverallSecurityScore(passwords.size, weakPasswords, reusedPasswords, commonPasswords, breachedPasswords)
        )
    }
    
    /**
     * 计算整体安全评分
     */
    private fun calculateOverallSecurityScore(
        total: Int,
        weak: Int,
        reused: Int,
        common: Int,
        breached: Int
    ): Int {
        if (total == 0) return 100
        
        val weakRatio = weak.toDouble() / total
        val reusedRatio = reused.toDouble() / total
        val commonRatio = common.toDouble() / total
        val breachedRatio = breached.toDouble() / total
        
        val score = 100 - (weakRatio * 30 + reusedRatio * 25 + commonRatio * 25 + breachedRatio * 20)
        return score.toInt().coerceIn(0, 100)
    }
    
    /**
     * 生成密码哈希
     */
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * 安全统计数据类
     */
    data class SecurityStatistics(
        val totalPasswords: Int,
        val weakPasswords: Int,
        val reusedPasswords: Int,
        val commonPasswords: Int,
        val breachedPasswords: Int,
        val averageStrength: Double,
        val securityScore: Int
    )
}