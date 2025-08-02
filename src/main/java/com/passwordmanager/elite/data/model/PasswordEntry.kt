package com.passwordmanager.elite.data.model

import kotlinx.serialization.Serializable

/**
 * 密码条目数据模型
 * 对应数据库中的PasswordEntry表
 */
@Serializable
data class PasswordEntry(
    val id: Long = 0,
    val title: String,
    val username: String? = null,
    val password: String,
    val website: String? = null,
    val notes: String? = null,
    val category: PasswordCategory = PasswordCategory.LOGIN,
    val iconUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long? = null,
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList(),
    val customFields: Map<String, String> = emptyMap()
)

/**
 * 密码分类枚举
 */
enum class PasswordCategory(val displayName: String) {
    LOGIN("登录密码"),
    BANK_CARD("银行卡"),
    IDENTITY_CARD("身份证件"),
    SECURE_NOTE("安全备忘"),
    WIFI("WiFi密码"),
    SOFTWARE("软件许可"),
    OTHER("其他")
}

/**
 * 安全设置数据模型
 */
@Serializable
data class SecuritySettings(
    val id: Long = 1,
    val masterPasswordHash: String,
    val salt: String,
    val biometricEnabled: Boolean = false,
    val autoLockTimeout: Int = 300, // 秒
    val failedAttempts: Int = 0,
    val lastUnlockTime: Long? = null,
    val encryptionKeyId: String
)

/**
 * 同步记录数据模型
 */
@Serializable
data class SyncRecord(
    val id: Long = 0,
    val entryId: Long,
    val action: SyncAction,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
    val deviceId: String
)

/**
 * 同步操作类型
 */
enum class SyncAction {
    CREATE, UPDATE, DELETE
}

/**
 * 审计日志数据模型
 */
@Serializable
data class AuditLog(
    val id: Long = 0,
    val action: String,
    val entryId: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceInfo: String? = null,
    val ipAddress: String? = null,
    val success: Boolean = true
)

/**
 * 密码分析数据模型
 */
@Serializable
data class PasswordAnalysis(
    val id: Long = 0,
    val entryId: Long,
    val strengthScore: Int, // 0-100
    val hasUppercase: Boolean = false,
    val hasLowercase: Boolean = false,
    val hasNumbers: Boolean = false,
    val hasSymbols: Boolean = false,
    val length: Int,
    val isCommon: Boolean = false,
    val isReused: Boolean = false,
    val breachCount: Int = 0,
    val lastAnalyzed: Long = System.currentTimeMillis()
)

/**
 * 密码强度等级
 */
enum class PasswordStrength(val score: IntRange, val displayName: String, val color: Long) {
    WEAK(0..39, "弱", 0xFFE53E3E),
    FAIR(40..59, "一般", 0xFFFF8C00),
    GOOD(60..79, "良好", 0xFF32CD32),
    STRONG(80..100, "强", 0xFF228B22);
    
    companion object {
        fun fromScore(score: Int): PasswordStrength {
            return values().find { score in it.score } ?: WEAK
        }
    }
}

/**
 * 密码统计数据
 */
data class PasswordStats(
    val totalCount: Int = 0,
    val strongCount: Int = 0,
    val mediumCount: Int = 0,
    val weakCount: Int = 0,
    val reusedCount: Int = 0,
    val breachedCount: Int = 0,
    val categoryStats: Map<PasswordCategory, Int> = emptyMap()
)