package com.passwordmanager.elite.data.repository

import com.passwordmanager.elite.data.dao.PasswordDao
import com.passwordmanager.elite.data.model.*
import com.passwordmanager.elite.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.regex.Pattern

/**
 * 密码仓库实现类
 * 提供密码管理相关的具体数据操作实现
 */
class PasswordRepositoryImpl(
    private val passwordDao: PasswordDao,
    private val cryptoManager: CryptoManager
) : PasswordRepository {
    
    // 常用弱密码列表
    private val commonPasswords = setOf(
        "123456", "password", "123456789", "12345678", "12345",
        "1234567", "1234567890", "qwerty", "abc123", "111111",
        "123123", "admin", "letmein", "welcome", "monkey"
    )
    
    override fun getAllPasswords(): Flow<List<PasswordEntry>> {
        return passwordDao.getAllPasswords().map { passwords ->
            passwords.map { decryptPasswordEntry(it) }
        }
    }
    
    override fun getPasswordsByCategory(category: PasswordCategory): Flow<List<PasswordEntry>> {
        return passwordDao.getPasswordsByCategory(category.name.lowercase()).map { passwords ->
            passwords.map { decryptPasswordEntry(it) }
        }
    }
    
    override fun searchPasswords(query: String): Flow<List<PasswordEntry>> {
        return passwordDao.searchPasswords(query).map { passwords ->
            passwords.map { decryptPasswordEntry(it) }
        }
    }
    
    override fun getPasswordById(id: Long): Flow<PasswordEntry?> {
        return passwordDao.getPasswordById(id).map { password ->
            password?.let { decryptPasswordEntry(it) }
        }
    }
    
    override suspend fun insertPassword(entry: PasswordEntry): Long {
        val encryptedEntry = encryptPasswordEntry(entry)
        val entryId = passwordDao.insertPassword(encryptedEntry)
        
        // 分析密码强度
        analyzePassword(entryId, entry.password)
        
        // 创建同步记录
        createSyncRecord(entryId, SyncAction.CREATE)
        
        // 记录审计日志
        logAction("CREATE_PASSWORD", entryId)
        
        return entryId
    }
    
    override suspend fun updatePassword(entry: PasswordEntry) {
        val encryptedEntry = encryptPasswordEntry(entry)
        passwordDao.updatePassword(encryptedEntry)
        
        // 重新分析密码强度
        analyzePassword(entry.id, entry.password)
        
        // 创建同步记录
        createSyncRecord(entry.id, SyncAction.UPDATE)
        
        // 记录审计日志
        logAction("UPDATE_PASSWORD", entry.id)
    }
    
    override suspend fun deletePassword(id: Long) {
        passwordDao.deletePassword(id)
        
        // 创建同步记录
        createSyncRecord(id, SyncAction.DELETE)
        
        // 记录审计日志
        logAction("DELETE_PASSWORD", id)
    }
    
    override suspend fun updateLastUsedTime(id: Long) {
        passwordDao.updateLastUsedTime(id)
        logAction("USE_PASSWORD", id)
    }
    
    override fun getFavoritePasswords(): Flow<List<PasswordEntry>> {
        return passwordDao.getFavoritePasswords().map { passwords ->
            passwords.map { decryptPasswordEntry(it) }
        }
    }
    
    override fun getSecuritySettings(): Flow<SecuritySettings?> {
        return passwordDao.getSecuritySettings()
    }
    
    override suspend fun updateSecuritySettings(settings: SecuritySettings) {
        passwordDao.insertOrUpdateSecuritySettings(settings)
        logAction("UPDATE_SECURITY_SETTINGS")
    }
    
    override suspend fun updateFailedAttempts(attempts: Int) {
        passwordDao.updateFailedAttempts(attempts)
        logAction("FAILED_AUTHENTICATION", success = false)
    }
    
    override suspend fun resetFailedAttempts() {
        passwordDao.resetFailedAttempts()
        logAction("SUCCESSFUL_AUTHENTICATION")
    }
    
    override suspend fun analyzePassword(entryId: Long, password: String): PasswordAnalysis {
        val analysis = performPasswordAnalysis(password, entryId)
        passwordDao.insertOrUpdatePasswordAnalysis(analysis)
        return analysis
    }
    
    override fun getPasswordAnalysis(entryId: Long): Flow<PasswordAnalysis?> {
        return passwordDao.getPasswordAnalysis(entryId)
    }
    
    override fun getWeakPasswords(): Flow<List<PasswordEntry>> {
        return passwordDao.getWeakPasswords().map { passwords ->
            passwords.map { decryptPasswordEntry(it) }
        }
    }
    
    override fun getPasswordStats(): Flow<PasswordStats> {
        return passwordDao.getPasswordStrengthStats()
    }
    
    override suspend fun createSyncRecord(entryId: Long, action: SyncAction) {
        // 实现同步记录创建逻辑
        // 这里暂时留空，后续实现同步功能时补充
    }
    
    override fun getUnsyncedRecords(): Flow<List<SyncRecord>> {
        // 实现获取未同步记录逻辑
        // 这里暂时返回空流，后续实现同步功能时补充
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }
    
    override suspend fun markRecordSynced(recordId: Long) {
        // 实现标记记录已同步逻辑
        // 这里暂时留空，后续实现同步功能时补充
    }
    
    override suspend fun logAction(action: String, entryId: Long?, success: Boolean) {
        // 实现审计日志记录逻辑
        // 这里暂时留空，后续可以根据需要实现详细的日志记录
    }
    
    override fun getAuditLogs(limit: Int): Flow<List<AuditLog>> {
        // 实现获取审计日志逻辑
        // 这里暂时返回空流，后续可以根据需要实现
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }
    
    /**
     * 加密密码条目
     */
    private suspend fun encryptPasswordEntry(entry: PasswordEntry): PasswordEntry {
        return entry.copy(
            password = cryptoManager.encrypt(entry.password),
            notes = entry.notes?.let { cryptoManager.encrypt(it) }
        )
    }
    
    /**
     * 解密密码条目
     */
    private suspend fun decryptPasswordEntry(entry: PasswordEntry): PasswordEntry {
        return try {
            entry.copy(
                password = cryptoManager.decrypt(entry.password),
                notes = entry.notes?.let { cryptoManager.decrypt(it) }
            )
        } catch (e: Exception) {
            // 解密失败时返回原始条目，但标记密码为无效
            entry.copy(password = "解密失败")
        }
    }
    
    /**
     * 执行密码强度分析
     */
    private suspend fun performPasswordAnalysis(password: String, entryId: Long): PasswordAnalysis {
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasNumbers = password.any { it.isDigit() }
        val hasSymbols = password.any { !it.isLetterOrDigit() }
        val length = password.length
        val isCommon = commonPasswords.contains(password.lowercase())
        
        // 计算密码强度分数
        var score = 0
        
        // 长度评分 (0-30分)
        score += when {
            length >= 12 -> 30
            length >= 8 -> 20
            length >= 6 -> 10
            else -> 0
        }
        
        // 字符类型评分 (0-40分)
        if (hasUppercase) score += 10
        if (hasLowercase) score += 10
        if (hasNumbers) score += 10
        if (hasSymbols) score += 10
        
        // 复杂度评分 (0-20分)
        val charTypes = listOf(hasUppercase, hasLowercase, hasNumbers, hasSymbols).count { it }
        score += when (charTypes) {
            4 -> 20
            3 -> 15
            2 -> 10
            1 -> 5
            else -> 0
        }
        
        // 常见密码惩罚
        if (isCommon) score -= 30
        
        // 重复字符惩罚
        if (hasRepeatingChars(password)) score -= 10
        
        // 连续字符惩罚
        if (hasSequentialChars(password)) score -= 10
        
        // 确保分数在0-100范围内
        score = score.coerceIn(0, 100)
        
        return PasswordAnalysis(
            entryId = entryId,
            strengthScore = score,
            hasUppercase = hasUppercase,
            hasLowercase = hasLowercase,
            hasNumbers = hasNumbers,
            hasSymbols = hasSymbols,
            length = length,
            isCommon = isCommon,
            isReused = false, // 需要与其他密码比较
            breachCount = 0, // 需要调用泄露检查API
            lastAnalyzed = System.currentTimeMillis()
        )
    }
    
    /**
     * 检查是否有重复字符
     */
    private fun hasRepeatingChars(password: String): Boolean {
        return password.zipWithNext().any { (a, b) -> a == b }
    }
    
    /**
     * 检查是否有连续字符
     */
    private fun hasSequentialChars(password: String): Boolean {
        return password.zipWithNext().any { (a, b) -> 
            Math.abs(a.code - b.code) == 1
        }
    }
}