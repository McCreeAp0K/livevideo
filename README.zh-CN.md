# SimpleLiveRoom

SimpleLiveRoom 是一个精简的 Android 直播间示例项目，基于 Kotlin、Media3 ExoPlayer、Retrofit、OkHttp WebSocket 和 Fresco 构建。它演示了如何搭建一个基础直播间，包括视频播放、房间信息展示、实时评论、在线人数更新，以及一些轻量级性能优化实践。

英文版说明见 [README.md](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/README.md)。

## 项目简介

这个项目聚焦于“单个直播间”体验，当前包含以下能力：

- 基于 `Media3 ExoPlayer` 的 DASH 直播流播放
- 通过 REST API 拉取主播信息和历史评论
- 通过 WebSocket 接收在线人数和实时评论
- 通过 HTTP 接口发送评论
- 使用 Fresco 加载头像
- 首帧 loading 覆盖层，优化用户感知启动速度
- 基础的播放器生命周期管理
- 在线人数节流和评论批量更新，减少 UI 高频刷新

这个项目代码量不大、结构比较直观，适合：

- 学习 Android 直播间基础实现
- 作为直播间页面原型
- 作为“播放器 + 实时消息”整合示例

## 主要功能

### 播放能力

- 使用 `androidx.media3` 播放 DASH 直播流
- 使用自定义 `LoadControl` 提升起播速度
- 首帧到来前显示全屏 loading 层
- 优先启动播放器，在播放器就绪后再加载房间数据
- 在 `onPause()` 暂停播放，在 `onResume()` 恢复播放

### 房间 UI

- 展示主播昵称、头像、关注数
- 展示在线人数
- 展示精简评论区
- 评论区始终自动滚动到最后一条

### 实时数据

- 通过 REST 拉取房间初始化信息和历史评论
- 通过 WebSocket 接收：
  - 在线人数更新
  - 实时评论批量消息
- 通过 HTTP 接口发送评论

### 已包含的性能优化

- 首帧 loading 层，优化首屏感知
- 将房间数据初始化延后到播放器可播之后
- WebSocket 回调切回主线程再更新 UI
- 在线人数节流，减少高频文本刷新
- `ViewModel` 中对评论做批量聚合刷新
- Activity 中对评论列表做增量渲染，减少整表刷新

## 页面与核心流程

主页面实现位于 [MainActivity.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/ui/liveroom/MainActivity.kt) 和 [activity_main.xml](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/res/layout/activity_main.xml)。

典型启动流程如下：

1. 创建播放器并准备直播流。
2. 在首帧到来前展示 loading 覆盖层。
3. 当播放器进入 `STATE_READY` 或渲染首帧后，隐藏 loading。
4. 拉取主播信息和历史评论。
5. 建立 WebSocket 连接，开始接收在线人数和实时评论。
6. 新评论到达后，评论区自动滚动到底部。

## 项目结构

```text
SimpleLiveRoom/
├── app/
│   ├── src/main/java/com/example/simpleliveroom/
│   │   ├── data/
│   │   │   ├── api/
│   │   │   ├── repository/
│   │   │   └── ws/
│   │   ├── model/
│   │   ├── ui/liveroom/
│   │   └── LiveApplication.kt
│   └── src/main/res/
│       ├── layout/
│       └── values/
├── gradle/
├── build.gradle.kts
└── README.md
```

### 关键文件

- [MainActivity.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/ui/liveroom/MainActivity.kt)
  - 承载直播间页面
  - 创建和管理播放器
  - 观察 UI 状态
  - 处理评论列表渲染

- [LiveRoomViewModel.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/ui/liveroom/LiveRoomViewModel.kt)
  - 管理房间 UI 状态
  - 加载初始化数据
  - 发送评论
  - 对在线人数做节流
  - 对评论做批量聚合

- [LiveRoomRepository.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/data/repository/LiveRoomRepository.kt)
  - 负责打通 REST 与 WebSocket 数据源

- [WebSocketManager.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/data/ws/WebSocketManager.kt)
  - 建立 WebSocket 连接
  - 解析实时消息
  - 在主线程分发 UI 更新

- [ApiService.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/data/api/ApiService.kt)
  - 定义 REST 接口

- [RetrofitProvider.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/data/api/RetrofitProvider.kt)
  - 配置基础域名和 Gson 解析器

- [activity_main.xml](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/res/layout/activity_main.xml)
  - 定义直播间布局
  - 包含播放器区域、loading 覆盖层、主播信息区、在线人数、评论区和输入栏

## 架构说明

项目采用轻量级 MVVM 风格结构：

- `UI 层`
  - `MainActivity`
  - `CommentAdapter`

- `状态层`
  - `LiveRoomUiState`
  - `LiveRoomViewModel`

