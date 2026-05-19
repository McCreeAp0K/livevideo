package com.example.simpleliveroom.ui.liveroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.simpleliveroom.data.repository.LiveRoomRepository
/**
 * LiveRoomViewModel工厂
 * 但 Android 默认创建 ViewModel 时，更擅长处理“无参构造”的情况。
 * 如果你什么都不配，系统并不知道：
 * - repository 从哪里来
 * - 该怎么 new 这个 ViewModel
 */
class LiveRoomViewModelFactory(
    private val repository: LiveRoomRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LiveRoomViewModel::class.java)) {
            return LiveRoomViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}