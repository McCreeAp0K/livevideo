package com.example.simpleliveroom.data.ws

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.simpleliveroom.model.BaseWsMessage
import com.example.simpleliveroom.model.CommentBatchData
import com.example.simpleliveroom.model.CommentMessage
import com.example.simpleliveroom.model.ViewerCountData
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * WebSocket管理器
 */
class WebSocketManager(
    private val roomId: String,
    private val onViewerCountUpdate: (Int) -> Unit,
    private val onCommentBatchReceived: (List<CommentMessage>) -> Unit
) {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun connect() {
        val request = Request.Builder()
            .url("ws://10.37.242.55:3000/ws/room-viewers?room_id=$roomId")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocketManager", "WebSocket connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocketManager", "message: $text")

                try {
                    val baseMessage = gson.fromJson(text, BaseWsMessage::class.java)

                    when (baseMessage.type) {
                        "room_viewer_count" -> {
                            val viewerData = gson.fromJson(
                                baseMessage.data,
                                ViewerCountData::class.java
                            )
                            mainHandler.post {
                                onViewerCountUpdate(viewerData.viewer_count)
                            }
                        }

                        "room_comment_batch" -> {
                            val commentBatchData = gson.fromJson(
                                baseMessage.data,
                                CommentBatchData::class.java
                            )
                            mainHandler.post {
                                onCommentBatchReceived(commentBatchData.messages)
                            }
                        }

                        else -> {
                            Log.d("WebSocketManager", "Unknown message type: ${baseMessage.type}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WebSocketManager", "message handling error", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketManager", "WebSocket failed", t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketManager", "WebSocket closed: $reason")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Activity stopped")
        webSocket = null
    }
}
