package com.passwordmanager.elite.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passwordmanager.elite.data.model.SyncStatus
import com.passwordmanager.elite.domain.service.*
import com.passwordmanager.elite.ui.components.GlassCard
import com.passwordmanager.elite.ui.components.GlassButton
import com.passwordmanager.elite.ui.theme.AppColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 数据同步管理页面
 */
@Composable
fun SyncScreen(
    syncService: SyncService,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    var showConflictDialog by remember { mutableStateOf(false) }
    var conflicts by remember { mutableStateOf<List<SyncConflict>>(emptyList()) }
    var syncSettings by remember { mutableStateOf(syncService.getSyncSettings()) }
    
    val syncStatus by syncService.syncStatus.collectAsState()
    val syncProgress by syncService.syncProgress.collectAsState()
    val lastSyncTime by syncService.lastSyncTime.collectAsState()
    
    val scope = rememberCoroutineScope()
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .alpha(animatedAlpha)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // 顶部标题
                SyncScreenHeader()
            }
            
            item {
                // 同步状态卡片
                SyncStatusCard(
                    syncStatus = syncStatus,
                    syncProgress = syncProgress,
                    lastSyncTime = lastSyncTime
                )
            }
            
            item {
                // 同步操作按钮
                SyncActionsCard(
                    syncStatus = syncStatus,
                    onSyncNow = {
                        scope.launch {
                            // TODO: 获取当前密码列表
                            val passwords = emptyList<com.passwordmanager.elite.data.model.PasswordEntry>()
                            syncService.startSync(passwords)
                        }
                    },
                    onPullFromCloud = {
                        scope.launch {
                            syncService.pullFromCloud()
                        }
                    },
                    onCheckConflicts = {
                        scope.launch {
                            // TODO: 获取当前密码列表
                            val passwords = emptyList<com.passwordmanager.elite.data.model.PasswordEntry>()
                            conflicts = syncService.checkSyncConflicts(passwords)
                            if (conflicts.isNotEmpty()) {
                                showConflictDialog = true
                            }
                        }
                    }
                )
            }
            
            item {
                // 同步设置卡片
                SyncSettingsCard(
                    settings = syncSettings,
                    onSettingsChanged = { newSettings ->
                        syncSettings = newSettings
                        syncService.updateSyncSettings(newSettings)
                    }
                )
            }
            
            item {
                // 同步历史卡片
                SyncHistoryCard()
            }
            
            item {
                // 设备管理卡片
                DeviceManagementCard()
            }
        }
        
        // 冲突解决对话框
        if (showConflictDialog) {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onDismiss = { showConflictDialog = false },
                onResolve = { resolutions ->
                    scope.launch {
                        syncService.resolveConflicts(conflicts, resolutions)
                        showConflictDialog = false
                    }
                }
            )
        }
    }
}

/**
 * 同步页面标题
 */
@Composable
private fun SyncScreenHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Sync,
            contentDescription = null,
            tint = AppColors.Accent,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "数据同步",
            style = MaterialTheme.typography.headlineSmall,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 同步状态卡片
 */
