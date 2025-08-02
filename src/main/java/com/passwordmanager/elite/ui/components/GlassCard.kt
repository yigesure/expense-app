package com.passwordmanager.elite.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.passwordmanager.elite.ui.theme.AppColors
import com.passwordmanager.elite.ui.theme.GlassShapes

/**
 * 玻璃拟态卡片组件
 * 实现半透明背景、模糊效果和柔光边框
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 16.dp,
    blurRadius: Dp = 10.dp,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = AppColors.GlassBackground,
    borderColor: Color = AppColors.GlassBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    Card(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor.copy(alpha = 0.3f),
                            backgroundColor.copy(alpha = 0.1f)
                        )
                    ),
                    shape = shape
                )
                .border(
                    width = borderWidth,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AppColors.GlassHighlight.copy(alpha = 0.5f),
                            borderColor.copy(alpha = 0.3f)
                        )
                    ),
                    shape = shape
                )
                .clip(shape)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

/**
 * 玻璃拟态按钮组件
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = AppColors.CyanBlue.copy(alpha = 0.3f),
    content: @Composable RowScope.() -> Unit
) {
    val shape = GlassShapes.Button
    
    Box(
        modifier = modifier
            .clickable(enabled = enabled) { onClick() }
            .background(
                brush = Brush.verticalGradient(
                    colors = if (enabled) {
                        listOf(
                            backgroundColor.copy(alpha = 0.4f),
                            backgroundColor.copy(alpha = 0.2f)
                        )
                    } else {
                        listOf(
                            AppColors.TextTertiary.copy(alpha = 0.2f),
                            AppColors.TextTertiary.copy(alpha = 0.1f)
                        )
                    }
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.GlassHighlight.copy(alpha = 0.4f),
                        AppColors.GlassBorder.copy(alpha = 0.2f)
                    )
                ),
                shape = shape
            )
            .clip(shape)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

/**
 * 玻璃拟态输入框背景
 */
@Composable
fun GlassTextField(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = GlassShapes.TextField
    
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.GlassBackground.copy(alpha = 0.4f),
                        AppColors.GlassBackground.copy(alpha = 0.2f)
                    )
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                color = AppColors.GlassBorder.copy(alpha = 0.3f),
                shape = shape
            )
            .clip(shape)
    ) {
        content()
    }
}