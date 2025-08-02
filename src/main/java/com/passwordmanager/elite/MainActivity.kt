package com.passwordmanager.elite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.passwordmanager.elite.ui.MainScreen
import com.passwordmanager.elite.ui.screens.AuthenticationScreen
import com.passwordmanager.elite.ui.theme.PasswordManagerTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    
    private val biometricManager: com.passwordmanager.elite.security.BiometricManager by inject()
    private val authenticationManager: com.passwordmanager.elite.security.AuthenticationManager by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 启用边到边显示
        enableEdgeToEdge()
        
        // 设置状态栏和导航栏透明
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            PasswordManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
    
    @Composable
    private fun AppContent() {
        var isAuthenticated by remember { mutableStateOf(false) }
        var isBiometricAvailable by remember { mutableStateOf(false) }
        
        // 检查生物识别可用性
        LaunchedEffect(Unit) {
            isBiometricAvailable = biometricManager.isBiometricAvailable()
        }
        
        if (isAuthenticated) {
            MainScreen()
        } else {
            AuthenticationScreen(
                onAuthenticationSuccess = {
                    isAuthenticated = true
                },
                onBiometricAuthRequest = {
                    performBiometricAuthentication { success ->
                        if (success) {
                            isAuthenticated = true
                        }
                    }
                },
                isBiometricAvailable = isBiometricAvailable
            )
        }
    }
    
    private fun performBiometricAuthentication(onResult: (Boolean) -> Unit) {
        val biometricPrompt = BiometricPrompt(
            this as FragmentActivity,
            androidx.core.content.ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onResult(false)
                }
                
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(true)
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onResult(false)
                }
            }
        )
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("生物识别验证")
            .setSubtitle("使用您的生物识别信息解锁密码管理器")
            .setNegativeButtonText("使用密码")
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PasswordManagerTheme {
        MainScreen()
    }
}
