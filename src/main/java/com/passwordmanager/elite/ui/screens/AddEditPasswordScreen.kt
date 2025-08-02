package com.passwordmanager.elite.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passwordmanager.elite.ui.components.GlassCard
import com.passwordmanager.elite.ui.components.GlassButton
import com.passwordmanager.elite.ui.theme.AppColors
import com.passwordmanager.elite.ui.theme.GlassShapes

/**
 * 添加/编辑密码页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPasswordScreen(
    passwordId: String? = null, // null表示添加新密码，非null表示编辑现有密码
    onNavigateBack: () -> Unit,
    onPasswordSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    var showGeneratorDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    
    // 表单状态
    var formState by remember {
        mutableStateOf(
            PasswordFormState(
                title = "",
                website = "",
                username = "",
                password = "",
                notes = "",
                category = "其他",
                isFavorite = false
            )
        )
    }
    
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isFormValid by remember { mutableStateOf(false) }
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "fade_in"
    )
    
    // 验证表单
    LaunchedEffect(formState) {
        isFormValid = formState.title.isNotBlank() && 
                     formState.username.isNotBlank() && 
                     formState.password.isNotBlank()
    }
    
    LaunchedEffect(Unit) {
        isVisible = true
        // 如果是编辑模式，加载现有数据
        if (passwordId != null) {
            // TODO: 从数据库加载密码数据
            formState = formState.copy(
                title = "GitHub",
                website = "https://github.com",
                username = "user@example.com",
                password = "MySecurePassword123!",
                notes = "主要开发账户",
                category = "开发工具"
            )
        }
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
            AddEditTopBar(
                title = if (passwordId == null) "添加密码" else "编辑密码",
                onNavigateBack = onNavigateBack,
                onSave = {
                    if (isFormValid) {
                        // TODO: 保存密码到数据库
                        onPasswordSaved()
                    }
                },
                isFormValid = isFormValid
            )
            
            // 表单内容
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 基本信息卡片
                BasicInfoCard(
                    formState = formState,
                    onFormStateChange = { formState = it },
                    onShowCategoryDialog = { showCategoryDialog = true }
                )
                
                // 登录凭据卡片
                LoginCredentialsCard(
                    formState = formState,
                    onFormStateChange = { formState = it },
                    isPasswordVisible = isPasswordVisible,
                    onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                    onShowGenerator = { showGeneratorDialog = true }
                )
                
                // 备注卡片
                NotesCard(
                    notes = formState.notes,
                    onNotesChange = { formState = formState.copy(notes = it) }
                )
                
                // 其他选项卡片
                OptionsCard(
                    isFavorite = formState.isFavorite,
                    onToggleFavorite = { formState = formState.copy(isFavorite = !formState.isFavorite) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // 密码生成器对话框
        if (showGeneratorDialog) {
            PasswordGeneratorDialog(
                onPasswordGenerated = { generatedPassword ->
                    formState = formState.copy(password = generatedPassword)
                    showGeneratorDialog = false
                },
                onDismiss = { showGeneratorDialog = false }
            )
        }
        
        // 分类选择对话框
        if (showCategoryDialog) {
            CategorySelectionDialog(
                currentCategory = formState.category,
                onCategorySelected = { category ->
                    formState = formState.copy(category = category)
                    showCategoryDialog = false
                },
                onDismiss = { showCategoryDialog = false }
            )
        }
    }
}

/**
 * 顶部应用栏
 */
