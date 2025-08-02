package com.passwordmanager.elite.data.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.passwordmanager.elite.data.model.*
import com.passwordmanager.elite.database.PasswordDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 密码数据访问对象
 * 提供对密码相关数据的CRUD操作
 */
class PasswordDao(private val database: PasswordDatabase) {
    
    private val queries = database.passwordDatabaseQueries
    
    /**
     * 获取所有密码条目
     */
    fun getAllPasswords(): Flow<List<PasswordEntry>> {
        return queries.selectAllPasswords()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map { it.toPasswordEntry() }
            }
    }
    
    /**
     * 根据分类获取密码条目
     */
    fun getPasswordsByCategory(category: String): Flow<List<PasswordEntry>> {
        return queries.selectPasswordsByCategory(category)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map { it.toPasswordEntry() }
            }
    }
    
    /**
     * 搜索密码条目
     */
    fun searchPasswords(query: String): Flow<List<PasswordEntry>> {
        return queries.searchPasswords(query, query, query)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map { it.toPasswordEntry() }
            }
    }
    
    /**
     * 根据ID获取密码条目
     */
    fun getPasswordById(id: Long): Flow<PasswordEntry?> {
        return queries.selectPasswordById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toPasswordEntry() }
    }
    
    /**
     * 插入新密码条目
     */
    suspend fun insertPassword(entry: PasswordEntry): Long {
        queries.insertPassword(
            title = entry.title,
            username = entry.username,
            password = entry.password,
            website = entry.website,
            notes = entry.notes,
            category = entry.category.name.lowercase(),
            icon_url = entry.iconUrl,
            created_at = entry.createdAt,
            updated_at = entry.updatedAt,
            is_favorite = if (entry.isFavorite) 1L else 0L,
            tags = Json.encodeToString(entry.tags),
            custom_fields = Json.encodeToString(entry.customFields)
        )
        return database.passwordDatabaseQueries.lastInsertRowId().executeAsOne()
    }
    
    /**
     * 更新密码条目
     */
    suspend fun updatePassword(entry: PasswordEntry) {
        queries.updatePassword(
            title = entry.title,
            username = entry.username,
            password = entry.password,
            website = entry.website,
            notes = entry.notes,
            category = entry.category.name.lowercase(),
            icon_url = entry.iconUrl,
            updated_at = System.currentTimeMillis(),
            is_favorite = if (entry.isFavorite) 1L else 0L,
            tags = Json.encodeToString(entry.tags),
            custom_fields = Json.encodeToString(entry.customFields),
            id = entry.id
        )
    }
    
    /**
     * 删除密码条目
     */
    suspend fun deletePassword(id: Long) {
        queries.deletePassword(id)
    }
    
    /**
     * 更新最后使用时间
     */
    suspend fun updateLastUsedTime(id: Long) {
        queries.updateLastUsedTime(System.currentTimeMillis(), id)
    }
    
    /**
     * 获取收藏的密码条目
     */
    fun getFavoritePasswords(): Flow<List<PasswordEntry>> {
        return queries.selectFavoritePasswords()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map { it.toPasswordEntry() }
            }
    }
    
    /**
     * 获取安全设置
     */
    fun getSecuritySettings(): Flow<SecuritySettings?> {
        return queries.selectSecuritySettings()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toSecuritySettings() }
    }
    
    /**
     * 插入或更新安全设置
     */
    suspend fun insertOrUpdateSecuritySettings(settings: SecuritySettings) {
        queries.insertOrUpdateSecuritySettings(
            master_password_hash = settings.masterPasswordHash,
            salt = settings.salt,
            biometric_enabled = if (settings.biometricEnabled) 1L else 0L,
            auto_lock_timeout = settings.autoLockTimeout.toLong(),
            failed_attempts = settings.failedAttempts.toLong(),
            last_unlock_time = settings.lastUnlockTime,
            encryption_key_id = settings.encryptionKeyId
        )
    }
    
    /**
     * 更新失败尝试次数
     */
    suspend fun updateFailedAttempts(attempts: Int) {
        queries.updateFailedAttempts(attempts.toLong())
    }
    
    /**
     * 重置失败尝试次数
     */
    suspend fun resetFailedAttempts() {
        queries.resetFailedAttempts(System.currentTimeMillis())
    }
    
    /**
     * 插入密码分析数据
     */
    suspend fun insertOrUpdatePasswordAnalysis(analysis: PasswordAnalysis) {
        queries.insertOrUpdatePasswordAnalysis(
            entry_id = analysis.entryId,
            strength_score = analysis.strengthScore.toLong(),
            has_uppercase = if (analysis.hasUppercase) 1L else 0L,
            has_lowercase = if (analysis.hasLowercase) 1L else 0L,
            has_numbers = if (analysis.hasNumbers) 1L else 0L,
            has_symbols = if (analysis.hasSymbols) 1L else 0L,
            length = analysis.length.toLong(),
            is_common = if (analysis.isCommon) 1L else 0L,
            is_reused = if (analysis.isReused) 1L else 0L,
            breach_count = analysis.breachCount.toLong(),
            last_analyzed = analysis.lastAnalyzed
        )
    }
    
    /**
     * 获取密码分析数据
     */
    fun getPasswordAnalysis(entryId: Long): Flow<PasswordAnalysis?> {
        return queries.selectPasswordAnalysis(entryId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toPasswordAnalysis() }
    }
    
    /**
     * 获取弱密码条目
     */
    fun getWeakPasswords(): Flow<List<PasswordEntry>> {
        return queries.selectWeakPasswords()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map { 
                    PasswordEntry(
                        id = it.id,
                        title = it.title,
                        username = it.username,
                        password = it.password,
                        website = it.website,
                        notes = it.notes,
                        category = PasswordCategory.valueOf(it.category.uppercase()),
                        iconUrl = it.icon_url,
                        createdAt = it.created_at,
                        updatedAt = it.updated_at,
                        lastUsedAt = it.last_used_at,
                        isFavorite = it.is_favorite == 1L,
                        tags = try { Json.decodeFromString(it.tags ?: "[]") } catch (e: Exception) { emptyList() },
                        customFields = try { Json.decodeFromString(it.custom_fields ?: "{}") } catch (e: Exception) { emptyMap() }
                    )
                }
            }
    }
    
    /**
     * 获取密码强度统计
     */
    fun getPasswordStrengthStats(): Flow<PasswordStats> {
        return queries.selectPasswordStrengthStats()
            .asFlow()
            .mapToOne(Dispatchers.IO)
            .map { stats ->
                PasswordStats(
                    totalCount = stats.total_count?.toInt() ?: 0,
                    strongCount = stats.strong_count?.toInt() ?: 0,
                    mediumCount = stats.medium_count?.toInt() ?: 0,
                    weakCount = stats.weak_count?.toInt() ?: 0
                )
            }
    }
}

