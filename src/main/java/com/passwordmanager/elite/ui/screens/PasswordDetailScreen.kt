package com.passwordmanager.elite.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passwordmanager.elite.ui.components.GlassCard
import com.passwordmanager.elite.ui.components.GlassButton
import com.passwordmanager.elite.ui.theme.AppColors
import com.passwordmanager.elite.ui.theme.GlassShapes

/**
 * 密码详情页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDetailScreen(
    passwordId: String,
    onNavigateBack: () -> Unit,
    onEditPassword: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "fade_in"
    )
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    // 模拟密码数据
    val passwordData = remember {
        PasswordDetailData(
            id = passwordId,
            title = "GitHub",
            website = "https://github.com",
            username = "user@example.com",
            password = "MySecurePassword123!",
            notes = "主要开发账户，用于代码管理和协作",
            category = "开发工具",
            createdAt = "2024-01-15",
            lastModified = "2024-01-20",
            lastUsed = "2 小时前",
            strength = PasswordStrength.STRONG,
            isFavorite = true
        )
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
        ) {
            // 顶部应用栏
            DetailTopBar(
                title = passwordData.title,
                isFavorite = passwordData.isFavorite,
                onNavigateBack = onNavigateBack,
                onToggleFavorite = { /* TODO: 切换收藏状态 */ },
                onEdit = onEditPassword,
                onDelete = { showDeleteDialog = true }
            )
            
            // 内容区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 网站信息卡片
                WebsiteInfoCard(
                    website = passwordData.website,
                    category = passwordData.category
                )
                
                // 登录凭据卡片
                LoginCredentialsCard(
                    username = passwordData.username,
                    password = passwordData.password,
                    strength = passwordData.strength
                )
                
                // 备注卡片
                if (passwordData.notes.isNotEmpty()) {
                    NotesCard(notes = passwordData.notes)
                }
                
                // 安全信息卡片
                SecurityInfoCard(
                    createdAt = passwordData.createdAt,
                    lastModified = passwordData.lastModified,
                    lastUsed = passwordData.lastUsed,
                    strength = passwordData.strength
                )
                
                // 快速操作按钮
                QuickActionsCard(
                    onCopyUsername = { /* TODO: 复制用户名 */ },
                    onCopyPassword = { /* TODO: 复制密码 */ },
                    onOpenWebsite = { /* TODO: 打开网站 */ }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // 删除确认对话框
        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                onConfirm = {
                    showDeleteDialog = false
                    // TODO: 删除密码
                    onNavigateBack()
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

/**
 * 顶部应用栏
 */
@Composable
private fun DetailTopBar(
    title: String,
    isFavorite: Boolean,
    onNavigateBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .background(
                    color = AppColors.GlassBackground.copy(alpha = 0.3f),
                    shape = GlassShapes.Button
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = AppColors.TextPrimary
            )
        }
        
        // 标题
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
        
        // 操作按钮
        Row {
            // 收藏按钮
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .background(
                        color = AppColors.GlassBackground.copy(alpha = 0.3f),
                        shape = GlassShapes.Button
                    )
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "取消收藏" else "收藏",
                    tint = if (isFavorite) AppColors.Error else AppColors.TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 更多操作菜单
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .background(
                            color = AppColors.GlassBackground.copy(alpha = 0.3f),
                            shape = GlassShapes.Button
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多操作",
                        tint = AppColors.TextPrimary
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .background(
                            color = AppColors.Surface,
                            shape = GlassShapes.Card
                        )
                ) {
                    DropdownMenuItem(
                        text = { Text("编辑", color = AppColors.TextPrimary) },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = AppColors.Accent
                            )
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("删除", color = AppColors.Error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = AppColors.Error
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * 网站信息卡片
 */
@Composable
private fun WebsiteInfoCard(
    website: String,
    category: String
) {
    GlassCard {
        Column {
            Text(
                text = "网站信息",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DetailInfoRow(
                icon = Icons.Default.Language,
                label = "网站地址",
                value = website,
                copyable = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            DetailInfoRow(
                icon = Icons.Default.Category,
                label = "分类",
                value = category,
                copyable = false
            )
        }
    }
}

/**
 * 登录凭据卡片
 */
@Composable
private fun LoginCredentialsCard(
    username: String,
    password: String,
    strength: PasswordStrength
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    GlassCard {
        Column {
            Text(
                text = "登录凭据",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DetailInfoRow(
                icon = Icons.Default.Person,
                label = "用户名",
                value = username,
                copyable = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            PasswordInfoRow(
                password = password,
                isVisible = isPasswordVisible,
                onToggleVisibility = { isPasswordVisible = !isPasswordVisible },
                strength = strength
            )
        }
    }
}

/**
 * 密码信息行
 */
@Composable
private fun PasswordInfoRow(
    password: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    strength: PasswordStrength
) {
    val clipboardManager = LocalClipboardManager.current
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "密码",
            tint = AppColors.Accent,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "密码",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isVisible) password else "••••••••••••",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                
                // 密码强度指示器
                PasswordStrengthIndicator(strength = strength)
            }
        }
        
        // 可见性切换按钮
        IconButton(
            onClick = onToggleVisibility,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (isVisible) "隐藏密码" else "显示密码",
                tint = AppColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
        
        // 复制按钮
        IconButton(
            onClick = {
                clipboardManager.setText(AnnotatedString(password))
                // TODO: 显示复制成功提示
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "复制密码",
                tint = AppColors.Accent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * 详细信息行组件
 */
@Composable
private fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    copyable: Boolean = false
) {
    val clipboardManager = LocalClipboardManager.current
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AppColors.Accent,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
        
        if (copyable) {
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(value))
                    // TODO: 显示复制成功提示
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "复制",
                    tint = AppColors.Accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * 备注卡片
 */
@Composable
private fun NotesCard(notes: String) {
    GlassCard {
        Column {
            Text(
                text = "备注",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * 安全信息卡片
 */
@Composable
private fun SecurityInfoCard(
    createdAt: String,
    lastModified: String,
    lastUsed: String,
    strength: PasswordStrength
) {
    GlassCard {
        Column {
            Text(
                text = "安全信息",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DetailInfoRow(
                icon = Icons.Default.Schedule,
                label = "创建时间",
                value = createdAt
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            DetailInfoRow(
                icon = Icons.Default.Update,
                label = "最后修改",
                value = lastModified
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            DetailInfoRow(
                icon = Icons.Default.AccessTime,
                label = "最后使用",
                value = lastUsed
            )
        }
    }
}

/**
 * 快速操作卡片
 */
@Composable
private fun QuickActionsCard(
    onCopyUsername: () -> Unit,
    onCopyPassword: () -> Unit,
    onOpenWebsite: () -> Unit
) {
    GlassCard {
        Column {
            Text(
                text = "快速操作",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassButton(
                    onClick = onCopyUsername,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = AppColors.TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "复制用户名",
                        color = AppColors.TextPrimary,
                        fontSize = 12.sp
                    )
                }
                
                GlassButton(
                    onClick = onCopyPassword,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AppColors.TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "复制密码",
                        color = AppColors.TextPrimary,
                        fontSize = 12.sp
                    )
                }
                
                GlassButton(
                    onClick = onOpenWebsite,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = AppColors.TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "打开网站",
                        color = AppColors.TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * 密码强度指示器
 */
@Composable
private fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val color = when (strength) {
        PasswordStrength.WEAK -> AppColors.Error
        PasswordStrength.MEDIUM -> AppColors.Warning
        PasswordStrength.STRONG -> AppColors.Success
    }
    
    val text = when (strength) {
        PasswordStrength.WEAK -> "弱"
        PasswordStrength.MEDIUM -> "中"
        PasswordStrength.STRONG -> "强"
    }
    
    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.2f),
                shape = GlassShapes.TextField
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 删除确认对话框
 */
@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        title = {
            Text(
                text = "删除密码",
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "确定要删除这个密码吗？此操作无法撤销。",
                color = AppColors.TextSecondary
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = "删除",
                    color = AppColors.Error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "取消",
                    color = AppColors.TextSecondary
                )
            }
        }
    )
}

/**
 * 密码详情数据类
 */
private data class PasswordDetailData(
    val id: String,
    val title: String,
    val website: String,
    val username: String,
    val password: String,
    val notes: String,
    val category: String,
    val createdAt: String,
    val lastModified: String,
    val lastUsed: String,
    val strength: PasswordStrength,
    val isFavorite: Boolean
)

/**
 * 密码强度枚举
 */
private enum class PasswordStrength {
    WEAK, MEDIUM, STRONG
}