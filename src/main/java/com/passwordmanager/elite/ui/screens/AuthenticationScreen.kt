package com.passwordmanager.elite.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passwordmanager.elite.ui.components.GlassCard
import com.passwordmanager.elite.ui.components.GlassButton
import com.passwordmanager.elite.ui.theme.AppColors
import kotlinx.coroutines.delay

/**
 * 认证页面 - 应用启动时的身份验证界面
 */
@Composable
fun AuthenticationScreen(
    onAuthenticationSuccess: () -> Unit,
    onBiometricAuthRequest: () -> Unit,
    isBiometricAvailable: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    var authMode by remember { mutableStateOf(AuthMode.BIOMETRIC) }
    var masterPassword by remember { mutableStateOf("") }
    var isAuthenticating by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "fade_in"
    )
    
    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
        // 如果生物识别可用，自动尝试生物识别认证
        if (isBiometricAvailable && authMode == AuthMode.BIOMETRIC) {
            delay(800)
            onBiometricAuthRequest()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.MidnightBlue,
                        AppColors.CyanBlue.copy(alpha = 0.4f),
                        AppColors.MidnightBlue
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(animatedAlpha)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 应用Logo和标题
            AppLogo()
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // 认证卡片
            AuthenticationCard(
                authMode = authMode,
                masterPassword = masterPassword,
                onMasterPasswordChange = { masterPassword = it },
                onAuthModeChange = { authMode = it },
                onBiometricAuth = onBiometricAuthRequest,
                onPasswordAuth = {
                    // TODO: 验证主密码
                    isAuthenticating = true
                    // 模拟认证过程
                    // 实际应用中应该调用 AuthenticationManager
                    if (masterPassword.isNotEmpty()) {
                        onAuthenticationSuccess()
                    } else {
                        authError = "请输入主密码"
                        isAuthenticating = false
                    }
                },
                isAuthenticating = isAuthenticating,
                authError = authError,
                isBiometricAvailable = isBiometricAvailable
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 底部提示
            SecurityTip()
        }
        
        // 错误提示
        if (authError != null) {
            LaunchedEffect(authError) {
                delay(3000)
                authError = null
                isAuthenticating = false
            }
        }
    }
}

/**
 * 应用Logo组件
 */
@Composable
private fun AppLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_glow"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo图标
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppColors.Accent.copy(alpha = glowAlpha),
                            Color.Transparent
                        ),
                        radius = 100f
                    ),
                    shape = CircleShape
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                AppColors.Accent,
                                AppColors.CyanBlue
                            )
                        ),
                        shape = CircleShape
                    )
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "应用Logo",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 应用标题
        Text(
            text = "密码管理器",
            style = MaterialTheme.typography.headlineLarge,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "安全 • 简洁 • 可靠",
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.TextSecondary,
            letterSpacing = 2.sp
        )
    }
}

/**
 * 认证卡片
 */
@Composable
private fun AuthenticationCard(
    authMode: AuthMode,
    masterPassword: String,
    onMasterPasswordChange: (String) -> Unit,
    onAuthModeChange: (AuthMode) -> Unit,
    onBiometricAuth: () -> Unit,
    onPasswordAuth: () -> Unit,
    isAuthenticating: Boolean,
    authError: String?,
    isBiometricAvailable: Boolean
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 认证模式切换
            AuthModeSelector(
                currentMode = authMode,
                onModeChange = onAuthModeChange,
                isBiometricAvailable = isBiometricAvailable
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 认证内容
            when (authMode) {
                AuthMode.BIOMETRIC -> {
                    BiometricAuthContent(
                        onBiometricAuth = onBiometricAuth,
                        onSwitchToPassword = { onAuthModeChange(AuthMode.PASSWORD) },
                        isAuthenticating = isAuthenticating
                    )
                }
                AuthMode.PASSWORD -> {
                    PasswordAuthContent(
                        password = masterPassword,
                        onPasswordChange = onMasterPasswordChange,
                        onPasswordAuth = onPasswordAuth,
                        isAuthenticating = isAuthenticating,
                        authError = authError
                    )
                }
            }
        }
    }
}

/**
 * 认证模式选择器
 */
