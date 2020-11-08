# Netty学习

### Chapter1-异步和事件驱动

#### 1.什么是Netty?

Java网络编程提供的原生API复杂难用，而Netty将这些复杂难用的API接口优化封装，提供给我们简单易用的API接口。一句话，用较简单的抽象隐藏底层实现的复杂性。

Java原生API示例

![image-20201028233349787](img/image-20201028233349787.png)

是阻塞的，一个连接创建一个线程，效率低，最主要的是线程多了，上下文切换的开销很大

![image-20201028233502910](img/image-20201028233502910.png)



Netty使用了Java NIO，避免了以上问题

![image-20201028233638169](img/image-20201028233638169.png)

使用较少的线程便可以处理许多连接，因此也减少了内存管理和上下文切换所带来开销；

当没有 I/O 操作需要处理的时候，线程也可以被用于其他任务。

#### 2.Netty优点

- 统一的 API，支持多种传输类型，阻塞的和非阻塞的
- 易于使用
- 拥有比 Java 的核心 API 更高的吞吐量以及更低的延迟
- 完整的 SSL/TLS 以及 StartTLS 支持
- ...

#### 3.异步和事件驱动

异步：for example，你在煮饭的同时，可以做菜，并发进行

事件驱动：for example，点击登录，进入网页，是由点击事件触发

#### 4.Netty核心组件

- Channel：可以看作是传入（入站）或者传出（出站）数据的载体
- 回调：其实就是一个方法，一个指向已经被提供给另外一个方法的方法的引用。这使得后
  者可以在适当的时候调用前者。
- Future：可以看作是一个异步操作的结果的占位符；它将在未来的某个时刻完成，并提供对其结果的访问。
- 事件：Netty使用不同的事件来通知我们状态的改变或者是操作的状态，让我们在已经的事件触发适当的动作。比如数据读取的时候，打印一个“hello world”
- ChannelHandler：处理器，针对特定的事件执行特定的动作



![image-20201028232710848](img/image-20201028232710848.png)

事件被分发给ChannelHandler类中的方法，Netty处理链可以对事件进行过滤筛选，执行相应的动作。





### Chapter2-你的第一款Netty应用程序

#### 1.Netty客户端和服务端示意图

实现功能：客户端发啥消息，服务端返回同样的消息，体现**请求-响应交互模式**

![image-20201104003246013](img/image-20201104003246013.png)

#### 2.编写Echo服务器

- ChannelHandler: 处理客户端发送数的据，及业务逻辑

- 引导：配置服务器的启动代码

  很好体现了**解耦**思想，将业务逻辑与网络处理代码分离，分成两部分

##### 2.1 ChannelHandler和业务逻辑

- channelRead() : 对于每个传入的消息都要调用
- channelReadComplete() : 通知ChannelInboundHandler最后一次对channelRead()的调用是当前批量读取中的最后一条消息
- exceptionCaught() :  在读取操作期间，有异常抛出时会调用

**EchoServerHandler**

![image-20201104004439969](img/image-20201104004439969.png)

ChannelInboundHandlerAdapter 有一个直观的 API，并且**它的每个方法都可以被重写以挂钩到事件生命周期的恰当点上**

##### 2.2 引导服务器

- 绑定监听端口，并接受传入的连接请求
- 配置 Channel ，以将有关的入站消息通知给 EchoServerHandler 实例

**EchoServer**

![image-20201104004707361](img/image-20201104004707361.png)

- 创建一个```ServerBootstrap```的实例以引导和绑定服务器

- 创建并分配一个```NioEventLoopGroup```实例以进行事件的处理，如接受新连接或读写数据
- 指定服务器绑定的本地的 ```InetSocketAddress```
- 使用一个 ```EchoServerHandler ```的实例初始化每一个新的 Channel
- 调用 ```ServerBootstrap.bind()```方法以绑定服务器

#### 3. 编写Echo客户端

##### 3.1 ChannelHandler和业务逻辑

- channelActive(): 在与服务器的连接建立之后被调用
- channelRead0(): 当从服务器接收到一条消息时被调用
- exceptionCaught(): 在处理过程中引发异常时被调用

**EchoClientHandler**

![image-20201104004734867](img/image-20201104004734867.png)

##### 3.2 引导客户端

**EchoClient**

![image-20201104004827401](img/image-20201104004827401.png)

- 为初始化客户端，创建了一个``` Bootstrap ```实例
- 为进行事件处理分配了一个``` NioEventLoopGroup ```实例，其中事件处理包括**创建新的连接以及处理入站和出站数据**
- 为服务器连接创建了一个 ```InetSocketAddress ```实例
- 当连接被建立时，一个 ```EchoClientHandler``` 实例会被安装到（该 Channel 的）```ChannelPipeline``` 中
- 在一切都设置完成后，调用 ```Bootstrap.connect()```方法连接到远程节点

