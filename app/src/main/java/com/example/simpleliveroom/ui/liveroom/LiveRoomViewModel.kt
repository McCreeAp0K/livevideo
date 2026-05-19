package com.example.simpleliveroom.ui.liveroom

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.simpleliveroom.data.repository.LiveRoomRepository
import com.example.simpleliveroom.model.AnchorInfo
import com.example.simpleliveroom.model.CommentMessage

class LiveRoomViewModel(
    private val repository: LiveRoomRepository
) : ViewModel() {

    companion object {
        private const val VIEWER_COUNT_THROTTLE_MS = 1000L
        private const val COMMENT_BATCH_WINDOW_MS = 300L
    }

    private val roomId = "1001"
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _uiState = MutableLiveData(LiveRoomUiState())
    val uiState: LiveData<LiveRoomUiState> = _uiState

    private var lastViewerCountDispatchAt = 0L
    private var pendingViewerCount: Int? = null
    private val viewerCountDispatchRunnable = Runnable {
        val viewerCount = pendingViewerCount ?: return@Runnable
        pendingViewerCount = null
        dispatchViewerCount(viewerCount)
    }

    private val pendingComments = mutableListOf<CommentMessage>()
    private val commentBatchFlushRunnable = Runnable {
        flushPendingComments()
    }

    private fun currentState(): LiveRoomUiState {
        return _uiState.value ?: LiveRoomUiState()
    }

    private fun updateState(transform: (LiveRoomUiState) -> LiveRoomUiState) {
        _uiState.value = transform(currentState())
    }

    fun loadInitialData() {
        setLoading(true)

        repository.loadAnchorInfo(
            roomId = roomId,
            onSuccess = { anchorInfo ->
                updateAnchorInfo(anchorInfo)
            },
            onError = { message ->
                showError(message)
            }
        )

        repository.loadMessages(
            roomId = roomId,
            onSuccess = { comments ->
                replaceComments(comments)
                setLoading(false)
            },
            onError = { message ->
                showError(message)
                setLoading(false)
            }
        )
    }

    fun sendComment(comment: String, onSuccess: (() -> Unit)? = null) {
        if (comment.isBlank()) {
            showError("评论内容不能为空")
            return
        }

        repository.postMessage(
            roomId = roomId,
            comment = comment,
            onSuccess = { newComment ->
                addComment(newComment)
                onSuccess?.invoke()
            },
            onError = { message ->
                showError(message)
            }
        )
    }

    fun connectWebSocket() {
        repository.connectWebSocket(
            roomId = roomId,
            onViewerCountUpdate = { viewerCount ->
                updateViewerCount(viewerCount)
            },
            onCommentBatchReceived = { newComments ->
                addComments(newComments)
            }
        )
    }

    fun disconnectWebSocket() {
        repository.disconnectWebSocket()
    }

    fun updateAnchorInfo(anchorInfo: AnchorInfo) {
        updateState { state ->
            state.copy(
                anchorName = anchorInfo.name,
                anchorAvatarUrl = anchorInfo.avatar,
                followCountText = "关注 ${anchorInfo.follower_num}"
            )
        }
    }

    fun updateViewerCount(viewerCount: Int) {
        val now = SystemClock.elapsedRealtime()
        val elapsed = now - lastViewerCountDispatchAt

        if (lastViewerCountDispatchAt == 0L || elapsed >= VIEWER_COUNT_THROTTLE_MS) {
            pendingViewerCount = null
            mainHandler.removeCallbacks(viewerCountDispatchRunnable)
            dispatchViewerCount(viewerCount)
            return
        }

        pendingViewerCount = viewerCount
        mainHandler.removeCallbacks(viewerCountDispatchRunnable)
        mainHandler.postDelayed(
            viewerCountDispatchRunnable,
            VIEWER_COUNT_THROTTLE_MS - elapsed
        )
    }

    private fun dispatchViewerCount(viewerCount: Int) {
        lastViewerCountDispatchAt = SystemClock.elapsedRealtime()
        updateState { state ->
            state.copy(
                viewerCountText = "${viewerCount} 在线"
            )
        }
    }

    fun replaceComments(comments: List<CommentMessage>) {
        mainHandler.removeCallbacks(commentBatchFlushRunnable)
        val mergedComments = if (pendingComments.isEmpty()) {
            comments
        } else {
            comments + pendingComments
        }
        pendingComments.clear()

        updateState { state ->
            state.copy(
                comments = mergedComments
            )
        }
    }

    fun addComment(comment: CommentMessage) {
        updateState { state ->
            state.copy(
                comments = state.comments + comment
            )
        }
    }

    fun addComments(newComments: List<CommentMessage>) {
        if (newComments.isEmpty()) return

        pendingComments.addAll(newComments)
        mainHandler.removeCallbacks(commentBatchFlushRunnable)
        mainHandler.postDelayed(commentBatchFlushRunnable, COMMENT_BATCH_WINDOW_MS)
    }

    private fun flushPendingComments() {
        if (pendingComments.isEmpty()) return

        val batchedComments = pendingComments.toList()
        pendingComments.clear()

        updateState { state ->
            state.copy(
                comments = state.comments + batchedComments
            )
        }
    }

    fun setLoading(isLoading: Boolean) {
        updateState { state ->
            state.copy(
                isLoading = isLoading
            )
        }
    }

    fun showError(message: String) {
        updateState { state ->
            state.copy(
                errorMessage = message
            )
        }
    }

    fun clearError() {
        updateState { state ->
            state.copy(
                errorMessage = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        mainHandler.removeCallbacks(viewerCountDispatchRunnable)
        mainHandler.removeCallbacks(commentBatchFlushRunnable)
        pendingViewerCount = null
        pendingComments.clear()
        repository.disconnectWebSocket()
    }
}
