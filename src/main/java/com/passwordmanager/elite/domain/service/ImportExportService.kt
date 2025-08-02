package com.passwordmanager.elite.domain.service

import com.passwordmanager.elite.data.model.PasswordCategory
import com.passwordmanager.elite.data.model.PasswordEntry
import com.passwordmanager.elite.data.repository.PasswordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.StringReader

/**
 * 导入导出服务
 * 负责密码数据的导入导出功能，支持多种格式
 */
class ImportExportService(
    private val passwordRepository: PasswordRepository
) {
    
    companion object {
        private const val EXPORT_VERSION = "1.0"
        private const val MAX_IMPORT_SIZE = 10 * 1024 * 1024 // 10MB
    }
    
    /**
     * 导出数据格式
     */
    @Serializable
    data class ExportData(
        val version: String,
        val exportTime: Long,
        val appName: String,
        val totalCount: Int,
        val passwords: List<ExportPasswordEntry>
    )
    
    /**
     * 导出密码条目格式
     */
    @Serializable
    data class ExportPasswordEntry(
        val title: String,
        val username: String?,
        val password: String,
        val website: String?,
        val notes: String?,
        val category: String,
        val tags: List<String>,
        val customFields: Map<String, String>,
        val isFavorite: Boolean,
        val createdAt: Long,
        val updatedAt: Long,
        val lastUsedAt: Long?
    )
    
    /**
     * 导入结果
     */
    data class ImportResult(
        val success: Boolean,
        val importedCount: Int,
        val skippedCount: Int,
        val errorCount: Int,
        val errors: List<String>,
        val duplicates: List<String>
    )
    
    /**
     * 导出结果
     */
    data class ExportResult(
        val success: Boolean,
        val data: String?,
        val count: Int,
        val error: String?
    )
    
    /**
     * 导出所有密码为JSON格式
     */
    suspend fun exportToJson(): ExportResult = withContext(Dispatchers.IO) {
        try {
            val passwords = mutableListOf<PasswordEntry>()
            passwordRepository.getAllPasswords().collect { passwords.addAll(it) }
            
            val exportData = ExportData(
                version = EXPORT_VERSION,
                exportTime = System.currentTimeMillis(),
                appName = "密码管理器精英版",
                totalCount = passwords.size,
                passwords = passwords.map { entry ->
                    ExportPasswordEntry(
                        title = entry.title,
                        username = entry.username,
                        password = entry.password,
                        website = entry.website,
                        notes = entry.notes,
                        category = entry.category.name,
                        tags = entry.tags,
                        customFields = entry.customFields,
                        isFavorite = entry.isFavorite,
                        createdAt = entry.createdAt,
                        updatedAt = entry.updatedAt,
                        lastUsedAt = entry.lastUsedAt
                    )
                }
            )
            
            val json = Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }
            
            val jsonString = json.encodeToString(ExportData.serializer(), exportData)
            
            ExportResult(
                success = true,
                data = jsonString,
                count = passwords.size,
                error = null
            )
        } catch (e: Exception) {
            ExportResult(
                success = false,
                data = null,
                count = 0,
                error = "导出失败: ${e.message}"
            )
        }
    }
    
    /**
     * 导出为CSV格式
     */
    suspend fun exportToCsv(): ExportResult = withContext(Dispatchers.IO) {
        try {
            val passwords = mutableListOf<PasswordEntry>()
            passwordRepository.getAllPasswords().collect { passwords.addAll(it) }
            
            val csvBuilder = StringBuilder()
            
            // CSV头部
            csvBuilder.appendLine("标题,用户名,密码,网站,备注,分类,标签,是否收藏,创建时间,更新时间,最后使用时间")
            
            // 数据行
            passwords.forEach { entry ->
                csvBuilder.appendLine(
                    listOf(
                        escapeCsvField(entry.title),
                        escapeCsvField(entry.username ?: ""),
                        escapeCsvField(entry.password),
                        escapeCsvField(entry.website ?: ""),
                        escapeCsvField(entry.notes ?: ""),
                        escapeCsvField(entry.category.displayName),
                        escapeCsvField(entry.tags.joinToString(";")),
                        if (entry.isFavorite) "是" else "否",
                        formatTimestamp(entry.createdAt),
                        formatTimestamp(entry.updatedAt),
                        entry.lastUsedAt?.let { formatTimestamp(it) } ?: ""
                    ).joinToString(",")
                )
            }
            
            ExportResult(
                success = true,
                data = csvBuilder.toString(),
                count = passwords.size,
                error = null
            )
        } catch (e: Exception) {
            ExportResult(
                success = false,
                data = null,
                count = 0,
                error = "CSV导出失败: ${e.message}"
            )
        }
    }
    
    /**
     * 从JSON导入密码
     */
    suspend fun importFromJson(jsonData: String): ImportResult = withContext(Dispatchers.IO) {
        try {
            if (jsonData.length > MAX_IMPORT_SIZE) {
                return@withContext ImportResult(
                    success = false,
                    importedCount = 0,
                    skippedCount = 0,
                    errorCount = 0,
                    errors = listOf("导入文件过大，超过10MB限制"),
                    duplicates = emptyList()
                )
            }
            
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            
            val exportData = json.decodeFromString(ExportData.serializer(), jsonData)
            
            var importedCount = 0
            var skippedCount = 0
            var errorCount = 0
            val errors = mutableListOf<String>()
            val duplicates = mutableListOf<String>()
            
            // 获取现有密码用于重复检查
            val existingPasswords = mutableListOf<PasswordEntry>()
            passwordRepository.getAllPasswords().collect { existingPasswords.addAll(it) }
            
            exportData.passwords.forEach { exportEntry ->
                try {
                    // 检查重复
                    val isDuplicate = existingPasswords.any { existing ->
                        existing.title == exportEntry.title && 
                        existing.username == exportEntry.username &&
                        existing.website == exportEntry.website
                    }
                    
                    if (isDuplicate) {
                        duplicates.add("${exportEntry.title} (${exportEntry.username ?: "无用户名"})")
                        skippedCount++
                        return@forEach
                    }
                    
                    // 转换为内部格式
                    val passwordEntry = PasswordEntry(
                        id = 0, // 自动生成
                        title = exportEntry.title,
                        username = exportEntry.username,
                        password = exportEntry.password,
                        website = exportEntry.website,
                        notes = exportEntry.notes,
                        category = parseCategory(exportEntry.category),
                        tags = exportEntry.tags,
                        customFields = exportEntry.customFields,
                        isFavorite = exportEntry.isFavorite,
                        createdAt = exportEntry.createdAt,
                        updatedAt = exportEntry.updatedAt,
                        lastUsedAt = exportEntry.lastUsedAt
                    )
                    
                    // 验证数据
                    val validationErrors = validateImportEntry(passwordEntry)
                    if (validationErrors.isNotEmpty()) {
                        errors.addAll(validationErrors.map { "${exportEntry.title}: $it" })
                        errorCount++
                        return@forEach
                    }
                    
                    // 插入数据库
                    passwordRepository.insertPassword(passwordEntry)
                    importedCount++
                    
                } catch (e: Exception) {
                    errors.add("${exportEntry.title}: ${e.message}")
                    errorCount++
                }
            }
            
            ImportResult(
                success = importedCount > 0,
                importedCount = importedCount,
                skippedCount = skippedCount,
                errorCount = errorCount,
                errors = errors,
                duplicates = duplicates
            )
            
        } catch (e: Exception) {
            ImportResult(
                success = false,
                importedCount = 0,
                skippedCount = 0,
                errorCount = 0,
                errors = listOf("JSON解析失败: ${e.message}"),
                duplicates = emptyList()
            )
        }
    }
    
    /**
     * 从CSV导入密码
     */
    suspend fun importFromCsv(csvData: String): ImportResult = withContext(Dispatchers.IO) {
        try {
            if (csvData.length > MAX_IMPORT_SIZE) {
                return@withContext ImportResult(
                    success = false,
                    importedCount = 0,
                    skippedCount = 0,
                    errorCount = 0,
                    errors = listOf("导入文件过大，超过10MB限制"),
                    duplicates = emptyList()
                )
            }
            
            val reader = BufferedReader(StringReader(csvData))
            val lines = reader.readLines()
            
            if (lines.isEmpty()) {
                return@withContext ImportResult(
                    success = false,
                    importedCount = 0,
                    skippedCount = 0,
                    errorCount = 0,
                    errors = listOf("CSV文件为空"),
                    duplicates = emptyList()
                )
            }
            
            var importedCount = 0
            var skippedCount = 0
            var errorCount = 0
            val errors = mutableListOf<String>()
            val duplicates = mutableListOf<String>()
            
            // 获取现有密码用于重复检查
            val existingPasswords = mutableListOf<PasswordEntry>()
            passwordRepository.getAllPasswords().collect { existingPasswords.addAll(it) }
            
            // 跳过头部行，从第二行开始处理数据
            lines.drop(1).forEachIndexed { index, line ->
                try {
                    val fields = parseCsvLine(line)
                    
                    if (fields.size < 3) { // 至少需要标题、用户名、密码
                        errors.add("第${index + 2}行: 字段数量不足")
                        errorCount++
                        return@forEachIndexed
                    }
                    
                    val title = fields.getOrNull(0)?.trim() ?: ""
                    val username = fields.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
                    val password = fields.getOrNull(2)?.trim() ?: ""
                    val website = fields.getOrNull(3)?.trim()?.takeIf { it.isNotEmpty() }
                    val notes = fields.getOrNull(4)?.trim()?.takeIf { it.isNotEmpty() }
                    val categoryName = fields.getOrNull(5)?.trim() ?: "其他"
                    val tagsString = fields.getOrNull(6)?.trim() ?: ""
                    val isFavoriteString = fields.getOrNull(7)?.trim() ?: "否"
                    
                    if (title.isEmpty() || password.isEmpty()) {
                        errors.add("第${index + 2}行: 标题和密码不能为空")
                        errorCount++
                        return@forEachIndexed
                    }
                    
                    // 检查重复
                    val isDuplicate = existingPasswords.any { existing ->
                        existing.title == title && 
                        existing.username == username &&
                        existing.website == website
                    }
                    
                    if (isDuplicate) {
                        duplicates.add("$title (${username ?: "无用户名"})")
                        skippedCount++
                        return@forEachIndexed
                    }
                    
                    val passwordEntry = PasswordEntry(
                        id = 0,
                        title = title,
                        username = username,
                        password = password,
                        website = website,
                        notes = notes,
                        category = parseCategory(categoryName),
                        tags = if (tagsString.isNotEmpty()) tagsString.split(";") else emptyList(),
                        customFields = emptyMap(),
                        isFavorite = isFavoriteString == "是" || isFavoriteString.lowercase() == "true",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        lastUsedAt = null
                    )
                    
                    // 验证数据
                    val validationErrors = validateImportEntry(passwordEntry)
                    if (validationErrors.isNotEmpty()) {
                        errors.addAll(validationErrors.map { "第${index + 2}行 $title: $it" })
                        errorCount++
                        return@forEachIndexed
                    }
                    
                    // 插入数据库
                    passwordRepository.insertPassword(passwordEntry)
                    importedCount++
                    
                } catch (e: Exception) {
                    errors.add("第${index + 2}行: ${e.message}")
                    errorCount++
                }
            }
            
            ImportResult(
                success = importedCount > 0,
                importedCount = importedCount,
                skippedCount = skippedCount,
                errorCount = errorCount,
                errors = errors,
                duplicates = duplicates
            )
            
        } catch (e: Exception) {
            ImportResult(
                success = false,
                importedCount = 0,
                skippedCount = 0,
                errorCount = 0,
                errors = listOf("CSV解析失败: ${e.message}"),
                duplicates = emptyList()
            )
        }
    }
    
    /**
     * 解析分类
     */
    private fun parseCategory(categoryName: String): PasswordCategory {
        return when (categoryName.lowercase()) {
            "登录", "login" -> PasswordCategory.LOGIN
            "银行卡", "bank", "banking" -> PasswordCategory.BANK_CARD
            "身份证", "identity", "id" -> PasswordCategory.IDENTITY
            "信用卡", "credit", "credit_card" -> PasswordCategory.CREDIT_CARD
            "社交", "social" -> PasswordCategory.SOCIAL
            "工作", "work" -> PasswordCategory.WORK
            "购物", "shopping" -> PasswordCategory.SHOPPING
            "娱乐", "entertainment" -> PasswordCategory.ENTERTAINMENT
            else -> PasswordCategory.OTHER
        }
    }
    
    /**
     * 验证导入条目
     */
    private fun validateImportEntry(entry: PasswordEntry): List<String> {
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
     * 转义CSV字段
     */
    private fun escapeCsvField(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\"" 
        } else {
            field
        }
    }
    
    /**
     * 解析CSV行
     */
    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        var currentField = StringBuilder()
        var inQuotes = false
        var i = 0
        
        while (i < line.length) {
            val char = line[i]
            
            when {
                char == '"' && !inQuotes -> {
                    inQuotes = true
                }
                char == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        // 转义的引号
                        currentField.append('"')
                        i++ // 跳过下一个引号
                    } else {
                        inQuotes = false
                    }
                }
                char == ',' && !inQuotes -> {
                    fields.add(currentField.toString())
                    currentField.clear()
                }
                else -> {
                    currentField.append(char)
                }
            }
            i++
        }
        
        fields.add(currentField.toString())
        return fields
    }
    
    /**
     * 格式化时间戳
     */
    private fun formatTimestamp(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    /**
     * 获取导入格式说明
     */
    fun getImportFormatDescription(): String {
        return """
            支持的导入格式：
            
            1. JSON格式：
            - 完整的应用数据导出格式
            - 包含所有字段和元数据
            - 推荐用于应用间数据迁移
            
            2. CSV格式：
            - 表格格式，易于编辑
            - 字段顺序：标题,用户名,密码,网站,备注,分类,标签,是否收藏,创建时间,更新时间,最后使用时间
            - 支持从其他密码管理器导出的CSV文件
            
            注意事项：
            - 文件大小限制：10MB
            - 重复条目将被跳过
            - 无效数据将被记录在错误报告中
        """.trimIndent()
    }
}