@Composable
private fun SyncStatusCard(
    syncStatus: SyncStatus,
    syncProgress: Float,
    lastSyncTime: Long?
) {
    GlassCard {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "同步状态",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 状态图标和文本
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SyncStatusIcon(syncStatus = syncStatus)
                
                Column {
                    Text(
                        text = getSyncStatusText(syncStatus),
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (lastSyncTime != null) {
                        Text(
                            text = "上次同步: ${formatSyncTime(lastSyncTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondary
                        )
                    }
                }
            }
            
            // 进度条
            if (syncStatus == SyncStatus.SYNCING && syncProgress > 0f) {
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = syncProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AppColors.Accent,
                    trackColor = AppColors.GlassBackground.copy(alpha = 0.3f)
                )
                
                Text(
                    text = "${(syncProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * 同步状态图标
 */
@Composable
private fun SyncStatusIcon(syncStatus: SyncStatus) {
    val (icon, color) = when (syncStatus) {
        SyncStatus.IDLE -> Icons.Default.CloudOff to AppColors.TextSecondary
        SyncStatus.SYNCING -> Icons.Default.Sync to AppColors.Accent
        SyncStatus.SUCCESS -> Icons.Default.CloudDone to Color(0xFF4CAF50)
        SyncStatus.ERROR -> Icons.Default.CloudOff to AppColors.Error
        SyncStatus.RESOLVING_CONFLICTS -> Icons.Default.Warning to Color(0xFFFF9800)
    }
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.2f),
                        color.copy(alpha = 0.1f)
                    )
                ),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.3f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 同步操作卡片
 */
@Composable
private fun SyncActionsCard(
    syncStatus: SyncStatus,
    onSyncNow: () -> Unit,
    onPullFromCloud: () -> Unit,
    onCheckConflicts: () -> Unit
) {
    GlassCard {
        Column {
            Text(
                text = "同步操作",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassButton(
                    text = "立即同步",
                    icon = Icons.Default.CloudUpload,
                    onClick = onSyncNow,
                    enabled = syncStatus != SyncStatus.SYNCING,
                    modifier = Modifier.weight(1f)
                )
                
                GlassButton(
                    text = "拉取数据",
                    icon = Icons.Default.CloudDownload,
                    onClick = onPullFromCloud,
                    enabled = syncStatus != SyncStatus.SYNCING,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            GlassButton(
                text = "检查冲突",
                icon = Icons.Default.CompareArrows,
                onClick = onCheckConflicts,
                enabled = syncStatus != SyncStatus.SYNCING,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 同步设置卡片
 */
@Composable
private fun SyncSettingsCard(
    settings: SyncSettings,
    onSettingsChanged: (SyncSettings) -> Unit
) {
    GlassCard {
        Column {
            Text(
                text = "同步设置",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 自动同步开关
            SyncSettingItem(
                title = "自动同步",
                description = "定期自动同步数据",
                checked = settings.autoSyncEnabled,
                onCheckedChange = { enabled ->
                    onSettingsChanged(settings.copy(autoSyncEnabled = enabled))
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 仅WiFi同步开关
            SyncSettingItem(
                title = "仅WiFi同步",
                description = "只在WiFi网络下进行同步",
                checked = settings.wifiOnlySync,
                onCheckedChange = { enabled ->
                    onSettingsChanged(settings.copy(wifiOnlySync = enabled))
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 加密传输开关
            SyncSettingItem(
                title = "加密传输",
                description = "使用端到端加密保护数据",
                checked = settings.encryptionEnabled,
                onCheckedChange = { enabled ->
                    onSettingsChanged(settings.copy(encryptionEnabled = enabled))
                }
            )
        }
    }
}

/**
 * 同步设置项目
 */
@Composable
private fun SyncSettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                uncheckedTrackColor = AppColors.GlassBackground.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * 同步历史卡片
 */
@Composable
private fun SyncHistoryCard() {
    val syncHistory = remember {
        listOf(
            SyncHistoryItem("成功同步", "同步了42个密码", "2小时前", SyncHistoryType.SUCCESS),
            SyncHistoryItem("解决冲突", "解决了3个冲突", "1天前", SyncHistoryType.CONFLICT_RESOLVED),
            SyncHistoryItem("同步失败", "网络连接超时", "2天前", SyncHistoryType.ERROR),
            SyncHistoryItem("成功同步", "同步了38个密码", "3天前", SyncHistoryType.SUCCESS)
        )
    }
    
    GlassCard {
        Column {
            Text(
                text = "同步历史",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            syncHistory.forEach { item ->
                SyncHistoryItemView(item = item)
                if (item != syncHistory.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * 同步历史项目视图
 */
@Composable
private fun SyncHistoryItemView(item: SyncHistoryItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.type.icon,
            contentDescription = null,
            tint = item.type.color,
            modifier = Modifier.size(18.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }
        
        Text(
            text = item.timestamp,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.TextTertiary
        )
    }
}

/**
 * 设备管理卡片
 */
@Composable
private fun DeviceManagementCard() {
    val connectedDevices = remember {
        listOf(
            ConnectedDevice("iPhone 13", "iOS 16.0", "2小时前", true),
            ConnectedDevice("MacBook Pro", "macOS 13.0", "1天前", false),
            ConnectedDevice("iPad Air", "iPadOS 16.0", "3天前", false)
        )
    }
    
    GlassCard {
        Column {
            Text(
                text = "已连接设备",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            connectedDevices.forEach { device ->
                ConnectedDeviceItem(device = device)
                if (device != connectedDevices.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * 已连接设备项目
 */
@Composable
private fun ConnectedDeviceItem(device: ConnectedDevice) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = getDeviceIcon(device.name),
            contentDescription = null,
            tint = if (device.isCurrentDevice) AppColors.Accent else AppColors.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                
                if (device.isCurrentDevice) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = AppColors.Accent.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "当前设备",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.Accent,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            
            Text(
                text = "${device.platform} • 上次同步: ${device.lastSync}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }
    }
}

/**
 * 冲突解决对话框
 */
@Composable
private fun ConflictResolutionDialog(
    conflicts: List<SyncConflict>,
    onDismiss: () -> Unit,
    onResolve: (Map<String, ConflictResolution>) -> Unit
) {
    var resolutions by remember {
        mutableStateOf(
            conflicts.associate { it.passwordId to ConflictResolution.KEEP_LOCAL }
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "解决同步冲突",
                color = AppColors.TextPrimary
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(conflicts) { conflict ->
                    ConflictResolutionItem(
                        conflict = conflict,
                        resolution = resolutions[conflict.passwordId] ?: ConflictResolution.KEEP_LOCAL,
                        onResolutionChanged = { newResolution ->
                            resolutions = resolutions + (conflict.passwordId to newResolution)
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onResolve(resolutions) }
            ) {
                Text("解决冲突", color = AppColors.Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = AppColors.TextSecondary)
            }
        },
        containerColor = AppColors.GlassBackground,
        tonalElevation = 0.dp
    )
}

/**
 * 冲突解决项目
 */
@Composable
private fun ConflictResolutionItem(
    conflict: SyncConflict,
    resolution: ConflictResolution,
    onResolutionChanged: (ConflictResolution) -> Unit
) {
    Column {
        Text(
            text = conflict.localEntry?.siteName ?: conflict.cloudEntry?.siteName ?: "未知网站",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = getConflictDescription(conflict.conflictType),
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ConflictResolution.values().forEach { option ->
                FilterChip(
                    selected = resolution == option,
                    onClick = { onResolutionChanged(option) },
                    label = {
                        Text(
                            text = getResolutionText(option),
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppColors.Accent.copy(alpha = 0.2f),
                        selectedLabelColor = AppColors.Accent
                    )
                )
            }
        }
    }
}

// 辅助函数

private fun getSyncStatusText(status: SyncStatus): String {
    return when (status) {
        SyncStatus.IDLE -> "空闲"
        SyncStatus.SYNCING -> "同步中..."
        SyncStatus.SUCCESS -> "同步成功"
        SyncStatus.ERROR -> "同步失败"
        SyncStatus.RESOLVING_CONFLICTS -> "解决冲突中..."
    }
}

private fun formatSyncTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun getDeviceIcon(deviceName: String): ImageVector {
    return when {
        deviceName.contains("iPhone", ignoreCase = true) -> Icons.Default.PhoneIphone
        deviceName.contains("iPad", ignoreCase = true) -> Icons.Default.Tablet
        deviceName.contains("Mac", ignoreCase = true) -> Icons.Default.Computer
        else -> Icons.Default.DeviceUnknown
    }
}

private fun getConflictDescription(type: ConflictType): String {
    return when (type) {
        ConflictType.MODIFIED_BOTH -> "本地和云端都有修改"
        ConflictType.LOCAL_ONLY -> "仅本地存在"
        ConflictType.CLOUD_ONLY -> "仅云端存在"
    }
}

private fun getResolutionText(resolution: ConflictResolution): String {
    return when (resolution) {
        ConflictResolution.KEEP_LOCAL -> "保留本地"
        ConflictResolution.KEEP_CLOUD -> "保留云端"
        ConflictResolution.MERGE -> "合并"
    }
}

// 数据类

data class SyncHistoryItem(
    val title: String,
    val description: String,
    val timestamp: String,
    val type: SyncHistoryType
)

enum class SyncHistoryType(val icon: ImageVector, val color: Color) {
    SUCCESS(Icons.Default.CheckCircle, Color(0xFF4CAF50)),
    ERROR(Icons.Default.Error, Color(0xFFE53E3E)),
    CONFLICT_RESOLVED(Icons.Default.Warning, Color(0xFFFF9800))
}

data class ConnectedDevice(
    val name: String,
    val platform: String,
    val lastSync: String,
    val isCurrentDevice: Boolean
)