package com.example.simpleliveroom.ui.liveroom

import android.net.Uri
import android.os.Bundle
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
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simpleliveroom.R
import com.example.simpleliveroom.data.repository.LiveRoomRepository
import com.facebook.drawee.view.SimpleDraweeView

class MainActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private var hasStartedRoomData = false
    private var hasHiddenLoading = false
    private var renderedCommentCount = 0

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

    private fun setupPlayer(playerView: PlayerView, videoLoading: View) {
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
                playerView.player = exoPlayer
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            hideLoadingIfNeeded(videoLoading)
                            startRoomDataIfNeeded()
                        }
                    }

                    override fun onRenderedFirstFrame() {
                        hideLoadingIfNeeded(videoLoading)
                        startRoomDataIfNeeded()
                    }
                })

                val mediaItem = MediaItem.fromUri(
                    "https://akamaibroadcasteruseast.akamaized.net/cmaf/live/657078/akasource/out.mpd"
                )
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
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
}
