package com.example.simpleliveroom.ui.liveroom

import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simpleliveroom.R
import com.example.simpleliveroom.data.repository.LiveRoomRepository
import com.facebook.drawee.view.SimpleDraweeView

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "FirstFrameTrace"
    }

    private var player: ExoPlayer? = null
    private var hasStartedRoomData = false
    private var hasHiddenLoading = false
    private var renderedCommentCount = 0
    private var firstFrameTrace = FirstFrameTrace()

    private val viewModel: LiveRoomViewModel by viewModels {
        LiveRoomViewModelFactory(LiveRoomRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val rvComments = findViewById<RecyclerView>(R.id.rvComments)
        val etComment = findViewById<EditText>(R.id.etComment)
        val btnSend = findViewById<Button>(R.id.btnSend)
        val playerView = findViewById<PlayerView>(R.id.playerView)
        val videoLoading = findViewById<View>(R.id.videoLoading)
        val tvAnchorName = findViewById<TextView>(R.id.tvAnchorName)
        val tvFollowCount = findViewById<TextView>(R.id.tvFollowCount)
        val tvViewerCount = findViewById<TextView>(R.id.tvViewerCount)
        val ivAnchorAvatar = findViewById<SimpleDraweeView>(R.id.ivAnchorAvatar)

        val commentAdapter = CommentAdapter(mutableListOf())

        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = commentAdapter

        /**
         * - 监听 UI 状态变化
         */
        viewModel.uiState.observe(this) { state ->
            tvAnchorName.text = state.anchorName
            tvFollowCount.text = state.followCountText
            tvViewerCount.text = state.viewerCountText

            if (state.anchorAvatarUrl.isNotBlank()) {
                ivAnchorAvatar.setImageURI(Uri.parse(state.anchorAvatarUrl))
            }

            when {
                state.comments.size < renderedCommentCount -> {
                    commentAdapter.replaceComments(state.comments)
                    renderedCommentCount = state.comments.size
                    scrollCommentsToBottom(rvComments, renderedCommentCount)
                }

                state.comments.size > renderedCommentCount -> {
                    val newComments = state.comments.subList(
                        renderedCommentCount,
                        state.comments.size
                    )
                    if (renderedCommentCount == 0) {
                        commentAdapter.replaceComments(state.comments)
                    } else {
                        commentAdapter.addComments(newComments)
                    }
                    renderedCommentCount = state.comments.size
                    scrollCommentsToBottom(rvComments, renderedCommentCount)
                }
            }

            state.errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        setupPlayer(playerView, videoLoading)

        btnSend.setOnClickListener {
            val inputText = etComment.text.toString().trim()

            viewModel.sendComment(inputText) {
                etComment.text.clear()
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayer(playerView: PlayerView, videoLoading: View) {
        // 从开始搭建播放器链路起记时，用来衡量建链到首帧的端到端耗时。
        firstFrameTrace = FirstFrameTrace().also {
            it.mark("setup_player_start")
        }
        /**
         * - minBufferMs
         * - maxBufferMs
         * - bufferForPlaybackMs
         * - bufferForPlaybackAfterRebufferMs
         */
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1500,
                5000,
                500,
                1000
            )
            .build()

        player = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .build().also { exoPlayer ->
                firstFrameTrace.mark("player_built")
                playerView.player = exoPlayer
                exoPlayer.addListener(object : Player.Listener {
                    // 记录状态切换，便于区分耗时主要卡在建链、缓冲还是就绪后的渲染阶段。
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        firstFrameTrace.mark("state_${playerStateName(playbackState)}")
                        if (playbackState == Player.STATE_READY) {
                            startRoomDataIfNeeded()
                        }
                    }

                    // 真实首帧渲染完成时打点，这是评估“看到画面”时延的关键节点。
                    override fun onRenderedFirstFrame() {
                        firstFrameTrace.mark("rendered_first_frame")
                        hideLoadingIfNeeded(videoLoading)
                        startRoomDataIfNeeded()
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        // 失败链路也保留时序，方便定位异常发生在首帧前的哪个阶段。
                        firstFrameTrace.mark("player_error_${error.errorCodeName}")
                        if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                            recoverFromBehindLiveWindow(exoPlayer)
                        }
                    }
                })

                val mediaItem = MediaItem.fromUri(
                    "https://akamaibroadcasteruseast.akamaized.net/cmaf/live/657078/akasource/out.mpd"
                )
                firstFrameTrace.mark("media_item_created")
                exoPlayer.setMediaItem(mediaItem)
                firstFrameTrace.mark("media_item_set")
                exoPlayer.prepare()
                firstFrameTrace.mark("prepare_called")
                exoPlayer.play()
                firstFrameTrace.mark("play_called")
            }
    }

    private fun hideLoadingIfNeeded(videoLoading: View) {
        if (hasHiddenLoading) return
        hasHiddenLoading = true
        videoLoading.animate()
            .alpha(0f)
            .setDuration(180)
            .withEndAction {
                videoLoading.visibility = View.GONE
                videoLoading.alpha = 1f
            }
            .start()
    }

    private fun startRoomDataIfNeeded() {
        if (hasStartedRoomData) return
        hasStartedRoomData = true
        viewModel.loadInitialData()
        viewModel.connectWebSocket()
    }

    private fun scrollCommentsToBottom(rvComments: RecyclerView, itemCount: Int) {
        if (itemCount <= 0) return
        rvComments.post {
            rvComments.scrollToPosition(itemCount - 1)
        }
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onPause() {
        player?.pause()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        viewModel.disconnectWebSocket()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }

    private fun playerStateName(playbackState: Int): String {
        return when (playbackState) {
            Player.STATE_IDLE -> "idle"
            Player.STATE_BUFFERING -> "buffering"
            Player.STATE_READY -> "ready"
            Player.STATE_ENDED -> "ended"
            else -> "unknown"
        }
    }

    private fun recoverFromBehindLiveWindow(exoPlayer: ExoPlayer) {
        firstFrameTrace.mark("recover_behind_live_window_started")
        exoPlayer.seekToDefaultPosition()
        exoPlayer.prepare()
        exoPlayer.play()
        firstFrameTrace.mark("recover_behind_live_window_finished")
    }

    private inner class FirstFrameTrace {
        private val startAtMs = SystemClock.elapsedRealtime()
        private val marks = linkedMapOf<String, Long>()

        fun mark(name: String) {
            if (marks.containsKey(name)) return
            val nowMs = SystemClock.elapsedRealtime()
            val sinceStartMs = nowMs - startAtMs
            val previousAtMs = marks.values.lastOrNull() ?: startAtMs
            marks[name] = nowMs
            val deltaMs = nowMs - previousAtMs
            // since_start 表示从 setupPlayer 开始累计的总耗时，delta 表示距离上一个埋点的阶段耗时。
            Log.d(
                TAG,
                "event=$name since_start=${sinceStartMs}ms delta=${deltaMs}ms thread=${Thread.currentThread().name}"
            )
        }
    }
}
