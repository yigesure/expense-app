package com.passwordmanager.elite

import android.app.Application
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.passwordmanager.elite.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * 密码管理器应用程序类
 * 负责初始化加密库、依赖注入和全局配置
 */
class PasswordManagerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化加密库
        initializeCrypto()
        
        // 初始化依赖注入
        startKoin {
            androidContext(this@PasswordManagerApplication)
            modules(appModule)
        }
    }
    
    /**
     * 初始化Google Tink加密库
     */
    private fun initializeCrypto() {
        try {
            AeadConfig.register()
        } catch (e: Exception) {
            throw RuntimeException("加密库初始化失败", e)
        }
    }
}