- `数据层`
  - `LiveRoomRepository`
  - `ApiService`
  - `RetrofitProvider`
  - `WebSocketManager`

- `模型层`
  - REST 响应模型
  - WebSocket 消息模型

### 数据流

REST 数据流：

1. `MainActivity` 通过 `ViewModel` 触发房间初始化。
2. `LiveRoomViewModel` 调用 `LiveRoomRepository` 拉取主播信息和历史评论。
3. `LiveRoomRepository` 使用 Retrofit 发起请求。
4. 请求结果被组装进 `LiveRoomUiState`。
5. `MainActivity` 观察状态并刷新 UI。

WebSocket 数据流：

1. `LiveRoomViewModel` 请求 Repository 建立连接。
2. `WebSocketManager` 接收实时消息。
3. 在线人数和评论消息切回主线程分发。
4. `LiveRoomViewModel` 对高频更新进行节流和批处理。
5. `MainActivity` 渲染最新状态。

## 技术栈

### 语言与构建

- Kotlin
- Gradle Kotlin DSL
- Android SDK 36
- Java 11 兼容

### Android 依赖

- AndroidX AppCompat
- AndroidX ConstraintLayout
- AndroidX RecyclerView
- AndroidX Media3 ExoPlayer

### 网络与序列化

- Retrofit `2.11.0`
- OkHttp `4.12.0`
- Gson `2.10.1`

### 图片加载

- Fresco `3.4.0`

## 运行要求

- 安装 Android Studio 并支持 Gradle
- 一台 Android 真机或模拟器
- 最低支持版本：`24`
- 目标版本：`36`
- 可访问的后端服务，至少需要提供：
  - 房间信息接口
  - 历史评论接口
  - 发送评论接口
  - WebSocket 在线人数与评论推送
- 可访问的 DASH 直播流地址

## 后端配置说明

当前项目使用固定的局域网测试地址：

- REST 基础地址：`http://10.37.242.55:3000/`
- WebSocket 地址：`ws://10.37.242.55:3000/ws/room-viewers?room_id=1001`

这些值目前是硬编码在以下文件中的：

- [RetrofitProvider.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/data/api/RetrofitProvider.kt)
- [WebSocketManager.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/data/ws/WebSocketManager.kt)
- [LiveRoomViewModel.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/ui/liveroom/LiveRoomViewModel.kt)

另外，项目在 [AndroidManifest.xml](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/AndroidManifest.xml) 中开启了明文流量，这适合本地调试，但上线前应改为 HTTPS 与 WSS。

## 接口预期

### REST

项目当前预期的接口形式如下：

- `GET /api/anchor?room_id=1001`
- `GET /api/messages?room_id=1001`
- `POST /api/messages`

### WebSocket

项目预期 WebSocket 消息具备 `type` 和 `data` 字段。

当前支持的消息类型：

- `room_viewer_count`
- `room_comment_batch`

## 运行方式

1. 使用 Android Studio 打开项目。
2. 确保手机或模拟器可以访问后端服务。
3. 根据需要修改基础域名、房间号或直播流地址。
4. 同步 Gradle。
5. 运行 `app` 模块。

## 当前行为说明

应用启动后会执行：

- 先初始化播放器并准备直播流
- 展示 loading 覆盖层
- 在播放器可播后开始拉取房间数据
- 拉取主播信息和历史评论
- 通过 WebSocket 接收实时评论和在线人数
- 评论区始终保持在最后一条

## 当前限制

这个项目目前更偏向 Demo 或原型，还不是生产可上线的直播产品。

目前明显的限制包括：

- 房间号和直播流地址是硬编码的
- 后端地址是硬编码的
- 没有登录与鉴权
- 没有 WebSocket 自动重连策略
- 没有完整的播放失败页
- 没有横屏或全屏模式
- 没有清晰度切换
- 没有监控埋点
- 没有敏感词过滤和内容审核
- 自动化测试较少

## 后续可扩展方向

如果要继续往产品化方向演进，推荐补充：

- 登录态与 token 鉴权
- HTTPS / WSS 安全链路
- 播放失败重试与弱网提示
- WebSocket 心跳与退避重连
- 评论审核与禁言能力
- 多环境配置
- 首帧、卡顿、崩溃、消息送达等监控指标
- 使用 `DiffUtil` 或 `ListAdapter` 进一步优化评论渲染
- 限制评论缓存上限
- 单测与 UI 自动化测试

## 这个项目适合学习什么

虽然项目不大，但已经能比较直观地展示以下实践：

- 如何把视频播放和实时数据整合到同一页面
- 如何把非核心初始化延后到首帧之后
- 如何用节流和批处理减少无效 UI 更新
- 如何用轻量 MVVM 风格组织一个 Android 业务页面

## License

当前仓库没有定义 License。如果你计划对外分享或分发项目，建议补充开源协议。
