package com.passwordmanager.elite.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.passwordmanager.elite.database.PasswordDatabase

/**
 * 密码数据库实例管理类
 * 负责创建和配置SQLDelight数据库实例
 */
object PasswordDatabaseFactory {
    
    private const val DATABASE_NAME = "password_database.db"
    private const val DATABASE_VERSION = 1
    
    /**
     * 创建数据库实例
     */
    fun create(context: Context): PasswordDatabase {
        val driver = createDriver(context)
        return PasswordDatabase(driver)
    }
    
    /**
     * 创建数据库驱动
     */
    private fun createDriver(context: Context): SqlDriver {
        return AndroidSqliteDriver(
            schema = PasswordDatabase.Schema,
            context = context,
            name = DATABASE_NAME,
            callback = object : AndroidSqliteDriver.Callback(PasswordDatabase.Schema) {
                override fun onOpen(driver: SqlDriver) {
                    super.onOpen(driver)
                    // 启用外键约束
                    driver.execute(null, "PRAGMA foreign_keys=ON", 0)
                    // 启用WAL模式以提高并发性能
                    driver.execute(null, "PRAGMA journal_mode=WAL", 0)
                    // 设置同步模式为NORMAL以平衡性能和安全性
                    driver.execute(null, "PRAGMA synchronous=NORMAL", 0)
                }
                
                override fun onUpgrade(driver: SqlDriver, oldVersion: Int, newVersion: Int) {
                    super.onUpgrade(driver, oldVersion, newVersion)
                    // 数据库升级逻辑
                    when (oldVersion) {
                        // 未来版本升级时在此处添加迁移逻辑
                    }
                }
            }
        )
    }
}

/**
 * 数据库迁移管理
 */
object DatabaseMigrations {
    
    /**
     * 执行数据库迁移
     */
    fun migrate(driver: SqlDriver, fromVersion: Int, toVersion: Int) {
        when {
            fromVersion < 2 && toVersion >= 2 -> {
                // 版本2的迁移逻辑
                // 例如：添加新表或新字段
            }
            // 添加更多版本的迁移逻辑
        }
    }
    
    /**
     * 创建初始数据
     */
    fun createInitialData(database: PasswordDatabase) {
        // 创建默认安全设置
        val queries = database.passwordDatabaseQueries
        
        // 检查是否已存在安全设置
        val existingSettings = queries.selectSecuritySettings().executeAsOneOrNull()
        if (existingSettings == null) {
            // 创建默认安全设置（需要用户首次设置主密码时更新）
            queries.insertOrUpdateSecuritySettings(
                master_password_hash = "",
                salt = "",
                biometric_enabled = 0L,
                auto_lock_timeout = 300L, // 5分钟
                failed_attempts = 0L,
                last_unlock_time = null,
                encryption_key_id = ""
            )
        }
    }
}