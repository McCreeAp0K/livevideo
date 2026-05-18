package com.example.simpleliveroom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * - 告诉 RecyclerView ：每一项评论长什么样
 * - 告诉 RecyclerView ：第几条评论应该显示什么内容
 */
class CommentAdapter(
    private val commentList: MutableList<CommentMessage>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.tvUserName.text = "${comment.userName}："
        holder.tvContent.text = comment.content
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    fun addComment(commentMessage: CommentMessage) {
        commentList.add(commentMessage)
        //notifyDataSetChanged() 会让整个列表都刷新
        notifyItemInserted(commentList.size - 1)
    }
}