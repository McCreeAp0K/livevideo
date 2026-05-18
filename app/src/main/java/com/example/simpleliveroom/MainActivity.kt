package com.example.simpleliveroom
//Bundle 是 Android 用来保存页面状态的一种数据容器。
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
//Toast 是 Android 里一种短暂弹出的提示消息。
import android.widget.Toast
//让界面支持“沉浸到系统栏区域”。
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
class MainActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        //v 是啥
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val playerView = findViewById<PlayerView>(R.id.playerView)
        val rvComments = findViewById<RecyclerView>(R.id.rvComments)
        val etComment = findViewById<EditText>(R.id.etComment)
        val btnSend = findViewById<Button>(R.id.btnSend)

        val commentList = mutableListOf(
            CommentMessage("小王", "主播好强"),
            CommentMessage("小李", "来了来了"),
            CommentMessage("小张", "这个直播不错"),
            CommentMessage("小赵", "公屏效果已经出来了")
        )
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        val mediaItem = MediaItem.fromUri(
            "https://akamaibroadcasteruseast.akamaized.net/cmaf/live/657078/akasource/out.mpd"
        )

        player?.setMediaItem(mediaItem)
        //- 解析媒体信息
        //- 建立网络请求
        //- 准备缓冲
        //- 为播放做准备
        player?.prepare()
        player?.play()

        val commentAdapter = CommentAdapter(commentList)

        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = commentAdapter

        btnSend.setOnClickListener {
            val inputText = etComment.text.toString().trim()

            if (inputText.isEmpty()) {
                //Toast.LENGTH_SHORT ：显示时间短一点
                Toast.makeText(this, "评论内容不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newComment = CommentMessage(
                userName = "我",
                content = inputText
            )

            commentAdapter.addComment(newComment)
            rvComments.scrollToPosition(commentAdapter.itemCount - 1)
            etComment.text.clear()
        }
    }
    //在页面停止时，释放 ExoPlayer 资源
    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}