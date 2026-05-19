package com.example.simpleliveroom.data.repository

import com.example.simpleliveroom.model.AnchorInfo
import com.example.simpleliveroom.model.CommentMessage
import com.example.simpleliveroom.model.PostMessageResponse
import com.example.simpleliveroom.data.api.RetrofitProvider
import com.example.simpleliveroom.data.ws.WebSocketManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
/**
 * - 拉主播信息
 * - 拉历史评论
 * - 发送评论
 * - 连接 WebSocket
 * - 断开 WebSocket
 */
class LiveRoomRepository {

    private var webSocketManager: WebSocketManager? = null

    fun loadAnchorInfo(
        roomId: String,
        onSuccess: (AnchorInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        RetrofitProvider.apiService.getAnchorInfo(roomId)
            .enqueue(object : Callback<AnchorInfo> {
                override fun onResponse(call: Call<AnchorInfo>, response: Response<AnchorInfo>) {
                    if (response.isSuccessful) {
                        val anchorInfo = response.body()
                        if (anchorInfo != null) {
                            onSuccess(anchorInfo)
                        } else {
                            onError("主播信息为空")
                        }
                    } else {
                        onError("主播信息请求失败")
                    }
                }

                override fun onFailure(call: Call<AnchorInfo>, t: Throwable) {
                    onError("主播信息加载失败")
                }
            })
    }

    fun loadMessages(
        roomId: String,
        onSuccess: (List<CommentMessage>) -> Unit,
        onError: (String) -> Unit
    ) {
        RetrofitProvider.apiService.getMessages(roomId)
            .enqueue(object : Callback<List<CommentMessage>> {
                override fun onResponse(
                    call: Call<List<CommentMessage>>,
                    response: Response<List<CommentMessage>>
                ) {
                    if (response.isSuccessful) {
                        onSuccess(response.body().orEmpty())
                    } else {
                        onError("评论列表请求失败")
                    }
                }

                override fun onFailure(call: Call<List<CommentMessage>>, t: Throwable) {
                    onError("评论列表加载失败")
                }
            })
    }

    fun postMessage(
        roomId: String,
        comment: String,
        onSuccess: (CommentMessage) -> Unit,
        onError: (String) -> Unit
    ) {
        RetrofitProvider.apiService.postMessage(roomId, comment)
            .enqueue(object : Callback<PostMessageResponse> {
                override fun onResponse(
                    call: Call<PostMessageResponse>,
                    response: Response<PostMessageResponse>
                ) {
                    if (response.isSuccessful) {
                        val postResponse = response.body()
                        if (postResponse != null && postResponse.status_code == 0) {
                            onSuccess(postResponse.data)
                        } else {
                            onError("发送评论失败")
                        }
                    } else {
                        onError("发送评论请求失败")
                    }
                }

                override fun onFailure(call: Call<PostMessageResponse>, t: Throwable) {
                    onError("发送评论失败")
                }
            })
    }

    fun connectWebSocket(
        roomId: String,
        onViewerCountUpdate: (Int) -> Unit,
        onCommentBatchReceived: (List<CommentMessage>) -> Unit
    ) {
        disconnectWebSocket()

        webSocketManager = WebSocketManager(
            roomId = roomId,
            onViewerCountUpdate = onViewerCountUpdate,
            onCommentBatchReceived = onCommentBatchReceived
        )

        webSocketManager?.connect()
    }

    fun disconnectWebSocket() {
        webSocketManager?.disconnect()
        webSocketManager = null
    }
}