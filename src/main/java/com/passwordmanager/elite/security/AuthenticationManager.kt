package com.passwordmanager.elite.security

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 认证管理器
 * 统一管理主密码认证和生物识别认证
 */
class AuthenticationManager(
    private val context: Context,
    private val cryptoManager: CryptoManager,
    private val biometricManager: BiometricAuthManager
) {
    
    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_MASTER_PASSWORD_HASH = "master_password_hash"
        private const val KEY_SALT = "salt"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_AUTO_LOCK_TIMEOUT = "auto_lock_timeout"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LAST_UNLOCK_TIME = "last_unlock_time"
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION = 30 * 60 * 1000L // 30分钟
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _authState = MutableStateFlow(AuthState.LOCKED)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _lockoutState = MutableStateFlow<LockoutState?>(null)
    val lockoutState: StateFlow<LockoutState?> = _lockoutState.asStateFlow()
    
    /**
     * 认证状态
     */
    enum class AuthState {
        LOCKED,         // 已锁定
        UNLOCKED,       // 已解锁
        SETUP_REQUIRED, // 需要设置
        LOCKED_OUT      // 被锁定
    }
    
    /**
     * 锁定状态
     */
    data class LockoutState(
        val remainingTime: Long,
        val failedAttempts: Int
    )
    
    /**
     * 认证结果
     */
    sealed class AuthResult {
        object Success : AuthResult()
        object WrongPassword : AuthResult()
        object BiometricFailed : AuthResult()
        object LockedOut : AuthResult()
        data class Error(val message: String) : AuthResult()
    }
    
    init {
        checkInitialState()
    }
    
    /**
     * 检查初始状态
     */
    private fun checkInitialState() {
        when {
            isFirstLaunch() -> _authState.value = AuthState.SETUP_REQUIRED
            isLockedOut() -> _authState.value = AuthState.LOCKED_OUT
            isAutoLockExpired() -> _authState.value = AuthState.LOCKED
            else -> _authState.value = AuthState.UNLOCKED
        }
    }
    
    /**
     * 是否首次启动
     */
    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }
    
    /**
     * 设置主密码
     */
    suspend fun setupMasterPassword(password: String): AuthResult {
        return try {
            val (hash, salt) = cryptoManager.hashMasterPassword(password)
            
            sharedPreferences.edit()
                .putString(KEY_MASTER_PASSWORD_HASH, hash)
                .putString(KEY_SALT, android.util.Base64.encodeToString(salt, android.util.Base64.DEFAULT))
                .putBoolean(KEY_IS_FIRST_LAUNCH, false)
                .putLong(KEY_LAST_UNLOCK_TIME, System.currentTimeMillis())
                .apply()
            
            // 初始化加密系统
            cryptoManager.initialize(password)
            
            _authState.value = AuthState.UNLOCKED
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error("设置主密码失败: ${e.message}")
        }
    }
    
    /**
     * 使用主密码认证
     */
    suspend fun authenticateWithMasterPassword(password: String): AuthResult {
        if (isLockedOut()) {
            return AuthResult.LockedOut
        }
        
        return try {
            val storedHash = sharedPreferences.getString(KEY_MASTER_PASSWORD_HASH, null)
                ?: return AuthResult.Error("未设置主密码")
            
            val saltString = sharedPreferences.getString(KEY_SALT, null)
                ?: return AuthResult.Error("认证数据损坏")
            
            val salt = android.util.Base64.decode(saltString, android.util.Base64.DEFAULT)
            
            if (cryptoManager.verifyMasterPassword(password, storedHash, salt)) {
                // 认证成功
                resetFailedAttempts()
                cryptoManager.initialize(password)
                _authState.value = AuthState.UNLOCKED
                updateLastUnlockTime()
                AuthResult.Success
            } else {
                // 认证失败
                incrementFailedAttempts()
                AuthResult.WrongPassword
            }
        } catch (e: Exception) {
            AuthResult.Error("认证失败: ${e.message}")
        }
    }
    
    /**
     * 使用生物识别认证
     */
    suspend fun authenticateWithBiometric(activity: FragmentActivity): AuthResult {
        if (!isBiometricEnabled()) {
            return AuthResult.Error("生物识别未启用")
        }
        
        if (!biometricManager.isBiometricSupported()) {
            return AuthResult.Error("设备不支持生物识别")
        }
        
        return when (val result = biometricManager.authenticate(activity)) {
            is BiometricAuthManager.BiometricResult.Success -> {
                _authState.value = AuthState.UNLOCKED
                updateLastUnlockTime()
                AuthResult.Success
            }
            is BiometricAuthManager.BiometricResult.Failed -> {
                AuthResult.BiometricFailed
            }
            is BiometricAuthManager.BiometricResult.Cancelled -> {
                AuthResult.Error("用户取消认证")
            }
            is BiometricAuthManager.BiometricResult.Error -> {
                AuthResult.Error(biometricManager.getErrorDescription(result.errorCode))
            }
        }
    }
    
    /**
     * 启用生物识别
     */
    fun enableBiometric() {
        sharedPreferences.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, true)
            .apply()
    }
    
    /**
     * 禁用生物识别
     */
    fun disableBiometric() {
        sharedPreferences.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
    }
    
    /**
     * 是否启用生物识别
     */
    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
    
    /**
     * 锁定应用
     */
    fun lock() {
        _authState.value = AuthState.LOCKED
    }
    
    /**
     * 设置自动锁定超时时间
     */
    fun setAutoLockTimeout(timeoutSeconds: Int) {
        sharedPreferences.edit()
            .putInt(KEY_AUTO_LOCK_TIMEOUT, timeoutSeconds)
            .apply()
    }
    
    /**
     * 获取自动锁定超时时间
     */
    fun getAutoLockTimeout(): Int {
        return sharedPreferences.getInt(KEY_AUTO_LOCK_TIMEOUT, 300) // 默认5分钟
    }
    
    /**
     * 检查是否自动锁定超时
     */
    private fun isAutoLockExpired(): Boolean {
        val lastUnlockTime = sharedPreferences.getLong(KEY_LAST_UNLOCK_TIME, 0)
        val timeoutMs = getAutoLockTimeout() * 1000L
        return System.currentTimeMillis() - lastUnlockTime > timeoutMs
    }
    
    /**
     * 更新最后解锁时间
     */
    private fun updateLastUnlockTime() {
        sharedPreferences.edit()
            .putLong(KEY_LAST_UNLOCK_TIME, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * 增加失败尝试次数
     */
    private fun incrementFailedAttempts() {
        val currentAttempts = sharedPreferences.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        sharedPreferences.edit()
            .putInt(KEY_FAILED_ATTEMPTS, currentAttempts)
            .apply()
        
        if (currentAttempts >= MAX_FAILED_ATTEMPTS) {
            _authState.value = AuthState.LOCKED_OUT
            _lockoutState.value = LockoutState(LOCKOUT_DURATION, currentAttempts)
        }
    }
    
    /**
     * 重置失败尝试次数
     */
    private fun resetFailedAttempts() {
        sharedPreferences.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .apply()
        _lockoutState.value = null
    }
    
    /**
     * 是否被锁定
     */
    private fun isLockedOut(): Boolean {
        val failedAttempts = sharedPreferences.getInt(KEY_FAILED_ATTEMPTS, 0)
        return failedAttempts >= MAX_FAILED_ATTEMPTS
    }
    
    /**
     * 获取失败尝试次数
     */
    fun getFailedAttempts(): Int {
        return sharedPreferences.getInt(KEY_FAILED_ATTEMPTS, 0)
    }
    
    /**
     * 获取剩余尝试次数
     */
    fun getRemainingAttempts(): Int {
        return (MAX_FAILED_ATTEMPTS - getFailedAttempts()).coerceAtLeast(0)
    }
    
    /**
     * 更改主密码
     */
    suspend fun changeMasterPassword(oldPassword: String, newPassword: String): AuthResult {
        // 首先验证旧密码
        val authResult = authenticateWithMasterPassword(oldPassword)
        if (authResult != AuthResult.Success) {
            return authResult
        }
        
        return try {
            val (hash, salt) = cryptoManager.hashMasterPassword(newPassword)
            
            sharedPreferences.edit()
                .putString(KEY_MASTER_PASSWORD_HASH, hash)
                .putString(KEY_SALT, android.util.Base64.encodeToString(salt, android.util.Base64.DEFAULT))
                .apply()
            
            // 重新初始化加密系统
            cryptoManager.initialize(newPassword)
            
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error("更改主密码失败: ${e.message}")
        }
    }
    
    /**
     * 清除所有认证数据
     */
    fun clearAllAuthData() {
        sharedPreferences.edit().clear().apply()
        cryptoManager.clearAllData()
        _authState.value = AuthState.SETUP_REQUIRED
        _lockoutState.value = null
    }
    
    /**
     * 检查认证状态
     */
    fun checkAuthState(): AuthState {
        return when {
            isFirstLaunch() -> AuthState.SETUP_REQUIRED
            isLockedOut() -> AuthState.LOCKED_OUT
            isAutoLockExpired() -> {
                _authState.value = AuthState.LOCKED
                AuthState.LOCKED
            }
            else -> _authState.value
        }
    }
    
    /**
     * 获取生物识别可用性
     */
    fun getBiometricAvailability(): BiometricAuthManager.BiometricAvailability {
        return biometricManager.checkBiometricAvailability()
    }
    
    /**
     * 获取生物识别类型描述
     */
    fun getBiometricTypeDescription(): String {
        return biometricManager.getBiometricTypeDescription()
    }
}