@Composable
private fun AddEditTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    isFormValid: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 取消按钮
        TextButton(onClick = onNavigateBack) {
            Text(
                text = "取消",
                color = AppColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        
        // 标题
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
        
        // 保存按钮
        TextButton(
            onClick = onSave,
            enabled = isFormValid
        ) {
            Text(
                text = "保存",
                color = if (isFormValid) AppColors.Accent else AppColors.TextTertiary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * 基本信息卡片
 */
@Composable
private fun BasicInfoCard(
    formState: PasswordFormState,
    onFormStateChange: (PasswordFormState) -> Unit,
    onShowCategoryDialog: () -> Unit
) {
    GlassCard {
        Column {
            Text(
                text = "基本信息",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 标题输入框
            GlassTextField(
                value = formState.title,
                onValueChange = { onFormStateChange(formState.copy(title = it)) },
                label = "网站名称",
                placeholder = "例如：GitHub",
                leadingIcon = Icons.Default.Language,
                isRequired = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 网站地址输入框
            GlassTextField(
                value = formState.website,
                onValueChange = { onFormStateChange(formState.copy(website = it)) },
                label = "网站地址",
                placeholder = "https://example.com",
                leadingIcon = Icons.Default.Link,
                keyboardType = KeyboardType.Uri
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 分类选择
            CategorySelector(
                selectedCategory = formState.category,
                onClick = onShowCategoryDialog
            )
        }
    }
}

/**
 * 登录凭据卡片
 */
@Composable
private fun LoginCredentialsCard(
    formState: PasswordFormState,
    onFormStateChange: (PasswordFormState) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onShowGenerator: () -> Unit
) {
    GlassCard {
        Column {
            Text(
                text = "登录凭据",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 用户名输入框
            GlassTextField(
                value = formState.username,
                onValueChange = { onFormStateChange(formState.copy(username = it)) },
                label = "用户名/邮箱",
                placeholder = "user@example.com",
                leadingIcon = Icons.Default.Person,
                isRequired = true,
                keyboardType = KeyboardType.Email
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 密码输入框
            PasswordTextField(
                value = formState.password,
                onValueChange = { onFormStateChange(formState.copy(password = it)) },
                isVisible = isPasswordVisible,
                onToggleVisibility = onTogglePasswordVisibility,
                onShowGenerator = onShowGenerator
            )
        }
    }
}

/**
 * 备注卡片
 */
@Composable
private fun NotesCard(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    GlassCard {
        Column {
            Text(
                text = "备注",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            GlassTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = "备注信息",
                placeholder = "添加备注信息（可选）",
                leadingIcon = Icons.Default.Notes,
                maxLines = 4,
                singleLine = false
            )
        }
    }
}

/**
 * 其他选项卡片
 */
@Composable
private fun OptionsCard(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    GlassCard {
        Column {
            Text(
                text = "其他选项",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 收藏选项
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleFavorite() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "收藏",
                    tint = if (isFavorite) AppColors.Error else AppColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "添加到收藏",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "收藏的密码会显示在首页",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Switch(
                    checked = isFavorite,
                    onCheckedChange = { onToggleFavorite() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppColors.Accent,
                        checkedTrackColor = AppColors.Accent.copy(alpha = 0.3f),
                        uncheckedThumbColor = AppColors.TextSecondary,
                        uncheckedTrackColor = AppColors.GlassBackground
                    )
                )
            }
        }
    }
}

/**
 * 玻璃拟态输入框
 */
@Composable
private fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    isRequired: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = 1,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
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
    
    Column(modifier = modifier) {
        // 标签
        Row {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
            if (isRequired) {
                Text(
                    text = " *",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Error
                )
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // 输入框
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AppColors.GlassBackground.copy(alpha = animatedBackgroundAlpha),
                            AppColors.GlassBackground.copy(alpha = animatedBackgroundAlpha * 0.7f)
                        )
                    ),
                    shape = GlassShapes.TextField
                )
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = animatedBorderColor.copy(alpha = 0.6f),
                    shape = GlassShapes.TextField
                )
                .clip(GlassShapes.TextField)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 前置图标
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isFocused) AppColors.Accent else AppColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                // 输入框
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        placeholder = {
                            Text(
                                text = placeholder,
                                color = AppColors.TextTertiary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        singleLine = singleLine,
                        maxLines = maxLines,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = keyboardType,
                            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColors.TextPrimary,
                            unfocusedTextColor = AppColors.TextPrimary,
                            focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            cursorColor = AppColors.Accent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                            }
                    )
                }
            }
        }
    }
}

/**
 * 密码输入框
 */
@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onShowGenerator: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
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
    
    Column {
        // 标签和生成器按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                Text(
                    text = "密码",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = " *",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Error
                )
            }
            
            TextButton(
                onClick = onShowGenerator,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AppColors.Accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "生成密码",
                    color = AppColors.Accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // 密码输入框
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AppColors.GlassBackground.copy(alpha = animatedBackgroundAlpha),
                            AppColors.GlassBackground.copy(alpha = animatedBackgroundAlpha * 0.7f)
                        )
                    ),
                    shape = GlassShapes.TextField
                )
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = animatedBorderColor.copy(alpha = 0.6f),
                    shape = GlassShapes.TextField
                )
                .clip(GlassShapes.TextField)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 锁图标
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isFocused) AppColors.Accent else AppColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 密码输入框
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(
                            text = "输入密码",
                            color = AppColors.TextTertiary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppColors.TextPrimary,
                        unfocusedTextColor = AppColors.TextPrimary,
                        focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        cursorColor = AppColors.Accent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                        }
                )
                
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
            }
        }
    }
}

