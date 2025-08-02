package com.passwordmanager.elite.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passwordmanager.elite.ui.components.GlassCard
import com.passwordmanager.elite.ui.components.GlassButton
import com.passwordmanager.elite.ui.theme.AppColors

/**
 * 设置页面
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "fade_in"
    )
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.MidnightBlue,
                        AppColors.CyanBlue.copy(alpha = 0.3f),
                        AppColors.MidnightBlue
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(animatedAlpha)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // 页面标题
            SettingsHeader()
            
            // 安全设置
            SecuritySettingsCard()
            
            // 应用设置
            AppSettingsCard()
            
            // 数据管理
            DataManagementCard(
                onBackup = { showBackupDialog = true },
                onExport = { showExportDialog = true }
            )
            
            // 关于应用
            AboutCard(
                onShowAbout = { showAboutDialog = true }
            )
            
            Spacer(modifier = Modifier.height(100.dp))
        }
        
        // 对话框
        if (showBackupDialog) {
            BackupDialog(
                onConfirm = {
                    showBackupDialog = false
                    // TODO: 执行备份操作
                },
                onDismiss = { showBackupDialog = false }
            )
        }
        
        if (showExportDialog) {
            ExportDialog(
                onConfirm = { format ->
                    showExportDialog = false
                    // TODO: 执行导出操作
                },
                onDismiss = { showExportDialog = false }
            )
        }
        
        if (showAboutDialog) {
            AboutDialog(
                onDismiss = { showAboutDialog = false }
            )
        }
    }
}

/**
 * 设置页面标题
 */
@Composable
private fun SettingsHeader() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineLarge,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "个性化您的密码管理体验",
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.TextSecondary
        )
    }
}

/**
 * 安全设置卡片
 */
