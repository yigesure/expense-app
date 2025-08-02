package com.passwordmanager.elite.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.passwordmanager.elite.ui.components.GlassCard
import com.passwordmanager.elite.ui.theme.AppColors
import com.passwordmanager.elite.ui.theme.GlassShapes

/**
 * 安全分析页面
 */
@Composable
fun SecurityAnalysisScreen(
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(SecurityTab.OVERVIEW) }
    
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(animatedAlpha)
        ) {
            // 顶部标题栏
            SecurityAnalysisTopBar()
            
            // 标签页选择器
            SecurityTabSelector(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
            
            // 内容区域
            when (selectedTab) {
                SecurityTab.OVERVIEW -> SecurityOverviewContent()
                SecurityTab.WEAK_PASSWORDS -> WeakPasswordsContent()
                SecurityTab.DUPLICATE_PASSWORDS -> DuplicatePasswordsContent()
                SecurityTab.OLD_PASSWORDS -> OldPasswordsContent()
            }
        }
    }
}

/**
 * 安全分析标签页枚举
 */
enum class SecurityTab(val title: String, val icon: ImageVector) {
    OVERVIEW("总览", Icons.Default.Dashboard),
    WEAK_PASSWORDS("弱密码", Icons.Default.Warning),
    DUPLICATE_PASSWORDS("重复密码", Icons.Default.ContentCopy),
    OLD_PASSWORDS("过期密码", Icons.Default.Schedule)
}

/**
 * 顶部标题栏
 */
@Composable
private fun SecurityAnalysisTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = AppColors.Accent,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "安全分析",
            style = MaterialTheme.typography.headlineSmall,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 标签页选择器
 */
