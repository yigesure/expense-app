package com.passwordmanager.elite.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 导航项目数据类
 */
data class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

/**
 * 应用导航路由
 */
object NavigationRoutes {
    const val HOME = "home"
    const val PASSWORDS = "passwords"
    const val GENERATOR = "generator"
    const val ANALYSIS = "analysis"
    const val SETTINGS = "settings"
    const val SYNC = "sync"
    const val PASSWORD_DETAIL = "password_detail"
    const val ADD_PASSWORD = "add_password"
    const val AUTHENTICATION = "authentication"
}

/**
 * 底部导航项目列表
 */
val bottomNavigationItems = listOf(
    NavigationItem(
        route = NavigationRoutes.HOME,
        title = "首页",
        icon = Icons.Default.Home,
        selectedIcon = Icons.Filled.Home
    ),
    NavigationItem(
        route = NavigationRoutes.PASSWORDS,
        title = "密码库",
        icon = Icons.Default.Lock,
        selectedIcon = Icons.Filled.Lock
    ),
    NavigationItem(
        route = NavigationRoutes.GENERATOR,
        title = "生成器",
        icon = Icons.Default.Add,
        selectedIcon = Icons.Filled.Add
    ),
    NavigationItem(
        route = NavigationRoutes.ANALYSIS,
        title = "安全分析",
        icon = Icons.Default.Security,
        selectedIcon = Icons.Filled.Security
    ),
    NavigationItem(
        route = NavigationRoutes.SETTINGS,
        title = "设置",
        icon = Icons.Default.Settings,
        selectedIcon = Icons.Filled.Settings
    )
)