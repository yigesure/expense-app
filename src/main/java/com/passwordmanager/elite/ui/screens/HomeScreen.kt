package com.passwordmanager.elite.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.passwordmanager.elite.ui.components.GlassSearchBar
import com.passwordmanager.elite.ui.theme.AppColors

/**
 * 主界面首页
 */
@Composable
fun HomeScreen(
    onNavigateToPasswords: () -> Unit,
    onNavigateToGenerator: () -> Unit,
    onNavigateToAnalysis: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isVisible by remember { mutableStateOf(false) }
    
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .alpha(animatedAlpha)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 欢迎标题
            item {
                WelcomeHeader()
            }
            
            // 搜索栏
            item {
                GlassSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { /* TODO: 实现搜索功能 */ },
                    placeholder = "搜索您的密码..."
                )
            }
            
            // 快速操作卡片
            item {
                QuickActionCards(
                    onNavigateToPasswords = onNavigateToPasswords,
                    onNavigateToGenerator = onNavigateToGenerator,
                    onNavigateToAnalysis = onNavigateToAnalysis
                )
            }
            
            // 安全状态概览
            item {
                SecurityOverviewCard()
            }
            
            // 最近使用的密码
            item {
                RecentPasswordsCard()
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

/**
 * 欢迎标题组件
 */
@Composable
private fun WelcomeHeader() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "欢迎回来",
            style = MaterialTheme.typography.headlineLarge,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "您的密码安全管家",
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.TextSecondary
        )
    }
}

/**
 * 快速操作卡片组
 */
@Composable
private fun QuickActionCards(
    onNavigateToPasswords: () -> Unit,
    onNavigateToGenerator: () -> Unit,
    onNavigateToAnalysis: () -> Unit
) {
    Column {
        Text(
            text = "快速操作",
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(
                listOf(
                    QuickAction("密码库", Icons.Default.Lock, "管理您的密码", onNavigateToPasswords),
                    QuickAction("生成器", Icons.Default.Add, "创建强密码", onNavigateToGenerator),
                    QuickAction("安全分析", Icons.Default.Security, "检查安全状态", onNavigateToAnalysis)
                )
            ) { action ->
                QuickActionCard(action = action)
            }
        }
    }
}

/**
 * 快速操作数据类
 */
private data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val description: String,
    val onClick: () -> Unit
)

/**
 * 快速操作卡片
 */
@Composable
private fun QuickActionCard(
    action: QuickAction,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.width(140.dp),
        onClick = action.onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                tint = AppColors.Accent,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = action.title,
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = action.description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * 安全状态概览卡片
 */
@Composable
private fun SecurityOverviewCard() {
    GlassCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "安全状态",
                    style = MaterialTheme.typography.titleLarge,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "安全状态",
                    tint = AppColors.Success,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SecurityStatusItem(
                    title = "强密码",
                    count = "85%",
                    color = AppColors.Success
                )
                
                SecurityStatusItem(
                    title = "重复密码",
                    count = "3",
                    color = AppColors.Warning
                )
                
                SecurityStatusItem(
                    title = "弱密码",
                    count = "2",
                    color = AppColors.Error
                )
            }
        }
    }
}

/**
 * 安全状态项目
 */
@Composable
private fun SecurityStatusItem(
    title: String,
    count: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.headlineSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSecondary
        )
    }
}

/**
 * 最近使用密码卡片
 */
@Composable
private fun RecentPasswordsCard() {
    GlassCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "最近使用",
                    style = MaterialTheme.typography.titleLarge,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                
                TextButton(
                    onClick = { /* TODO: 查看全部 */ }
                ) {
                    Text(
                        text = "查看全部",
                        color = AppColors.Accent,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 示例最近使用项目
            repeat(3) { index ->
                RecentPasswordItem(
                    title = "示例网站 ${index + 1}",
                    username = "user@example.com",
                    lastUsed = "${index + 1} 小时前"
                )
                
                if (index < 2) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * 最近使用密码项目
 */
@Composable
private fun RecentPasswordItem(
    title: String,
    username: String,
    lastUsed: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Language,
            contentDescription = title,
            tint = AppColors.Accent,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = username,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }
        
        Text(
            text = lastUsed,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextTertiary,
            fontSize = 10.sp
        )
    }
}