### Chapter3-Netty的组件和设计

#### 1. Channel接口

基本的 I/O 操作（bind()、connect()、read()和 write()）依赖于底层网络传输所提供的原语。

eg:

- EmbeddedChannel
- LocalServerChannel
- NioDatagramChannel
- NioSctcpChannel
- NioSocketChannel

#### 2. EventLoop

EventLoop定义了Netty的核心抽象，用于处理连接的生命周期所发生的事件。

![image-20201108235943161](img/image-20201108235943161.png)

- 一个`EventLoopGroup`包括一个或多个`EventLoop`
- 一个`EventLoop`在它的生命周期内只和一个`Thread`绑定
- 所有由`EventLoop`处理的I/O事件都将在它专有的Thread上处理
- 一个`Channel`在它的生命周期内只注册于一个`EventLoop`
- 一个EventLoop可能会被分配给一个或多个`Channel`

#### 3. ChannelFuture接口

Netty 中所有的 I/O 操作都是异步的。因为一个操作可能不会立即返回，所以我们需要一种用于在之后的某个时间点确定其结果的方法。为此，Netty 提供了ChannelFuture 接口，其 addListener()方法注册了一个 ChannelFutureListener，以便在某个操作完成时（无论是否成功）得到通知。

#### 4. ChannelHandler接口

ChannelHandler充当了所有处理入站和出站数据的应用程序逻辑的容器。

例如将数据从一种格式转换为另外一种格式，或者处理转换过程中所抛出的异常。

#### 5. ChannelPipeline接口

ChannelPipeline 提供了 ChannelHandler 链的容器，并定义了用于在该链上传播入站和出站事件流的 API。当 Channel 被创建时，它会被自动地分配到它专属的 ChannelPipeline。

ChannelHandler安装到ChannelPipeline中的过程如下：

- 一个`ChannelInitializer`的实现被注册到了`ServerBootstrap`中
- 当 `ChannelInitializer.initChannel()`方法被调用时，ChannelInitializer将在 ChannelPipeline 中安装一组自定义的 ChannelHandler
- `ChannelInitializer` 将它自己从 `ChannelPipeline` 中移除

Netty应用程序入站和出站数据流

![image-20201109002159692](img/image-20201109002159692.png)

**入站**：一个入站消息被读取，那么它会从 ChannelPipeline 的头部开始流动，并被传递给第一个 ChannelInboundHandler。这个 ChannelHandler 不一定会实际地修改数据，具体取决于它的具体功能，在这之后，数据将会被传递给链中的下一个ChannelInboundHandler。最终，数据将会到达 ChannelPipeline 的尾端，届时，所有处理就都结束了。

**出站**：数据将从ChannelOutboundHandler 链的尾端开始流动，直到它到达链的头部为止。在这之后，出站数据将会到达网络传输层，这里显示为 Socket。通常情况下，这将触发一个写操作。

在Netty中，有两种发送消息的方式。

1. 你可以直接写到`Channel`中，将会导致消息从Channel-Pipeline 的尾端开始流动
2. 也可以 写到和Channel-Handler相关联的`ChannelHandlerContext`对象中，导致消息从 ChannelPipeline 中的下一个 Channel-Handler 开始流动

常用的适配器类

- `ChannelHandlerAdapter`: 处理入站和出站消息
- `ChannelInboundHandlerAdapter`: 处理入站消息
- `ChannelOutboundHandlerAdapter`: 处理出站消息
- `ChannelDuplexHandler`: 收发消息

![image-20201109005048076](img/image-20201109005048076.png)

解码器：将netty接收的字节数组转成另一种格式，通常是一个Java对象

编码器：跟解码器相反，将一个对象转成字节数组

#### 6. 引导

Netty 的引导类为应用程序的网络层配置提供了容器，这涉及将一个进程绑定到某个指定的端口（服务器引导）

或者将一个进程连接到另一个运行在某个指定主机的指定端口上的进程（客户端引导）。

![image-20201109005829286](img/image-20201109005829286.png)

因为服务器需要两组不同的 Channel。第一组将只包含一个 ServerChannel，代表服务器自身的已绑定到某个本地端口的正在监听的套接字。而第二组将包含所有已创建的用来处理传入客户端连接（对于每个服务器已经接受的连接都有一个）的 Channel。

![image-20201109010157044](img/image-20201109010157044.png)

与 ServerChannel 相关联的 EventLoopGroup 将分配一个负责为传入连接请求创建Channel 的 EventLoop。一旦连接被接受，第二个 EventLoopGroup 就会给它的 Channel分配一个 EventLoop。