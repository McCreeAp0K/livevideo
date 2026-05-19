package com.example.simpleliveroom.ui.liveroom

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simpleliveroom.R
import com.example.simpleliveroom.model.CommentMessage
import com.facebook.drawee.view.SimpleDraweeView

class CommentAdapter(
    private val commentList: MutableList<CommentMessage>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivUserAvatar: SimpleDraweeView = itemView.findViewById(R.id.ivUserAvatar)
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
        holder.ivUserAvatar.setImageURI(Uri.parse(comment.avatar))
        holder.tvUserName.text = "${comment.name}："
        holder.tvContent.text = comment.comment
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    fun addComment(commentMessage: CommentMessage) {
        commentList.add(commentMessage)
        notifyItemInserted(commentList.size - 1)
    }

    fun addComments(newComments: List<CommentMessage>) {
        if (newComments.isEmpty()) return
        val startPosition = commentList.size
        commentList.addAll(newComments)
        notifyItemRangeInserted(startPosition, newComments.size)
    }

    fun replaceComments(newComments: List<CommentMessage>) {
        commentList.clear()
        commentList.addAll(newComments)
        notifyDataSetChanged()
    }
}