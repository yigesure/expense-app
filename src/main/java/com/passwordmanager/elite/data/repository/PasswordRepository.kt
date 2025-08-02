package com.passwordmanager.elite.data.repository

import com.passwordmanager.elite.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * 密码仓库接口
 * 定义密码管理相关的数据操作契约
 */
interface PasswordRepository {
    
    // 密码条目操作
    fun getAllPasswords(): Flow<List<PasswordEntry>>
    fun getPasswordsByCategory(category: PasswordCategory): Flow<List<PasswordEntry>>
    fun searchPasswords(query: String): Flow<List<PasswordEntry>>
    fun getPasswordById(id: Long): Flow<PasswordEntry?>
    suspend fun insertPassword(entry: PasswordEntry): Long
    suspend fun updatePassword(entry: PasswordEntry)
    suspend fun deletePassword(id: Long)
    suspend fun updateLastUsedTime(id: Long)
    fun getFavoritePasswords(): Flow<List<PasswordEntry>>
    
    // 安全设置操作
    fun getSecuritySettings(): Flow<SecuritySettings?>
    suspend fun updateSecuritySettings(settings: SecuritySettings)
    suspend fun updateFailedAttempts(attempts: Int)
    suspend fun resetFailedAttempts()
    
    // 密码分析操作
    suspend fun analyzePassword(entryId: Long, password: String): PasswordAnalysis
    fun getPasswordAnalysis(entryId: Long): Flow<PasswordAnalysis?>
    fun getWeakPasswords(): Flow<List<PasswordEntry>>
    fun getPasswordStats(): Flow<PasswordStats>
    
    // 同步操作
    suspend fun createSyncRecord(entryId: Long, action: SyncAction)
    fun getUnsyncedRecords(): Flow<List<SyncRecord>>
    suspend fun markRecordSynced(recordId: Long)
    
    // 审计日志
    suspend fun logAction(action: String, entryId: Long? = null, success: Boolean = true)
    fun getAuditLogs(limit: Int = 100): Flow<List<AuditLog>>
}