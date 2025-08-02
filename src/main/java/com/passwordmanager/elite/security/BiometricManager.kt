package com.passwordmanager.elite.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 生物识别管理器
 * 负责处理指纹识别、面部识别等生物识别认证功能
 */
class BiometricAuthManager(private val context: Context) {
    
    companion object {
        private const val TAG = "BiometricAuthManager"
    }
    
    private val biometricManager = BiometricManager.from(context)
    
    /**
     * 生物识别认证结果
     */
    sealed class BiometricResult {
        object Success : BiometricResult()
        data class Error(val errorCode: Int, val errorMessage: String) : BiometricResult()
        object Failed : BiometricResult()
        object Cancelled : BiometricResult()
    }
    
    /**
     * 生物识别可用性状态
     */
    enum class BiometricAvailability {
        AVAILABLE,              // 可用
        NOT_AVAILABLE,          // 不可用
        NOT_ENROLLED,           // 未注册生物识别
        HARDWARE_UNAVAILABLE,   // 硬件不可用
        SECURITY_UPDATE_REQUIRED // 需要安全更新
    }
    
    /**
     * 检查生物识别可用性
     */
    fun checkBiometricAvailability(): BiometricAvailability {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NOT_AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SECURITY_UPDATE_REQUIRED
            else -> BiometricAvailability.NOT_AVAILABLE
        }
    }
    
    /**
     * 是否支持生物识别
     */
    fun isBiometricSupported(): Boolean {
        return checkBiometricAvailability() == BiometricAvailability.AVAILABLE
    }
    
    /**
     * 执行生物识别认证
     */
    suspend fun authenticate(
        activity: FragmentActivity,
        title: String = "生物识别验证",
        subtitle: String = "使用指纹或面部识别解锁应用",
        description: String = "请验证您的身份以访问密码库",
        negativeButtonText: String = "取消"
    ): BiometricResult = suspendCancellableCoroutine { continuation ->
        
        val executor = ContextCompat.getMainExecutor(context)
        
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (continuation.isActive) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            continuation.resume(BiometricResult.Cancelled)
                        }
                        else -> {
                            continuation.resume(BiometricResult.Error(errorCode, errString.toString()))
                        }
                    }
                }
            }
            
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                if (continuation.isActive) {
                    continuation.resume(BiometricResult.Success)
                }
            }
            
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                if (continuation.isActive) {
                    continuation.resume(BiometricResult.Failed)
                }
            }
        })
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        
        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resume(BiometricResult.Error(-1, e.message ?: "未知错误"))
            }
        }
        
        // 设置取消回调
        continuation.invokeOnCancellation {
            try {
                biometricPrompt.cancelAuthentication()
            } catch (e: Exception) {
                // 忽略取消时的异常
            }
        }
    }
    
    /**
     * 获取生物识别类型描述
     */
    fun getBiometricTypeDescription(): String {
        return when (checkBiometricAvailability()) {
            BiometricAvailability.AVAILABLE -> {
                // 检查具体支持的生物识别类型
                val capabilities = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                when {
                    hasFingerprint() && hasFaceRecognition() -> "指纹识别和面部识别"
                    hasFingerprint() -> "指纹识别"
                    hasFaceRecognition() -> "面部识别"
                    else -> "生物识别"
                }
            }
            BiometricAvailability.NOT_ENROLLED -> "未设置生物识别"
            BiometricAvailability.HARDWARE_UNAVAILABLE -> "生物识别硬件不可用"
            BiometricAvailability.SECURITY_UPDATE_REQUIRED -> "需要安全更新"
            else -> "生物识别不可用"
        }
    }
    
    /**
     * 检查是否支持指纹识别
     */
    private fun hasFingerprint(): Boolean {
        return try {
            val packageManager = context.packageManager
            packageManager.hasSystemFeature("android.hardware.fingerprint")
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查是否支持面部识别
     */
    private fun hasFaceRecognition(): Boolean {
        return try {
            val packageManager = context.packageManager
            packageManager.hasSystemFeature("android.hardware.biometrics.face")
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取错误消息的用户友好描述
     */
    fun getErrorDescription(errorCode: Int): String {
        return when (errorCode) {
            BiometricPrompt.ERROR_HW_UNAVAILABLE -> "生物识别硬件当前不可用"
            BiometricPrompt.ERROR_UNABLE_TO_PROCESS -> "无法处理生物识别数据"
            BiometricPrompt.ERROR_TIMEOUT -> "生物识别超时"
            BiometricPrompt.ERROR_NO_SPACE -> "存储空间不足"
            BiometricPrompt.ERROR_CANCELED -> "生物识别已取消"
            BiometricPrompt.ERROR_LOCKOUT -> "尝试次数过多，请稍后再试"
            BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> "尝试次数过多，生物识别已被永久锁定"
            BiometricPrompt.ERROR_USER_CANCELED -> "用户取消了生物识别"
            BiometricPrompt.ERROR_NO_BIOMETRICS -> "未注册生物识别信息"
            BiometricPrompt.ERROR_HW_NOT_PRESENT -> "设备不支持生物识别"
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> "用户点击了取消按钮"
            BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> "未设置设备凭据"
            else -> "生物识别验证失败"
        }
    }
    
    /**
     * 创建快速认证提示信息
     */
    fun createQuickAuthPrompt(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("快速解锁")
            .setSubtitle("使用生物识别快速访问")
            .setNegativeButtonText("使用密码")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
    }
    
    /**
     * 创建安全操作认证提示信息
     */
    fun createSecureOperationPrompt(operationName: String): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("安全验证")
            .setSubtitle("执行$operationName 需要验证身份")
            .setDescription("为了保护您的数据安全，请验证您的身份")
            .setNegativeButtonText("取消")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
    }
}