/**
 * 扩展函数：将数据库查询结果转换为PasswordEntry对象
 */
private fun com.passwordmanager.elite.database.PasswordEntry.toPasswordEntry(): PasswordEntry {
    return PasswordEntry(
        id = id,
        title = title,
        username = username,
        password = password,
        website = website,
        notes = notes,
        category = PasswordCategory.valueOf(category.uppercase()),
        iconUrl = icon_url,
        createdAt = created_at,
        updatedAt = updated_at,
        lastUsedAt = last_used_at,
        isFavorite = is_favorite == 1L,
        tags = try { Json.decodeFromString(tags ?: "[]") } catch (e: Exception) { emptyList() },
        customFields = try { Json.decodeFromString(custom_fields ?: "{}") } catch (e: Exception) { emptyMap() }
    )
}

/**
 * 扩展函数：将数据库查询结果转换为SecuritySettings对象
 */
private fun com.passwordmanager.elite.database.SecuritySettings.toSecuritySettings(): SecuritySettings {
    return SecuritySettings(
        id = id,
        masterPasswordHash = master_password_hash,
        salt = salt,
        biometricEnabled = biometric_enabled == 1L,
        autoLockTimeout = auto_lock_timeout.toInt(),
        failedAttempts = failed_attempts.toInt(),
        lastUnlockTime = last_unlock_time,
        encryptionKeyId = encryption_key_id
    )
}

/**
 * 扩展函数：将数据库查询结果转换为PasswordAnalysis对象
 */
private fun com.passwordmanager.elite.database.PasswordAnalysis.toPasswordAnalysis(): PasswordAnalysis {
    return PasswordAnalysis(
        id = id,
        entryId = entry_id,
        strengthScore = strength_score.toInt(),
        hasUppercase = has_uppercase == 1L,
        hasLowercase = has_lowercase == 1L,
        hasNumbers = has_numbers == 1L,
        hasSymbols = has_symbols == 1L,
        length = length.toInt(),
        isCommon = is_common == 1L,
        isReused = is_reused == 1L,
        breachCount = breach_count.toInt(),
        lastAnalyzed = last_analyzed
    )
}