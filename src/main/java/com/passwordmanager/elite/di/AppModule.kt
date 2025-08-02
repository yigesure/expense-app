package com.passwordmanager.elite.di

import androidx.room.Room
import com.passwordmanager.elite.data.database.PasswordDatabase
import com.passwordmanager.elite.data.dao.PasswordDao
import com.passwordmanager.elite.data.repository.PasswordRepository
import com.passwordmanager.elite.data.repository.PasswordRepositoryImpl
import com.passwordmanager.elite.domain.usecase.AuthenticationUseCase
import com.passwordmanager.elite.domain.usecase.PasswordGeneratorUseCase
import com.passwordmanager.elite.domain.usecase.PasswordManagementUseCase
import com.passwordmanager.elite.domain.usecase.SecurityAnalysisUseCase
import com.passwordmanager.elite.domain.service.PasswordAnalysisService
import com.passwordmanager.elite.domain.service.ImportExportService
import com.passwordmanager.elite.domain.service.SyncService
import com.passwordmanager.elite.presentation.viewmodel.AuthViewModel
import com.passwordmanager.elite.presentation.viewmodel.MainViewModel
import com.passwordmanager.elite.presentation.viewmodel.PasswordDetailViewModel
import com.passwordmanager.elite.presentation.viewmodel.PasswordGeneratorViewModel
import com.passwordmanager.elite.presentation.viewmodel.SettingsViewModel
import com.passwordmanager.elite.security.CryptoManager
import com.passwordmanager.elite.security.BiometricManager
import com.passwordmanager.elite.security.AuthenticationManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin依赖注入模块配置
 * 定义应用程序中所有依赖项的创建和注入规则
 */
val appModule = module {
    
    // 数据库
    single {
        PasswordDatabaseFactory.create(androidContext())
    }
    
    single { PasswordDao(get()) }
    
    // 安全管理
    single { CryptoManager(androidContext()) }
    single { BiometricAuthManager(androidContext()) }
    single { AuthenticationManager(androidContext(), get(), get()) }
    
    // 仓库层
    single<PasswordRepository> { 
        PasswordRepositoryImpl(
            passwordDao = get(),
            cryptoManager = get()
        ) 
    }
    
    // 服务层
    single { PasswordAnalysisService() }
    single { ImportExportService(get()) }
    single { SyncService(get(), get()) }
    
    // 用例层
    single { AuthenticationUseCase(get(), get()) }
    single { PasswordManagementUseCase(get()) }
    single { PasswordGeneratorUseCase() }
    single { SecurityAnalysisUseCase(get()) }
    
    // ViewModel层
    viewModel { AuthViewModel(get()) }
    viewModel { MainViewModel(get(), get()) }
    viewModel { PasswordDetailViewModel(get()) }
    viewModel { PasswordGeneratorViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
}