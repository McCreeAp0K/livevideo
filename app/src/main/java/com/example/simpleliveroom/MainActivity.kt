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

class MainActivity : AppCompatActivity() {

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

        val rvComments = findViewById<RecyclerView>(R.id.rvComments)
        val etComment = findViewById<EditText>(R.id.etComment)
        val btnSend = findViewById<Button>(R.id.btnSend)

        val commentList = mutableListOf(
            CommentMessage("小王", "主播好强"),
            CommentMessage("小李", "来了来了"),
            CommentMessage("小张", "这个直播不错"),
            CommentMessage("小赵", "公屏效果已经出来了")
        )

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
}