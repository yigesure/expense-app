package com.passwordmanager.elite.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passwordmanager.elite.ui.navigation.NavigationItem
import com.passwordmanager.elite.ui.theme.AppColors

/**
 * 玻璃拟态风格底部导航栏
 */
@Composable
fun GlassBottomNavigationBar(
    items: List<NavigationItem>,
    currentRoute: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.GlassBackground.copy(alpha = 0.9f),
                        AppColors.GlassBackground.copy(alpha = 0.7f)
                    )
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        AppColors.GlassHighlight.copy(alpha = 0.3f),
                        AppColors.GlassBorder.copy(alpha = 0.2f),
                        AppColors.GlassHighlight.copy(alpha = 0.3f)
                    )
                ),
                shape = shape
            )
            .clip(shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                BottomNavigationItem(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = { onItemClick(item.route) }
                )
            }
        }
    }
}

/**
 * 底部导航项目组件
 */
@Composable
private fun BottomNavigationItem(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )
    
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.Accent else AppColors.TextSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "color"
    )
    
    val animatedBackgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.3f else 0.0f,
        animationSpec = tween(durationMillis = 200),
        label = "background"
    )
    
    Column(
        modifier = modifier
            .scale(animatedScale)
            .clickable { onClick() }
            .background(
                color = AppColors.Accent.copy(alpha = animatedBackgroundAlpha),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.icon,
            contentDescription = item.title,
            tint = animatedColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = item.title,
            color = animatedColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}