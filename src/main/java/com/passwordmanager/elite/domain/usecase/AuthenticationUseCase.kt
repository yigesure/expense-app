package com.passwordmanager.elite.domain.usecase

import androidx.fragment.app.FragmentActivity
import com.passwordmanager.elite.data.repository.PasswordRepository
import com.passwordmanager.elite.security.AuthenticationManager
import kotlinx.coroutines.flow.StateFlow

/**
 * 认证用例
 * 封装所有与用户认证相关的业务逻辑
 */
class AuthenticationUseCase(
    private val authenticationManager: AuthenticationManager,
    private val passwordRepository: PasswordRepository
) {
    
    /**
     * 认证状态流
     */
    val authState: StateFlow<AuthenticationManager.AuthState> = authenticationManager.authState
    
    /**
     * 锁定状态流
     */
    val lockoutState: StateFlow<AuthenticationManager.LockoutState?> = authenticationManager.lockoutState
    
    /**
     * 检查是否首次启动
     */
    fun isFirstLaunch(): Boolean {
        return authenticationManager.isFirstLaunch()
    }
    
    /**
     * 设置主密码
     */
    suspend fun setupMasterPassword(password: String): AuthenticationManager.AuthResult {
        // 验证密码强度
        if (!isPasswordStrong(password)) {
            return AuthenticationManager.AuthResult.Error("密码强度不足，请使用至少8位包含大小写字母、数字和特殊字符的密码")
        }
        
        val result = authenticationManager.setupMasterPassword(password)
        
        if (result is AuthenticationManager.AuthResult.Success) {
            // 记录审计日志
            passwordRepository.logAction("SETUP_MASTER_PASSWORD", success = true)
        }
        
        return result
    }
    
    /**
     * 使用主密码认证
     */
    suspend fun authenticateWithMasterPassword(password: String): AuthenticationManager.AuthResult {
        val result = authenticationManager.authenticateWithMasterPassword(password)
        
        // 记录认证尝试
        when (result) {
            is AuthenticationManager.AuthResult.Success -> {
                passwordRepository.resetFailedAttempts()
                passwordRepository.logAction("MASTER_PASSWORD_AUTH_SUCCESS", success = true)
            }
            is AuthenticationManager.AuthResult.WrongPassword -> {
                val attempts = authenticationManager.getFailedAttempts()
                passwordRepository.updateFailedAttempts(attempts)
                passwordRepository.logAction("MASTER_PASSWORD_AUTH_FAILED", success = false)
            }
            else -> {
                passwordRepository.logAction("MASTER_PASSWORD_AUTH_ERROR", success = false)
            }
        }
        
        return result
    }
    
    /**
     * 使用生物识别认证
     */
    suspend fun authenticateWithBiometric(activity: FragmentActivity): AuthenticationManager.AuthResult {
        if (!isBiometricAvailable()) {
            return AuthenticationManager.AuthResult.Error("生物识别不可用")
        }
        
        val result = authenticationManager.authenticateWithBiometric(activity)
        
        // 记录认证尝试
        when (result) {
            is AuthenticationManager.AuthResult.Success -> {
                passwordRepository.logAction("BIOMETRIC_AUTH_SUCCESS", success = true)
            }
            is AuthenticationManager.AuthResult.BiometricFailed -> {
                passwordRepository.logAction("BIOMETRIC_AUTH_FAILED", success = false)
            }
            else -> {
                passwordRepository.logAction("BIOMETRIC_AUTH_ERROR", success = false)
            }
        }
        
        return result
    }
    
    /**
     * 启用生物识别
     */
    suspend fun enableBiometric(activity: FragmentActivity): Boolean {
        if (!isBiometricSupported()) {
            return false
        }
        
        // 先进行一次生物识别验证确保可用
        val result = authenticationManager.authenticateWithBiometric(activity)
        
        return if (result is AuthenticationManager.AuthResult.Success) {
            authenticationManager.enableBiometric()
            passwordRepository.logAction("BIOMETRIC_ENABLED", success = true)
            true
        } else {
            false
        }
    }
    
    /**
     * 禁用生物识别
     */
    suspend fun disableBiometric() {
        authenticationManager.disableBiometric()
        passwordRepository.logAction("BIOMETRIC_DISABLED", success = true)
    }
    
    /**
     * 检查生物识别是否启用
     */
    fun isBiometricEnabled(): Boolean {
        return authenticationManager.isBiometricEnabled()
    }
    
    /**
     * 检查生物识别是否可用
     */
    fun isBiometricAvailable(): Boolean {
        return isBiometricSupported() && isBiometricEnabled()
    }
    
    /**
     * 检查设备是否支持生物识别
     */
    fun isBiometricSupported(): Boolean {
        return authenticationManager.getBiometricAvailability() == 
            com.passwordmanager.elite.security.BiometricAuthManager.BiometricAvailability.AVAILABLE
    }
    
    /**
     * 获取生物识别类型描述
     */
    fun getBiometricTypeDescription(): String {
        return authenticationManager.getBiometricTypeDescription()
    }
    
    /**
     * 锁定应用
     */
    suspend fun lockApp() {
        authenticationManager.lock()
        passwordRepository.logAction("APP_LOCKED", success = true)
    }
    
    /**
     * 设置自动锁定超时时间
     */
    suspend fun setAutoLockTimeout(timeoutSeconds: Int) {
        authenticationManager.setAutoLockTimeout(timeoutSeconds)
        passwordRepository.logAction("AUTO_LOCK_TIMEOUT_CHANGED", success = true)
    }
    
    /**
     * 获取自动锁定超时时间
     */
    fun getAutoLockTimeout(): Int {
        return authenticationManager.getAutoLockTimeout()
    }
    
    /**
     * 获取失败尝试次数
     */
    fun getFailedAttempts(): Int {
        return authenticationManager.getFailedAttempts()
    }
    
    /**
     * 获取剩余尝试次数
     */
    fun getRemainingAttempts(): Int {
        return authenticationManager.getRemainingAttempts()
    }
    
    /**
     * 更改主密码
     */
    suspend fun changeMasterPassword(oldPassword: String, newPassword: String): AuthenticationManager.AuthResult {
        // 验证新密码强度
        if (!isPasswordStrong(newPassword)) {
            return AuthenticationManager.AuthResult.Error("新密码强度不足")
        }
        
        val result = authenticationManager.changeMasterPassword(oldPassword, newPassword)
        
        if (result is AuthenticationManager.AuthResult.Success) {
            passwordRepository.logAction("MASTER_PASSWORD_CHANGED", success = true)
        } else {
            passwordRepository.logAction("MASTER_PASSWORD_CHANGE_FAILED", success = false)
        }
        
        return result
    }
    
    /**
     * 清除所有数据（重置应用）
     */
    suspend fun resetApp() {
        authenticationManager.clearAllAuthData()
        passwordRepository.logAction("APP_RESET", success = true)
    }
    
    /**
     * 检查认证状态
     */
    fun checkAuthState(): AuthenticationManager.AuthState {
        return authenticationManager.checkAuthState()
    }
    
    /**
     * 验证密码强度
     */
    private fun isPasswordStrong(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() }
    }
    
    /**
     * 获取密码强度建议
     */
    fun getPasswordStrengthSuggestions(password: String): List<String> {
        val suggestions = mutableListOf<String>()
        
        if (password.length < 8) {
            suggestions.add("密码长度至少需要8位")
        }
        if (!password.any { it.isUpperCase() }) {
            suggestions.add("需要包含大写字母")
        }
        if (!password.any { it.isLowerCase() }) {
            suggestions.add("需要包含小写字母")
        }
        if (!password.any { it.isDigit() }) {
            suggestions.add("需要包含数字")
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            suggestions.add("需要包含特殊字符")
        }
        
        return suggestions
    }
    
    /**
     * 获取安全建议
     */
    fun getSecurityRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (!isBiometricEnabled() && isBiometricSupported()) {
            recommendations.add("建议启用生物识别以提高安全性和便利性")
        }
        
        val autoLockTimeout = getAutoLockTimeout()
        if (autoLockTimeout > 600) { // 超过10分钟
            recommendations.add("建议设置较短的自动锁定时间以提高安全性")
        }
        
        return recommendations
    }
}