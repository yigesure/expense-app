package com.passwordmanager.elite.domain.usecase

import com.passwordmanager.elite.data.model.*
import com.passwordmanager.elite.data.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 密码管理用例
 * 封装密码条目的增删改查和相关业务逻辑
 */
class PasswordManagementUseCase(
    private val passwordRepository: PasswordRepository
) {
    
    /**
     * 获取所有密码条目
     */
    fun getAllPasswords(): Flow<List<PasswordEntry>> {
        return passwordRepository.getAllPasswords()
    }
    
    /**
     * 根据分类获取密码条目
     */
    fun getPasswordsByCategory(category: PasswordCategory): Flow<List<PasswordEntry>> {
        return passwordRepository.getPasswordsByCategory(category)
    }
    
    /**
     * 搜索密码条目
     */
    fun searchPasswords(query: String): Flow<List<PasswordEntry>> {
        return if (query.isBlank()) {
            getAllPasswords()
        } else {
            passwordRepository.searchPasswords(query.trim())
        }
    }
    
    /**
     * 根据ID获取密码条目
     */
    fun getPasswordById(id: Long): Flow<PasswordEntry?> {
        return passwordRepository.getPasswordById(id)
    }
    
    /**
     * 添加新密码条目
     */
    suspend fun addPassword(entry: PasswordEntry): Result<Long> {
        return try {
            // 验证必填字段
            if (entry.title.isBlank()) {
                return Result.failure(IllegalArgumentException("标题不能为空"))
            }
            if (entry.password.isBlank()) {
                return Result.failure(IllegalArgumentException("密码不能为空"))
            }
            
            val entryId = passwordRepository.insertPassword(entry)
            Result.success(entryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新密码条目
     */
    suspend fun updatePassword(entry: PasswordEntry): Result<Unit> {
        return try {
            // 验证必填字段
            if (entry.title.isBlank()) {
                return Result.failure(IllegalArgumentException("标题不能为空"))
            }
            if (entry.password.isBlank()) {
                return Result.failure(IllegalArgumentException("密码不能为空"))
            }
            
            passwordRepository.updatePassword(entry.copy(updatedAt = System.currentTimeMillis()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除密码条目
     */
    suspend fun deletePassword(id: Long): Result<Unit> {
        return try {
            passwordRepository.deletePassword(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 批量删除密码条目
     */
    suspend fun deletePasswords(ids: List<Long>): Result<Unit> {
        return try {
            ids.forEach { id ->
                passwordRepository.deletePassword(id)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 复制密码条目
     */
    suspend fun duplicatePassword(id: Long): Result<Long> {
        return try {
            val originalEntry = passwordRepository.getPasswordById(id)
                .map { it }
                .let { flow ->
                    var entry: PasswordEntry? = null
                    flow.collect { entry = it }
                    entry
                } ?: return Result.failure(IllegalArgumentException("密码条目不存在"))
            
            val duplicatedEntry = originalEntry.copy(
                id = 0,
                title = "${originalEntry.title} (副本)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                lastUsedAt = null
            )
            
            val newId = passwordRepository.insertPassword(duplicatedEntry)
            Result.success(newId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 标记密码为收藏/取消收藏
     */
    suspend fun toggleFavorite(id: Long): Result<Unit> {
        return try {
            val entry = passwordRepository.getPasswordById(id)
                .map { it }
                .let { flow ->
                    var entry: PasswordEntry? = null
                    flow.collect { entry = it }
                    entry
                } ?: return Result.failure(IllegalArgumentException("密码条目不存在"))
            
            val updatedEntry = entry.copy(
                isFavorite = !entry.isFavorite,
                updatedAt = System.currentTimeMillis()
            )
            
            passwordRepository.updatePassword(updatedEntry)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取收藏的密码条目
     */
    fun getFavoritePasswords(): Flow<List<PasswordEntry>> {
        return passwordRepository.getFavoritePasswords()
    }
    
    /**
     * 更新密码使用时间
     */
    suspend fun markPasswordUsed(id: Long): Result<Unit> {
        return try {
            passwordRepository.updateLastUsedTime(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取最近使用的密码条目
     */
    fun getRecentlyUsedPasswords(limit: Int = 10): Flow<List<PasswordEntry>> {
        return getAllPasswords().map { passwords ->
            passwords
                .filter { it.lastUsedAt != null }
                .sortedByDescending { it.lastUsedAt }
                .take(limit)
        }
    }
    
    /**
     * 获取按分类统计的密码数量
     */
    fun getPasswordCountByCategory(): Flow<Map<PasswordCategory, Int>> {
        return getAllPasswords().map { passwords ->
            passwords.groupBy { it.category }
                .mapValues { it.value.size }
        }
    }
    
    /**
     * 导出密码数据（JSON格式）
     */
    suspend fun exportPasswords(): Result<String> {
        return try {
            val passwords = mutableListOf<PasswordEntry>()
            getAllPasswords().collect { passwords.addAll(it) }
            
            val exportData = mapOf(
                "version" to "1.0",
                "exportTime" to System.currentTimeMillis(),
                "passwords" to passwords.map { entry ->
                    mapOf(
                        "title" to entry.title,
                        "username" to entry.username,
                        "password" to entry.password,
                        "website" to entry.website,
                        "notes" to entry.notes,
                        "category" to entry.category.name,
                        "tags" to entry.tags,
                        "customFields" to entry.customFields,
                        "createdAt" to entry.createdAt,
                        "updatedAt" to entry.updatedAt
                    )
                }
            )
            
            val json = kotlinx.serialization.json.Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }
            
            Result.success(json.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), 
                json.parseToJsonElement(exportData.toString())))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 验证密码条目数据
     */
    fun validatePasswordEntry(entry: PasswordEntry): List<String> {
        val errors = mutableListOf<String>()
        
        if (entry.title.isBlank()) {
            errors.add("标题不能为空")
        }
        
        if (entry.title.length > 100) {
            errors.add("标题长度不能超过100个字符")
        }
        
        if (entry.password.isBlank()) {
            errors.add("密码不能为空")
        }
        
        if (entry.username?.length ?: 0 > 100) {
            errors.add("用户名长度不能超过100个字符")
        }
        
        if (entry.website?.let { !isValidUrl(it) } == true) {
            errors.add("网站地址格式不正确")
        }
        
        if (entry.notes?.length ?: 0 > 1000) {
            errors.add("备注长度不能超过1000个字符")
        }
        
        return errors
    }
    
    /**
     * 获取密码条目的安全建议
     */
    suspend fun getSecuritySuggestions(entry: PasswordEntry): List<String> {
        val suggestions = mutableListOf<String>()
        
        // 检查密码强度
        val analysis = passwordRepository.getPasswordAnalysis(entry.id)
        analysis.collect { passwordAnalysis ->
            passwordAnalysis?.let { analysis ->
                if (analysis.strengthScore < 60) {
                    suggestions.add("密码强度较弱，建议使用更复杂的密码")
                }
                
                if (analysis.isCommon) {
                    suggestions.add("这是一个常见密码，建议更换为更安全的密码")
                }
                
                if (analysis.isReused) {
                    suggestions.add("此密码在其他账户中重复使用，建议使用唯一密码")
                }
                
                if (analysis.breachCount > 0) {
                    suggestions.add("此密码可能已在数据泄露中暴露，强烈建议立即更换")
                }
            }
        }
        
        // 检查最后使用时间
        val daysSinceLastUsed = entry.lastUsedAt?.let { lastUsed ->
            (System.currentTimeMillis() - lastUsed) / (1000 * 60 * 60 * 24)
        }
        
        if (daysSinceLastUsed != null && daysSinceLastUsed > 90) {
            suggestions.add("此密码超过90天未使用，建议检查账户是否仍然有效")
        }
        
        // 检查更新时间
        val daysSinceUpdate = (System.currentTimeMillis() - entry.updatedAt) / (1000 * 60 * 60 * 24)
        if (daysSinceUpdate > 365) {
            suggestions.add("此密码超过一年未更新，建议定期更换密码")
        }
        
        return suggestions
    }
    
    /**
     * 验证URL格式
     */
    private fun isValidUrl(url: String): Boolean {
        return try {
            val pattern = Regex(
                "^(https?://)?" +
                "((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|" +
                "((\\d{1,3}\\.){3}\\d{1,3}))" +
                "(\\:\\d+)?(/[-a-z\\d%_.~+]*)*" +
                "(\\?[;&a-z\\d%_.~+=-]*)?" +
                "(\\#[-a-z\\d_]*)?$",
                RegexOption.IGNORE_CASE
            )
            pattern.matches(url)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 生成网站图标URL
     */
    fun generateIconUrl(website: String?): String? {
        if (website.isNullOrBlank()) return null
        
        return try {
            val cleanUrl = website.let { url ->
                when {
                    url.startsWith("http://") || url.startsWith("https://") -> url
                    else -> "https://$url"
                }
            }
            
            val domain = java.net.URL(cleanUrl).host
            "https://www.google.com/s2/favicons?domain=$domain&sz=64"
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 自动填充网站信息
     */
    suspend fun autoFillWebsiteInfo(url: String): Map<String, String?> {
        return try {
            val domain = java.net.URL(url).host
            val title = domain.split(".").let { parts ->
                if (parts.size > 1) {
                    parts[parts.size - 2].replaceFirstChar { it.uppercase() }
                } else {
                    domain.replaceFirstChar { it.uppercase() }
                }
            }
            
            mapOf(
                "title" to title,
                "website" to url,
                "iconUrl" to generateIconUrl(url)
            )
        } catch (e: Exception) {
            mapOf(
                "title" to null,
                "website" to url,
                "iconUrl" to null
            )
        }
    }
}