package com.example.simpleliveroom.data.api

import com.example.simpleliveroom.model.AnchorInfo
import com.example.simpleliveroom.model.CommentMessage
import com.example.simpleliveroom.model.PostMessageResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("api/anchor")
    fun getAnchorInfo(
        @Query("room_id") roomId: String
    ): Call<AnchorInfo>

    @GET("api/messages")
    fun getMessages(
        @Query("room_id") roomId: String
    ): Call<List<CommentMessage>>

    @FormUrlEncoded
    @POST("api/messages")
    fun postMessage(
        @Field("room_id") roomId: String,
        @Field("comment") comment: String
    ): Call<PostMessageResponse>
}