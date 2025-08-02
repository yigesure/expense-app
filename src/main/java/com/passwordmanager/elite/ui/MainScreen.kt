package com.passwordmanager.elite.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.passwordmanager.elite.ui.components.BottomNavigationBar
import com.passwordmanager.elite.ui.navigation.NavigationRoutes
import com.passwordmanager.elite.ui.navigation.bottomNavigationItems
import com.passwordmanager.elite.ui.screens.HomeScreen
import com.passwordmanager.elite.ui.screens.PasswordDetailScreen
import com.passwordmanager.elite.ui.screens.AddEditPasswordScreen
import com.passwordmanager.elite.ui.screens.SettingsScreen
import com.passwordmanager.elite.ui.screens.AuthenticationScreen
import com.passwordmanager.elite.ui.screens.SecurityAnalysisScreen
import com.passwordmanager.elite.ui.screens.SyncScreen
import com.passwordmanager.elite.ui.theme.AppColors
import com.passwordmanager.elite.domain.service.SyncService
import org.koin.androidx.compose.koinInject

/**
 * 主界面容器
 * 包含底部导航和页面内容
 */
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    syncService: SyncService = koinInject()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavigationRoutes.HOME
    
    Scaffold(
        containerColor = AppColors.MidnightBlue,
        bottomBar = {
            // 只在主要页面显示底部导航栏
            if (shouldShowBottomBar(currentRoute)) {
                GlassBottomNavigationBar(
                    items = bottomNavigationItems,
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                // 清除回退栈到首页
                                popUpTo(NavigationRoutes.HOME) {
                                    saveState = true
                                }
                                // 避免重复导航到同一页面
                                launchSingleTop = true
                                // 恢复状态
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AppColors.MidnightBlue,
                            AppColors.CyanBlue.copy(alpha = 0.2f),
                            AppColors.MidnightBlue
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            NavigationHost(navController = navController)
        }
    }
}

/**
 * 导航主机
 */
@Composable
private fun NavigationHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.HOME
    ) {
        // 首页
        composable(NavigationRoutes.HOME) {
            HomeScreen(
                onNavigateToPasswords = {
                    navController.navigate(NavigationRoutes.PASSWORDS)
                },
                onNavigateToGenerator = {
                    navController.navigate(NavigationRoutes.GENERATOR)
                },
                onNavigateToAnalysis = {
                    navController.navigate(NavigationRoutes.ANALYSIS)
                },
                onNavigateToSettings = {
                    navController.navigate(NavigationRoutes.SETTINGS)
                }
            )
        }
        
        // 密码库页面
        composable(NavigationRoutes.PASSWORDS) {
            PasswordsScreen(
                onNavigateToDetail = { passwordId ->
                    navController.navigate("${NavigationRoutes.PASSWORD_DETAIL}/$passwordId")
                },
                onNavigateToAdd = {
                    navController.navigate(NavigationRoutes.ADD_PASSWORD)
                }
            )
        }
        
        // 密码生成器页面
        composable(NavigationRoutes.GENERATOR) {
            GeneratorScreen()
        }
        
        // 安全分析页面
        composable(NavigationRoutes.ANALYSIS) {
            AnalysisScreen()
        }
        
        // 设置页面
        composable(NavigationRoutes.SETTINGS) {
            SettingsScreen()
        }
        
        // 数据同步页面
        composable(NavigationRoutes.SYNC) {
            SyncScreen(syncService = syncService)
        }
        
        // 密码详情页面
        composable("${NavigationRoutes.PASSWORD_DETAIL}/{passwordId}") { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getString("passwordId") ?: ""
            PasswordDetailScreen(
                passwordId = passwordId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditPassword = {
                    navController.navigate("${NavigationRoutes.PASSWORD_DETAIL}/$passwordId/edit")
                }
            )
        }
        
        // 添加密码页面
        composable(NavigationRoutes.ADD_PASSWORD) {
            AddEditPasswordScreen(
                passwordId = null, // null表示添加新密码
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPasswordSaved = {
                    navController.popBackStack()
                }
            )
        }
        
        // 编辑密码页面
        composable("${NavigationRoutes.PASSWORD_DETAIL}/{passwordId}/edit") { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getString("passwordId") ?: ""
            AddEditPasswordScreen(
                passwordId = passwordId, // 非null表示编辑现有密码
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPasswordSaved = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * 判断是否应该显示底部导航栏
 */
private fun shouldShowBottomBar(route: String): Boolean {
    return when {
        route.startsWith(NavigationRoutes.PASSWORD_DETAIL) -> false
        route == NavigationRoutes.ADD_PASSWORD -> false
        route == NavigationRoutes.AUTHENTICATION -> false
        else -> true
    }
}

/**
 * 临时占位页面组件
 */
@Composable
private fun PasswordsScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAdd: () -> Unit
) {
    PlaceholderScreen(
        title = "密码库",
        description = "这里将显示您保存的所有密码"
    )
}

@Composable
private fun GeneratorScreen() {
    PlaceholderScreen(
        title = "密码生成器",
        description = "生成强密码的工具"
    )
}

@Composable
private fun AnalysisScreen() {
    SecurityAnalysisScreen()
}



/**
 * 占位页面组件
 */
@Composable
private fun PlaceholderScreen(
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = AppColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary
            )
        }
    }
}