/**
 * 分类选择器
 */
@Composable
private fun CategorySelector(
    selectedCategory: String,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = "分类",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSecondary,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AppColors.GlassBackground.copy(alpha = 0.2f),
                            AppColors.GlassBackground.copy(alpha = 0.14f)
                        )
                    ),
                    shape = GlassShapes.TextField
                )
                .border(
                    width = 1.dp,
                    color = AppColors.GlassBorder.copy(alpha = 0.6f),
                    shape = GlassShapes.TextField
                )
                .clip(GlassShapes.TextField)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = selectedCategory,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextPrimary
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = AppColors.TextSecondary
                )
            }
        }
    }
}

/**
 * 密码生成器对话框
 */
@Composable
private fun PasswordGeneratorDialog(
    onPasswordGenerated: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var length by remember { mutableStateOf(16) }
    var includeUppercase by remember { mutableStateOf(true) }
    var includeLowercase by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(true) }
    var generatedPassword by remember { mutableStateOf("") }
    
    // 生成密码的简单实现
    fun generatePassword(): String {
        val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercase = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        
        var chars = ""
        if (includeUppercase) chars += uppercase
        if (includeLowercase) chars += lowercase
        if (includeNumbers) chars += numbers
        if (includeSymbols) chars += symbols
        
        return if (chars.isNotEmpty()) {
            (1..length).map { chars.random() }.joinToString("")
        } else {
            "Password123!"
        }
    }
    
    LaunchedEffect(length, includeUppercase, includeLowercase, includeNumbers, includeSymbols) {
        generatedPassword = generatePassword()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        title = {
            Text(
                text = "密码生成器",
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // 生成的密码显示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = AppColors.GlassBackground.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = generatedPassword,
                        color = AppColors.TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 长度滑块
                Text(
                    text = "长度: $length",
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Slider(
                    value = length.toFloat(),
                    onValueChange = { length = it.toInt() },
                    valueRange = 8f..32f,
                    colors = SliderDefaults.colors(
                        thumbColor = AppColors.Accent,
                        activeTrackColor = AppColors.Accent,
                        inactiveTrackColor = AppColors.GlassBackground
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 字符类型选项
                GeneratorOption("大写字母", includeUppercase) { includeUppercase = it }
                GeneratorOption("小写字母", includeLowercase) { includeLowercase = it }
                GeneratorOption("数字", includeNumbers) { includeNumbers = it }
                GeneratorOption("符号", includeSymbols) { includeSymbols = it }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onPasswordGenerated(generatedPassword) }
            ) {
                Text(
                    text = "使用此密码",
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
 * 生成器选项组件
 */
@Composable
private fun GeneratorOption(
    label: String,
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
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = AppColors.Accent,
                uncheckedColor = AppColors.TextSecondary,
                checkmarkColor = AppColors.TextPrimary
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = label,
            color = AppColors.TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 分类选择对话框
 */
@Composable
private fun CategorySelectionDialog(
    currentCategory: String,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = listOf(
        "社交媒体",
        "邮箱",
        "银行金融",
        "购物网站",
        "开发工具",
        "工作相关",
        "娱乐",
        "教育",
        "其他"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        title = {
            Text(
                text = "选择分类",
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                categories.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategorySelected(category) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = category == currentCategory,
                            onClick = { onCategorySelected(category) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AppColors.Accent,
                                unselectedColor = AppColors.TextSecondary
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = category,
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
 * 密码表单状态数据类
 */
private data class PasswordFormState(
    val title: String,
    val website: String,
    val username: String,
    val password: String,
    val notes: String,
    val category: String,
    val isFavorite: Boolean
)