@Composable
private fun SecurityTabSelector(
    selectedTab: SecurityTab,
    onTabSelected: (SecurityTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SecurityTab.values().forEach { tab ->
            SecurityTabItem(
                tab = tab,
                isSelected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 标签页项目
 */
@Composable
private fun SecurityTabItem(
    tab: SecurityTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBackgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.4f else 0.2f,
        animationSpec = tween(durationMillis = 200),
        label = "background_alpha"
    )
    
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.Accent else AppColors.GlassBorder,
        animationSpec = tween(durationMillis = 200),
        label = "border_color"
    )
    
    Box(
        modifier = modifier
            .clickable { onClick() }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.GlassBackground.copy(alpha = animatedBackgroundAlpha),
                        AppColors.GlassBackground.copy(alpha = animatedBackgroundAlpha * 0.7f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = animatedBorderColor.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = if (isSelected) AppColors.Accent else AppColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            
            Text(
                text = tab.title,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) AppColors.TextPrimary else AppColors.TextSecondary,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 安全总览内容
 */
@Composable
private fun SecurityOverviewContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 安全评分卡片
            SecurityScoreCard()
        }
        
        item {
            // 安全统计卡片
            SecurityStatsCard()
        }
        
        item {
            // 安全建议卡片
            SecurityRecommendationsCard()
        }
        
        item {
            // 最近安全事件
            RecentSecurityEventsCard()
        }
    }
}

/**
 * 安全评分卡片
 */
@Composable
private fun SecurityScoreCard() {
    var animatedScore by remember { mutableStateOf(0f) }
    val securityScore = 85 // 模拟安全评分
    
    LaunchedEffect(Unit) {
        animatedScore = securityScore.toFloat()
    }
    
    val animatedScoreValue by animateFloatAsState(
        targetValue = animatedScore,
        animationSpec = tween(durationMillis = 1500, easing = EaseOutCubic),
        label = "score_animation"
    )
    
    GlassCard {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "安全评分",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 圆形进度指示器
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                CircularProgressIndicator(
                    progress = animatedScoreValue / 100f,
                    modifier = Modifier.size(120.dp),
                    strokeWidth = 8.dp,
                    color = when {
                        animatedScoreValue >= 80 -> Color(0xFF4CAF50)
                        animatedScoreValue >= 60 -> Color(0xFFFF9800)
                        else -> AppColors.Error
                    },
                    trackColor = AppColors.GlassBackground.copy(alpha = 0.3f)
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${animatedScoreValue.toInt()}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                    
                    Text(
                        text = "分",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = when {
                    animatedScoreValue >= 80 -> "安全状况良好"
                    animatedScoreValue >= 60 -> "安全状况一般"
                    else -> "存在安全风险"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    animatedScoreValue >= 80 -> Color(0xFF4CAF50)
                    animatedScoreValue >= 60 -> Color(0xFFFF9800)
                    else -> AppColors.Error
                },
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 安全统计卡片
 */
@Composable
private fun SecurityStatsCard() {
    GlassCard {
        Column {
            Text(
                text = "安全统计",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecurityStatItem(
                    title = "弱密码",
                    count = 3,
                    color = AppColors.Error,
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )
                
                SecurityStatItem(
                    title = "重复密码",
                    count = 2,
                    color = Color(0xFFFF9800),
                    icon = Icons.Default.ContentCopy,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecurityStatItem(
                    title = "过期密码",
                    count = 5,
                    color = Color(0xFFFF9800),
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f)
                )
                
                SecurityStatItem(
                    title = "安全密码",
                    count = 42,
                    color = Color(0xFF4CAF50),
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 安全统计项目
 */
@Composable
private fun SecurityStatItem(
    title: String,
    count: Int,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.1f),
                        color.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 安全建议卡片
 */
@Composable
private fun SecurityRecommendationsCard() {
    val recommendations = listOf(
        SecurityRecommendation(
            title = "修复弱密码",
            description = "发现3个弱密码，建议立即更新",
            priority = SecurityPriority.HIGH,
            icon = Icons.Default.Warning
        ),
        SecurityRecommendation(
            title = "启用二次验证",
            description = "为重要账户启用双因素认证",
            priority = SecurityPriority.MEDIUM,
            icon = Icons.Default.Security
        ),
        SecurityRecommendation(
            title = "定期备份数据",
            description = "建议每周备份一次密码数据",
            priority = SecurityPriority.LOW,
            icon = Icons.Default.Backup
        )
    )
    
    GlassCard {
        Column {
            Text(
                text = "安全建议",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            recommendations.forEach { recommendation ->
                SecurityRecommendationItem(recommendation = recommendation)
                if (recommendation != recommendations.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * 安全建议项目
 */
@Composable
private fun SecurityRecommendationItem(
    recommendation: SecurityRecommendation
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: 处理建议点击 */ }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        recommendation.priority.color.copy(alpha = 0.1f),
                        recommendation.priority.color.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = recommendation.priority.color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = recommendation.icon,
            contentDescription = null,
            tint = recommendation.priority.color,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recommendation.title,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }
        
        // 优先级标签
        Box(
            modifier = Modifier
                .background(
                    color = recommendation.priority.color.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = recommendation.priority.label,
                style = MaterialTheme.typography.labelSmall,
                color = recommendation.priority.color,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * 最近安全事件卡片
 */
@Composable
private fun RecentSecurityEventsCard() {
    val securityEvents = listOf(
        SecurityEvent(
            title = "检测到弱密码",
            description = "GitHub账户密码强度不足",
            timestamp = "2小时前",
            type = SecurityEventType.WARNING
        ),
        SecurityEvent(
            title = "密码已更新",
            description = "成功更新了Gmail账户密码",
            timestamp = "1天前",
            type = SecurityEventType.SUCCESS
        ),
        SecurityEvent(
            title = "发现重复密码",
            description = "Twitter和Instagram使用相同密码",
            timestamp = "3天前",
            type = SecurityEventType.WARNING
        )
    )
    
    GlassCard {
        Column {
            Text(
                text = "最近安全事件",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            securityEvents.forEach { event ->
                SecurityEventItem(event = event)
                if (event != securityEvents.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * 安全事件项目
 */
@Composable
private fun SecurityEventItem(
    event: SecurityEvent
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = event.type.icon,
            contentDescription = null,
            tint = event.type.color,
            modifier = Modifier.size(18.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }
        
        Text(
            text = event.timestamp,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.TextTertiary
        )
    }
}

/**
 * 弱密码内容
 */
@Composable
private fun WeakPasswordsContent() {
    val weakPasswords = listOf(
        WeakPasswordItem("GitHub", "github.com", "弱密码", SecurityRisk.HIGH),
        WeakPasswordItem("测试网站", "test.com", "过于简单", SecurityRisk.HIGH),
        WeakPasswordItem("开发工具", "dev.example.com", "缺少特殊字符", SecurityRisk.MEDIUM)
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "发现 ${weakPasswords.size} 个弱密码",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "建议立即更新这些密码以提高安全性",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary
            )
        }
        
        items(weakPasswords) { item ->
            WeakPasswordCard(item = item)
        }
    }
}

/**
 * 重复密码内容
 */
@Composable
private fun DuplicatePasswordsContent() {
    val duplicateGroups = listOf(
        DuplicatePasswordGroup(
            sites = listOf("Twitter", "Instagram"),
            riskLevel = SecurityRisk.HIGH
        ),
        DuplicatePasswordGroup(
            sites = listOf("论坛A", "论坛B", "论坛C"),
            riskLevel = SecurityRisk.MEDIUM
        )
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "发现 ${duplicateGroups.size} 组重复密码",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "使用相同密码会增加安全风险",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary
            )
        }
        
        items(duplicateGroups) { group ->
            DuplicatePasswordCard(group = group)
        }
    }
}

/**
 * 过期密码内容
 */
@Composable
private fun OldPasswordsContent() {
    val oldPasswords = listOf(
        OldPasswordItem("银行账户", "bank.com", "365天前", SecurityRisk.HIGH),
        OldPasswordItem("邮箱", "email.com", "180天前", SecurityRisk.MEDIUM),
        OldPasswordItem("购物网站", "shop.com", "120天前", SecurityRisk.MEDIUM),
        OldPasswordItem("社交媒体", "social.com", "90天前", SecurityRisk.LOW),
        OldPasswordItem("工作平台", "work.com", "60天前", SecurityRisk.LOW)
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "发现 ${oldPasswords.size} 个过期密码",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "建议定期更新密码以保持安全",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary
            )
        }
        
        items(oldPasswords) { item ->
            OldPasswordCard(item = item)
        }
    }
}

/**
 * 弱密码卡片
 */
@Composable
private fun WeakPasswordCard(item: WeakPasswordItem) {
    GlassCard(
        modifier = Modifier.clickable { /* TODO: 导航到密码详情 */ }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = item.risk.color,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.siteName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = item.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
                
                Text(
                    text = item.reason,
                    style = MaterialTheme.typography.labelSmall,
                    color = item.risk.color
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = AppColors.TextTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * 重复密码卡片
 */
@Composable
private fun DuplicatePasswordCard(group: DuplicatePasswordGroup) {
    GlassCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = group.riskLevel.color,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "${group.sites.size} 个网站使用相同密码",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            group.sites.forEach { site ->
                Text(
                    text = "• $site",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
        }
    }
}

/**
 * 过期密码卡片
 */
@Composable
private fun OldPasswordCard(item: OldPasswordItem) {
    GlassCard(
        modifier = Modifier.clickable { /* TODO: 导航到密码详情 */ }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = item.risk.color,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.siteName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = item.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
                
                Text(
                    text = "上次更新: ${item.lastUpdated}",
                    style = MaterialTheme.typography.labelSmall,
                    color = item.risk.color
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = AppColors.TextTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// 数据类定义
data class SecurityRecommendation(
    val title: String,
    val description: String,
    val priority: SecurityPriority,
    val icon: ImageVector
)

data class SecurityEvent(
    val title: String,
    val description: String,
    val timestamp: String,
    val type: SecurityEventType
)

data class WeakPasswordItem(
    val siteName: String,
    val url: String,
    val reason: String,
    val risk: SecurityRisk
)

data class DuplicatePasswordGroup(
    val sites: List<String>,
    val riskLevel: SecurityRisk
)

data class OldPasswordItem(
    val siteName: String,
    val url: String,
    val lastUpdated: String,
    val risk: SecurityRisk
)

enum class SecurityPriority(val label: String, val color: Color) {
    HIGH("高", Color(0xFFE53E3E)),
    MEDIUM("中", Color(0xFFFF9800)),
    LOW("低", Color(0xFF4CAF50))
}

enum class SecurityEventType(val icon: ImageVector, val color: Color) {
    WARNING(Icons.Default.Warning, Color(0xFFFF9800)),
    SUCCESS(Icons.Default.CheckCircle, Color(0xFF4CAF50)),
    ERROR(Icons.Default.Error, Color(0xFFE53E3E))
}

enum class SecurityRisk(val color: Color) {
    HIGH(Color(0xFFE53E3E)),
    MEDIUM(Color(0xFFFF9800)),
    LOW(Color(0xFF4CAF50))
}