@Composable
private fun AuthModeSelector(
    currentMode: AuthMode,
    onModeChange: (AuthMode) -> Unit,
    isBiometricAvailable: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 生物识别选项
        AuthModeTab(
            title = "生物识别",
            icon = Icons.Default.Fingerprint,
            isSelected = currentMode == AuthMode.BIOMETRIC,
            isEnabled = isBiometricAvailable,
            onClick = { onModeChange(AuthMode.BIOMETRIC) }
        )
        
        // 主密码选项
        AuthModeTab(
            title = "主密码",
            icon = Icons.Default.Password,
            isSelected = currentMode == AuthMode.PASSWORD,
            isEnabled = true,
            onClick = { onModeChange(AuthMode.PASSWORD) }
        )
    }
}

/**
 * 认证模式标签
 */
@Composable
private fun AuthModeTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0.5f,
        label = "tab_alpha"
    )
    
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.Accent else AppColors.TextSecondary,
        label = "tab_color"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .alpha(animatedAlpha)
            .clickable(enabled = isEnabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = animatedColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = animatedColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
        
        // 选中指示器
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(2.dp)
                    .background(
                        color = AppColors.Accent,
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * 生物识别认证内容
 */
@Composable
private fun BiometricAuthContent(
    onBiometricAuth: () -> Unit,
    onSwitchToPassword: () -> Unit,
    isAuthenticating: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "biometric_animation")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 生物识别图标
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(if (isAuthenticating) pulseScale else 1f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppColors.Accent.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "生物识别",
                tint = AppColors.Accent,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = if (isAuthenticating) "正在验证..." else "使用生物识别解锁",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "将手指放在指纹传感器上\n或使用面部识别",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 生物识别按钮
        GlassButton(
            onClick = onBiometricAuth,
            enabled = !isAuthenticating,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isAuthenticating) {
                CircularProgressIndicator(
                    color = AppColors.TextPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (isAuthenticating) "验证中..." else "开始验证",
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 切换到密码认证
        TextButton(
            onClick = onSwitchToPassword,
            enabled = !isAuthenticating
        ) {
            Text(
                text = "使用主密码",
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * 密码认证内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordAuthContent(
    password: String,
    onPasswordChange: (String) -> Unit,
    onPasswordAuth: () -> Unit,
    isAuthenticating: Boolean,
    authError: String?
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 密码图标
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppColors.Accent.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "密码锁",
                tint = AppColors.Accent,
                modifier = Modifier.size(36.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "输入主密码",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "请输入您的主密码以解锁应用",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 密码输入框
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = {
                Text(
                    text = "主密码",
                    color = AppColors.TextSecondary
                )
            },
            placeholder = {
                Text(
                    text = "请输入主密码",
                    color = AppColors.TextTertiary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "密码",
                    tint = AppColors.TextSecondary
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { isPasswordVisible = !isPasswordVisible }
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPasswordVisible) "隐藏密码" else "显示密码",
                        tint = AppColors.TextSecondary
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    if (password.isNotEmpty()) {
                        onPasswordAuth()
                    }
                }
            ),
            singleLine = true,
            isError = authError != null,
            enabled = !isAuthenticating,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary,
                focusedBorderColor = AppColors.Accent,
                unfocusedBorderColor = AppColors.GlassBorder,
                errorBorderColor = AppColors.Error,
                cursorColor = AppColors.Accent,
                focusedLabelColor = AppColors.Accent,
                unfocusedLabelColor = AppColors.TextSecondary
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        // 错误提示
        if (authError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = authError,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Error
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 解锁按钮
        GlassButton(
            onClick = onPasswordAuth,
            enabled = password.isNotEmpty() && !isAuthenticating,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isAuthenticating) {
                CircularProgressIndicator(
                    color = AppColors.TextPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (isAuthenticating) "验证中..." else "解锁",
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * 安全提示
 */
@Composable
private fun SecurityTip() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "安全",
            tint = AppColors.Success,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "您的数据采用AES-256加密保护",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextTertiary,
            fontSize = 12.sp
        )
    }
}

/**
 * 认证模式枚举
 */
private enum class AuthMode {
    BIOMETRIC,  // 生物识别
    PASSWORD    // 主密码
}