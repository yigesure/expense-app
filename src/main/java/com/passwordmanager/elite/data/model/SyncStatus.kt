package com.passwordmanager.elite.data.model

/**
 * 同步状态枚举
 */
enum class SyncStatus {
    IDLE,                    // 空闲状态
    SYNCING,                 // 同步中
    SUCCESS,                 // 同步成功
    ERROR,                   // 同步失败
    RESOLVING_CONFLICTS      // 解决冲突中
}