@Composable
private fun SecuritySettingsCard() {
    var biometricEnabled by remember { mutableStateOf(true) }
    var autoLockEnabled by remember { mutableStateOf(true) }
    var autoLockTime by remember { mutableStateOf("5分钟") }
    var showAutoLockDialog by remember { mutableStateOf(false) }
    
    GlassCard {
        Column {
            SettingsSectionHeader(
                title = "安全设置",
                icon = Icons.Default.Security
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 生物识别解锁
            SettingsToggleItem(
                title = "生物识别解锁",
                description = "使用指纹或面部识别快速解锁",
                icon = Icons.Default.Fingerprint,
                checked = biometricEnabled,
                onCheckedChange = { biometricEnabled = it }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 自动锁定
            SettingsToggleItem(
                title = "自动锁定",
                description = "应用进入后台时自动锁定",
                icon = Icons.Default.Lock,
                checked = autoLockEnabled,
                onCheckedChange = { autoLockEnabled = it }
            )
            
            if (autoLockEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // 自动锁定时间
                SettingsClickableItem(
                    title = "自动锁定时间",
                    description = autoLockTime,
                    icon = Icons.Default.Timer,
                    onClick = { showAutoLockDialog = true }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 更改主密码
            SettingsClickableItem(
                title = "更改主密码",
                description = "修改应用的主密码",
                icon = Icons.Default.Key,
                onClick = { /* TODO: 打开更改主密码页面 */ }
            )
        }
    }
    
    // 自动锁定时间选择对话框
    if (showAutoLockDialog) {
        AutoLockTimeDialog(
            currentTime = autoLockTime,
            onTimeSelected = { time ->
                autoLockTime = time
                showAutoLockDialog = false
            },
            onDismiss = { showAutoLockDialog = false }
        )
    }
}

/**
 * 应用设置卡片
 */
@Composable
private fun AppSettingsCard() {
    var darkModeEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var clipboardClearEnabled by remember { mutableStateOf(true) }
    
    GlassCard {
        Column {
            SettingsSectionHeader(
                title = "应用设置",
                icon = Icons.Default.Settings
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 深色模式
            SettingsToggleItem(
                title = "深色模式",
                description = "使用深色主题界面",
                icon = Icons.Default.DarkMode,
                checked = darkModeEnabled,
                onCheckedChange = { darkModeEnabled = it }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 通知设置
            SettingsToggleItem(
                title = "推送通知",
                description = "接收安全提醒和更新通知",
                icon = Icons.Default.Notifications,
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 剪贴板自动清除
            SettingsToggleItem(
                title = "剪贴板自动清除",
                description = "复制密码后30秒自动清除剪贴板",
                icon = Icons.Default.ContentPaste,
                checked = clipboardClearEnabled,
                onCheckedChange = { clipboardClearEnabled = it }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 语言设置
            SettingsClickableItem(
                title = "语言",
                description = "简体中文",
                icon = Icons.Default.Language,
                onClick = { /* TODO: 打开语言选择 */ }
            )
        }
    }
}

/**
 * 数据管理卡片
 */
@Composable
private fun DataManagementCard(
    onBackup: () -> Unit,
    onExport: () -> Unit
) {
    GlassCard {
        Column {
            SettingsSectionHeader(
                title = "数据管理",
                icon = Icons.Default.Storage
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 备份数据
            SettingsClickableItem(
                title = "备份数据",
                description = "将密码数据备份到云端",
                icon = Icons.Default.CloudUpload,
                onClick = onBackup
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 导出数据
            SettingsClickableItem(
                title = "导出数据",
                description = "导出密码数据到文件",
                icon = Icons.Default.FileDownload,
                onClick = onExport
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 导入数据
            SettingsClickableItem(
                title = "导入数据",
                description = "从其他密码管理器导入数据",
                icon = Icons.Default.FileUpload,
                onClick = { /* TODO: 打开导入页面 */ }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 清除所有数据
            SettingsClickableItem(
                title = "清除所有数据",
                description = "删除所有保存的密码（不可恢复）",
                icon = Icons.Default.DeleteForever,
                onClick = { /* TODO: 显示确认对话框 */ },
                isDestructive = true
            )
        }
    }
}

/**
 * 关于应用卡片
 */
@Composable
private fun AboutCard(
    onShowAbout: () -> Unit
) {
    GlassCard {
        Column {
            SettingsSectionHeader(
                title = "关于",
                icon = Icons.Default.Info
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 应用版本
            SettingsClickableItem(
                title = "版本信息",
                description = "v1.0.0 (Build 1)",
                icon = Icons.Default.AppSettingsAlt,
                onClick = onShowAbout
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 隐私政策
            SettingsClickableItem(
                title = "隐私政策",
                description = "了解我们如何保护您的隐私",
                icon = Icons.Default.PrivacyTip,
                onClick = { /* TODO: 打开隐私政策 */ }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 用户协议
            SettingsClickableItem(
                title = "用户协议",
                description = "查看服务条款和使用协议",
                icon = Icons.Default.Description,
                onClick = { /* TODO: 打开用户协议 */ }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 反馈建议
            SettingsClickableItem(
                title = "反馈建议",
                description = "帮助我们改进产品",
                icon = Icons.Default.Feedback,
                onClick = { /* TODO: 打开反馈页面 */ }
            )
        }
    }
}

/**
 * 设置区域标题
 */
@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = AppColors.Accent,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 设置开关项目
 */
@Composable
private fun SettingsToggleItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = AppColors.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.Accent,
                checkedTrackColor = AppColors.Accent.copy(alpha = 0.3f),
                uncheckedThumbColor = AppColors.TextSecondary,
                uncheckedTrackColor = AppColors.GlassBackground
            )
        )
    }
}

/**
 * 设置点击项目
 */
@Composable
private fun SettingsClickableItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isDestructive) AppColors.Error else AppColors.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDestructive) AppColors.Error else AppColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "进入",
            tint = AppColors.TextTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * 自动锁定时间选择对话框
 */
@Composable
private fun AutoLockTimeDialog(
    currentTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val timeOptions = listOf("立即", "30秒", "1分钟", "5分钟", "15分钟", "30分钟", "1小时")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        title = {
            Text(
                text = "自动锁定时间",
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                timeOptions.forEach { time ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTimeSelected(time) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = time == currentTime,
                            onClick = { onTimeSelected(time) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AppColors.Accent,
                                unselectedColor = AppColors.TextSecondary
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = time,
                            color = AppColors.TextPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "确定",
                    color = AppColors.Accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

/**
 * 备份对话框
 */
@Composable
private fun BackupDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        title = {
            Text(
                text = "备份数据",
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "将您的密码数据加密备份到云端。备份过程中请保持网络连接。",
                color = AppColors.TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "开始备份",
                    color = AppColors.Accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "取消",
                    color = AppColors.TextSecondary
                )
            }
        }
    )
}

/**
 * 导出对话框
 */
@Composable
private fun ExportDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFormat by remember { mutableStateOf("JSON") }
    val formats = listOf("JSON", "CSV", "XML")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        title = {
            Text(
                text = "导出数据",
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "选择导出格式：",
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                formats.forEach { format ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedFormat = format }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = format == selectedFormat,
                            onClick = { selectedFormat = format },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AppColors.Accent,
                                unselectedColor = AppColors.TextSecondary
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = format,
                            color = AppColors.TextPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedFormat) }) {
                Text(
                    text = "导出",
                    color = AppColors.Accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "取消",
                    color = AppColors.TextSecondary
                )
            }
        }
    )
}

/**
 * 关于对话框
 */
@Composable
private fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        title = {
            Text(
                text = "关于密码管理器",
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "版本：v1.0.0 (Build 1)",
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "一款安全、优雅的密码管理应用，采用最新的加密技术保护您的数据安全。",
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "© 2024 密码管理器团队",
                    color = AppColors.TextTertiary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "确定",
                    color = AppColors.Accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}