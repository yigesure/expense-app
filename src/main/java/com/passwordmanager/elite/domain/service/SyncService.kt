package com.passwordmanager.elite.domain.service

import android.content.Context
import com.passwordmanager.elite.data.model.PasswordEntry
import com.passwordmanager.elite.data.model.SyncStatus
import com.passwordmanager.elite.security.CryptoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据同步服务
 * 实现端到端加密的跨设备数据同步
 */
@Singleton
class SyncService @Inject constructor(
    private val context: Context,
    private val cryptoManager: CryptoManager
) {
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()
    
    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * 开始同步数据
     */
    suspend fun startSync(passwords: List<PasswordEntry>): SyncResult {
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            _syncProgress.value = 0f
            
            // 1. 准备同步数据
            _syncProgress.value = 0.2f
            val syncData = prepareSyncData(passwords)
            
            // 2. 加密数据
            _syncProgress.value = 0.4f
            val encryptedData = encryptSyncData(syncData)
            
            // 3. 上传到云端
            _syncProgress.value = 0.6f
            val uploadResult = uploadToCloud(encryptedData)
            
            // 4. 验证同步结果
            _syncProgress.value = 0.8f
            val verificationResult = verifySyncResult(uploadResult)
            
            // 5. 更新本地状态
            _syncProgress.value = 1f
            updateLocalSyncStatus(verificationResult)
            
            _syncStatus.value = SyncStatus.SUCCESS
            _lastSyncTime.value = System.currentTimeMillis()
            
            SyncResult.Success(
                syncedCount = passwords.size,
                timestamp = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            SyncResult.Error(e.message ?: "同步失败")
        } finally {
            _syncProgress.value = 0f
        }
    }
    
    /**
     * 从云端拉取数据
     */
    suspend fun pullFromCloud(): SyncResult {
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            _syncProgress.value = 0f
            
            // 1. 从云端下载数据
            _syncProgress.value = 0.3f
            val encryptedData = downloadFromCloud()
            
            // 2. 解密数据
            _syncProgress.value = 0.6f
            val decryptedData = decryptSyncData(encryptedData)
            
            // 3. 验证数据完整性
            _syncProgress.value = 0.8f
            val validationResult = validateSyncData(decryptedData)
            
            // 4. 应用到本地数据库
            _syncProgress.value = 1f
            val appliedCount = applyToLocalDatabase(validationResult)
            
            _syncStatus.value = SyncStatus.SUCCESS
            _lastSyncTime.value = System.currentTimeMillis()
            
            SyncResult.Success(
                syncedCount = appliedCount,
                timestamp = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            SyncResult.Error(e.message ?: "拉取数据失败")
        } finally {
            _syncProgress.value = 0f
        }
    }
    
    /**
     * 检查同步冲突
     */
    suspend fun checkSyncConflicts(localPasswords: List<PasswordEntry>): List<SyncConflict> {
        return try {
            val cloudData = downloadFromCloud()
            val decryptedCloudData = decryptSyncData(cloudData)
            val cloudPasswords = decryptedCloudData.passwords
            
            val conflicts = mutableListOf<SyncConflict>()
            
            // 检查每个本地密码是否与云端有冲突
            localPasswords.forEach { localPassword ->
                val cloudPassword = cloudPasswords.find { it.id == localPassword.id }
                if (cloudPassword != null) {
                    // 比较修改时间
                    if (localPassword.updatedAt != cloudPassword.updatedAt) {
                        conflicts.add(
                            SyncConflict(
                                passwordId = localPassword.id,
                                localEntry = localPassword,
                                cloudEntry = cloudPassword,
                                conflictType = ConflictType.MODIFIED_BOTH
                            )
                        )
                    }
                }
            }
            
            // 检查云端独有的密码
            cloudPasswords.forEach { cloudPassword ->
                val localPassword = localPasswords.find { it.id == cloudPassword.id }
                if (localPassword == null) {
                    conflicts.add(
                        SyncConflict(
                            passwordId = cloudPassword.id,
                            localEntry = null,
                            cloudEntry = cloudPassword,
                            conflictType = ConflictType.CLOUD_ONLY
                        )
                    )
                }
            }
            
            // 检查本地独有的密码
            localPasswords.forEach { localPassword ->
                val cloudPassword = cloudPasswords.find { it.id == localPassword.id }
                if (cloudPassword == null) {
                    conflicts.add(
                        SyncConflict(
                            passwordId = localPassword.id,
                            localEntry = localPassword,
                            cloudEntry = null,
                            conflictType = ConflictType.LOCAL_ONLY
                        )
                    )
                }
            }
            
            conflicts
            
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 解决同步冲突
     */
    suspend fun resolveConflicts(
        conflicts: List<SyncConflict>,
        resolutions: Map<String, ConflictResolution>
    ): SyncResult {
        return try {
            _syncStatus.value = SyncStatus.RESOLVING_CONFLICTS
            
            val resolvedPasswords = mutableListOf<PasswordEntry>()
            
            conflicts.forEach { conflict ->
                val resolution = resolutions[conflict.passwordId] ?: ConflictResolution.KEEP_LOCAL
                
                when (resolution) {
                    ConflictResolution.KEEP_LOCAL -> {
                        conflict.localEntry?.let { resolvedPasswords.add(it) }
                    }
                    ConflictResolution.KEEP_CLOUD -> {
                        conflict.cloudEntry?.let { resolvedPasswords.add(it) }
                    }
                    ConflictResolution.MERGE -> {
                        val mergedEntry = mergePasswordEntries(conflict.localEntry, conflict.cloudEntry)
                        mergedEntry?.let { resolvedPasswords.add(it) }
                    }
                }
            }
            
            // 应用解决方案
            val appliedCount = applyResolvedPasswords(resolvedPasswords)
            
            _syncStatus.value = SyncStatus.SUCCESS
            
            SyncResult.Success(
                syncedCount = appliedCount,
                timestamp = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            SyncResult.Error(e.message ?: "解决冲突失败")
        }
    }
    
    /**
     * 获取同步设置
     */
    fun getSyncSettings(): SyncSettings {
        val prefs = context.getSharedPreferences("sync_settings", Context.MODE_PRIVATE)
        return SyncSettings(
            autoSyncEnabled = prefs.getBoolean("auto_sync_enabled", false),
            syncInterval = prefs.getInt("sync_interval", 30),
            wifiOnlySync = prefs.getBoolean("wifi_only_sync", true),
            encryptionEnabled = prefs.getBoolean("encryption_enabled", true)
        )
    }
    
    /**
     * 更新同步设置
     */
    fun updateSyncSettings(settings: SyncSettings) {
        val prefs = context.getSharedPreferences("sync_settings", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("auto_sync_enabled", settings.autoSyncEnabled)
            putInt("sync_interval", settings.syncInterval)
            putBoolean("wifi_only_sync", settings.wifiOnlySync)
            putBoolean("encryption_enabled", settings.encryptionEnabled)
            apply()
        }
    }
    
    // 私有方法实现
    
    private suspend fun prepareSyncData(passwords: List<PasswordEntry>): SyncData {
        return SyncData(
            version = SYNC_VERSION,
            timestamp = System.currentTimeMillis(),
            deviceId = getDeviceId(),
            passwords = passwords,
            checksum = calculateChecksum(passwords)
        )
    }
    
    private suspend fun encryptSyncData(syncData: SyncData): ByteArray {
        val jsonData = json.encodeToString(syncData)
        return cryptoManager.encrypt(jsonData.toByteArray())
    }
    
    private suspend fun decryptSyncData(encryptedData: ByteArray): SyncData {
        val decryptedBytes = cryptoManager.decrypt(encryptedData)
        val jsonData = String(decryptedBytes)
        return json.decodeFromString<SyncData>(jsonData)
    }
    
    private suspend fun uploadToCloud(encryptedData: ByteArray): CloudUploadResult {
        // TODO: 实现实际的云端上传逻辑
        // 这里可以集成Google Drive、iCloud、或自定义云服务
        
        // 模拟上传过程
        kotlinx.coroutines.delay(2000)
        
        return CloudUploadResult(
            success = true,
            cloudId = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis()
        )
    }
    
    private suspend fun downloadFromCloud(): ByteArray {
        // TODO: 实现实际的云端下载逻辑
        
        // 模拟下载过程
        kotlinx.coroutines.delay(1500)
        
        // 返回模拟的加密数据
        val mockSyncData = SyncData(
            version = SYNC_VERSION,
            timestamp = System.currentTimeMillis(),
            deviceId = "mock_device",
            passwords = emptyList(),
            checksum = ""
        )
        
        val jsonData = json.encodeToString(mockSyncData)
        return cryptoManager.encrypt(jsonData.toByteArray())
    }
    
    private suspend fun verifySyncResult(uploadResult: CloudUploadResult): Boolean {
        return uploadResult.success
    }
    
    private suspend fun updateLocalSyncStatus(verified: Boolean) {
        if (verified) {
            val prefs = context.getSharedPreferences("sync_status", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putLong("last_sync_time", System.currentTimeMillis())
                putBoolean("last_sync_success", true)
                apply()
            }
        }
    }
    
    private suspend fun validateSyncData(syncData: SyncData): SyncData {
        // 验证数据版本
        if (syncData.version != SYNC_VERSION) {
            throw IllegalStateException("不支持的同步数据版本: ${syncData.version}")
        }
        
        // 验证校验和
        val calculatedChecksum = calculateChecksum(syncData.passwords)
        if (calculatedChecksum != syncData.checksum) {
            throw IllegalStateException("数据校验失败")
        }
        
        return syncData
    }
    
    private suspend fun applyToLocalDatabase(syncData: SyncData): Int {
        // TODO: 实际应用到本地数据库
        // 这里需要与PasswordRepository集成
        return syncData.passwords.size
    }
    
    private suspend fun applyResolvedPasswords(passwords: List<PasswordEntry>): Int {
        // TODO: 应用解决冲突后的密码数据
        return passwords.size
    }
    
    private fun mergePasswordEntries(
        localEntry: PasswordEntry?,
        cloudEntry: PasswordEntry?
    ): PasswordEntry? {
        return when {
            localEntry == null -> cloudEntry
            cloudEntry == null -> localEntry
            localEntry.updatedAt > cloudEntry.updatedAt -> localEntry
            else -> cloudEntry
        }
    }
    
    private fun calculateChecksum(passwords: List<PasswordEntry>): String {
        val data = passwords.sortedBy { it.id }.joinToString { "${it.id}:${it.updatedAt}" }
        return data.hashCode().toString()
    }
    
    private fun getDeviceId(): String {
        val prefs = context.getSharedPreferences("device_info", Context.MODE_PRIVATE)
        var deviceId = prefs.getString("device_id", null)
        
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", deviceId).apply()
        }
        
        return deviceId
    }
    
    companion object {
        private const val SYNC_VERSION = 1
    }
}

/**
 * 同步数据结构
 */
@Serializable
data class SyncData(
    val version: Int,
    val timestamp: Long,
    val deviceId: String,
    val passwords: List<PasswordEntry>,
    val checksum: String
)

/**
 * 同步结果
 */
sealed class SyncResult {
    data class Success(
        val syncedCount: Int,
        val timestamp: Long
    ) : SyncResult()
    
    data class Error(
        val message: String
    ) : SyncResult()
}

/**
 * 同步冲突
 */
data class SyncConflict(
    val passwordId: String,
    val localEntry: PasswordEntry?,
    val cloudEntry: PasswordEntry?,
    val conflictType: ConflictType
)

/**
 * 冲突类型
 */
enum class ConflictType {
    MODIFIED_BOTH,  // 本地和云端都有修改
    LOCAL_ONLY,     // 仅本地存在
    CLOUD_ONLY      // 仅云端存在
}

/**
 * 冲突解决方案
 */
enum class ConflictResolution {
    KEEP_LOCAL,     // 保留本地版本
    KEEP_CLOUD,     // 保留云端版本
    MERGE           // 合并两个版本
}

/**
 * 同步设置
 */
data class SyncSettings(
    val autoSyncEnabled: Boolean = false,
    val syncInterval: Int = 30, // 分钟
    val wifiOnlySync: Boolean = true,
    val encryptionEnabled: Boolean = true
)

/**
 * 云端上传结果
 */
data class CloudUploadResult(
    val success: Boolean,
    val cloudId: String,
    val timestamp: Long
)