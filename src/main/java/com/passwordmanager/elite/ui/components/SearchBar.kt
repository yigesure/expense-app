package com.passwordmanager.elite.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passwordmanager.elite.ui.theme.AppColors

/**
 * 玻璃拟态风格搜索栏组件
 */
@Composable
fun GlassSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    placeholder: String = "搜索密码...",
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isFocused) AppColors.Accent else AppColors.GlassBorder,
        animationSpec = tween(durationMillis = 200),
        label = "border_color"
    )
    
    val animatedBackgroundAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.4f else 0.2f,
        animationSpec = tween(durationMillis = 200),
        label = "background_alpha"
    )
    
    val shape = RoundedCornerShape(16.dp)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.GlassBackground.copy(alpha = animatedBackgroundAlpha),
                        AppColors.GlassBackground.copy(alpha = animatedBackgroundAlpha * 0.7f)
                    )
                ),
                shape = shape
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = animatedBorderColor.copy(alpha = 0.6f),
                shape = shape
            )
            .clip(shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 搜索图标
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                tint = if (isFocused) AppColors.Accent else AppColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 搜索输入框
            Box(
                modifier = Modifier.weight(1f)
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    enabled = enabled,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = AppColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(AppColors.Accent),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearch(query)
                            keyboardController?.hide()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                        }
                )
                
                // 占位符文本
                if (query.isEmpty() && !isFocused) {
                    Text(
                        text = placeholder,
                        color = AppColors.TextTertiary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
            
            // 清除按钮
            if (query.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "清除",
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * 搜索建议项组件
 */
@Composable
fun SearchSuggestionItem(
    suggestion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(animatedScale)
            .clickable { 
                isPressed = true
                onClick()
            }
            .background(
                color = AppColors.GlassBackground.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = AppColors.TextTertiary,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = suggestion,
                color = AppColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}