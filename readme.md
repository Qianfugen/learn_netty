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

### Chapter4-传输

#### 1. 传输迁移

##### 1.1 未使用netty的OIO网络编程

![image-20201110222152464](img/image-20201110222152464.png)

##### 1.2 未使用netty的NIO网络编程

![image-20201110222410952](img/image-20201110222410952.png)

##### 1.3 使用netty的OIO网络编程

![image-20201110222516945](img/image-20201110222516945.png)

##### 1.4 使用netty的NIO网络编程

![image-20201110222631860](img/image-20201110222631860.png)

显而易见，java原生的OIO和NIO网络编程变化很大，复杂难用。但是netty的OIO和NIO网络编程，为每种传输的实现都暴露了相同的 API，所以无论选用哪一种传输的实现，你的代码都仍然几乎不受影响。

#### 2. 传输API

![image-20201110224333245](img/image-20201110224333245.png)

每个 Channel 都将会被分配一个 `ChannelPipeline` 和 `ChannelConfig`。

ChannelConfig 包含了该 Channel 的所有配置设置，并且支持热更新。

ChannelPipeline 持有所有将应用于入站和出站数据以及事件的 ChannelHandler 实例，这些 ChannelHandler 实现了应用程序用于处理状态变化以及数据处理的逻辑。

**ChannelHandler的典型用途：**

- 将数据从一种格式转成另一种格式
- 提供异常的通知
- 提供Channel变为活动或非活动的通知
- 提供当Channel注册到EventLoop或者从EventLoop注销时的通知
- 提供有关用户自定义事件的通知

**拦截过滤器**：ChannelPipeline 实现了一种常见的设计模式 — 拦截过滤器（Intercepting Filter）。UNIX 管道是另外一个熟悉的例子：多个命令被链接在一起，其中一个命令的输出端将连接到命令行中下一个命令的输入端。

![image-20201110224243277](img/image-20201110224243277.png)

#### 3. 内置的传输

​																**Netty所提供的传输**

| 名称     | 包                          | 描述                                                         |
| -------- | --------------------------- | ------------------------------------------------------------ |
| NIO      | io.netty.channel.socket.nio | 使用 java.nio.channels 包作为基础——基于<br/>选择器的方式     |
| Epoll    | io.netty.channel.epoll      | 由 JNI 驱动的 epoll() 和非阻塞 IO。这个传输支持<br/>只有在Linux上可用的多种特性，如 SO_REUSEPORT ，<br/>比NIO 传输更快，而且是完全非阻塞的 |
| OIO      | io.netty.channel.socket.oio | 使用 java.net 包作为基础——使用阻塞流                         |
| Local    | io.netty.channel.local      | 可以在 VM 内部通过管道进行通信的本地传输                     |
| Embedded | io.netty.channel.embedded   | Embedded 传输，允许使用 ChannelHandler 而又<br/>不需要一个真正的基于网络的传输。这在测试你的<br/>ChannelHandler 实现时非常有用 |

##### 3.1 NIO-非阻塞I/O

NIO 提供了一个所有 I/O 操作的全异步的实现，选择器Selector充当一个注册表。

![image-20201110225221714](img/image-20201110225221714.png)

![image-20201110232926060](img/image-20201110232926060.png)

![image-20201110234209901](img/image-20201110234209901.png)

![image-20201110235156473](img/image-20201110235156473.png)

![image-20201110235809946](img/image-20201110235809946.png)

![image-20201110235856288](img/image-20201110235856288.png)



1. 当客户端连接时，会通过`ServerSocketChannel`得到`SocketChannel`
2. 将SocketChannel注册到`Selector`上，`register(Selector sel, int ops)`，一个Selector可以注册多个SocketChannel
3. 注册后返回一个SelectionKey，会和该Selector关联（集合）
4. Selector进行监听，`select()`，返回有事件发生的通道个数
5. 进一步得到各个`SelectionKey`(有事件发生时)
6. 在通过SelectionKey的`channel()`反向获取SocketChannel
7. 可以通过得到的channel，完成业务处理

##### 3.2 Epoll — 用于 Linux 的本地非阻塞传输

**高负载下性能更佳，优于JDK的NIO实现**

Linux作为高性能网络编程的平台，其重要性与日俱增，这催生了大量先进特性的开发，其中包括**epoll——一个高度可扩展的I/O事件通知特性**。这个API自Linux内核版本 2.5.44（2002）被引入，提供了比旧的POSIX select和poll系统调用更好的性能，同时现在也是Linux上非阻塞网络编程的事实标准。

##### 3.3 OIO — 旧的阻塞 I/O

Netty 的 OIO 传输实现代表了一种折中：它可以通过常规的传输 API 使用，但是由于它是建立在 java.net 包的阻塞实现之上的，所以它不是异步的。

Netty是如何能够使用和用于异步传输相同的API来支持OIO的呢？
答案就是，Netty利用了SO_TIMEOUT这个Socket标志，它指定了等待一个I/O操作完成的最大毫秒数。如果操作在指定的时间间隔内没有完成，则将会抛出一个SocketTimeout Exception。Netty将捕获这个异常并继续处理循环。在EventLoop下一次运行时，它将再次尝试。

![image-20201111002226346](img/image-20201111002226346.png)

#### 4. 传输的用例

​																				**应用程序的最佳传输**

| 应用程序的需求                 | 推荐的传输                  |
| ------------------------------ | --------------------------- |
| 非阻塞代码库或者一个常规的起点 | NIO(或者在Linux上使用Epoll) |
| 阻塞代码库                     | OIO                         |
| 在同一个JVM内部的通信          | Local                       |
| 测试ChannelHandler的实现       | Embedded                    |

### Chapter5-ByteBuf

##### 1. ByteBuf API的优点

- 自定义缓冲区类型扩展
- 零拷贝
- 容量按需增长
- 读写模式不需要调用ByteBuffer的flip()切换
- 读写索引分离
- 支持方法的链式调用
- 支持引用计数
- 支持池化
- ...

##### 2.ByteBuf类-Netty的数据容器

**工作模式**

ByteBuf 维护了两个不同的索引：一个用于读取，一个用于写入。当你从 ByteBuf 读取时，它的 readerIndex 将会被递增已经被读取的字节数。同样地，当你写入 ByteBuf 时，它的writerIndex 也会被递增。

![image-20201201232811821](img/image-20201201232811821.png)

**使用模式**

1. 堆缓冲区

   最常用的 ByteBuf 模式是将数据存储在 JVM 的堆空间中。这种模式被称为支撑数组（backing array），它能在没有使用池化的情况下提供快速的分配和释放

2. 直接缓冲区

   直接缓冲区是另外一种 ByteBuf 模式。我们期望用于对象创建的内存分配永远都来自于堆中，但这并不是必须的——NIO 在 JDK 1.4 中引入的 ByteBuffer 类允许 JVM 实现通过本地调用来分配内存。

3. 复合缓冲区

   第三种也是最后一种模式使用的是复合缓冲区，它为多个 ByteBuf 提供一个聚合视图。在这里你可以根据需要添加或者删除 ByteBuf 实例，这是一个 JDK 的 ByteBuffer 实现完全缺失的特性。
   Netty 通过一个 ByteBuf 子类——CompositeByteBuf ——实现了这个模式，它提供了一个将多个缓冲区表示为单个合并缓冲区的虚拟表示。

   ![image-20201201234151148](img/image-20201201234151148.png)

##### 3.字节级操作

1. 随机访问索引

   ![image-20201201234932610](img/image-20201201234932610.png)

2. 顺序访问索引

   虽然 ByteBuf 同时具有读索引和写索引，但是 JDK 的 ByteBuffer 却只有一个索引，这也就是为什么必须调用 flip()方法来在读模式和写模式之间进行切换的原因。图 5-3 展示了ByteBuf 是如何被它的两个索引划分成 3 个区域的。

   ![image-20201201235053015](img/image-20201201235053015.png)

3. 可丢弃字节

   在图 5-3 中标记为可丢弃字节的分段包含了已经被读过的字节。通过调用 discardReadBytes()方法，可以丢弃它们并回收空间。这个分段的初始大小为 0，存储在 readerIndex 中，会随着 read 操作的执行而增加。

   缓冲区上调用discardReadBytes()方法后的结果。可以看到，可丢弃字节分段中的空间已经变为可写的了

   ![image-20201201235427423](img/image-20201201235427423.png)

4. 可读字节

   ByteBuf 的可读字节分段存储了实际数据。新分配的、包装的或者复制的缓冲区的默认的readerIndex 值为 0。任何名称以 read 或者 skip 开头的操作都将检索或者跳过位于当前readerIndex 的数据，并且将它增加已读字节数。

   ![image-20201201235924039](img/image-20201201235924039.png)

5. 可写字节

   可写字节分段是指一个拥有未定义内容的、写入就绪的内存区域。新分配的缓冲区的writerIndex 的默认值为 0。任何名称以 write 开头的操作都将从当前的 writerIndex 处开始写数据，并将它增加已经写入的字节数。如果写操作的目标也是 ByteBuf，并且没有指定源索引的值，则源缓冲区的 readerIndex 也同样会被增加相同的大小。

   ![image-20201202000624487](img/image-20201202000624487.png)

6. 索引管理

   可以通过调用markReaderIndex()、markWriterIndex()、resetWriterIndex()和 resetReaderIndex()来标记和重置 ByteBuf 的 readerIndex 和 writerIndex。

   可以通过调用 clear()方法来将 readerIndex 和 writerIndex 都设置为 0。注意，这并不会清除内存中的内容。

   ![image-20201202000916383](img/image-20201202000916383.png)

   ![image-20201202000927680](img/image-20201202000927680.png)

7. 查找操作

   在 ByteBuf中有多种可以用来确定指定值的索引的方法。最简单的是使用indexOf()方法。

   ![image-20201202001703544](img/image-20201202001703544.png)

8. 派生缓冲区

   派生缓冲区为 ByteBuf 提供了以专门的方式来呈现其内容的视图。这类视图是通过以下方法被创建的

   - duplicate()
   - slice()
   - slice(int, int)
   - Unpooled.unmodifiableBuffer(...)
   - order(ByteOrder)
   - readSlice(int)

   ![image-20201202002656706](img/image-20201202002656706.png)

   ![image-20201202002943685](img/image-20201202002943685.png)

9. 读/写操作

   正如我们所提到过的，有两种类别的读/写操作：

   - get()和 set()操作，从给定的索引开始，并且保持索引不变；
   - read()和 write()操作，从给定的索引开始，并且会根据已经访问过的字节数对索引进行调整。

   ![image-20201202003513031](img/image-20201202003513031.png)

   ![image-20201202003702455](img/image-20201202003702455.png)

   ![image-20201202003814551](img/image-20201202003814551.png)

   ![image-20201202003839878](img/image-20201202003839878.png)

##### 4.ByteBufHolder 接口

我们经常发现，除了实际的数据负载之外，我们还需要存储各种属性值。HTTP 响应便是一个很好的例子，除了表示为字节的内容，还包括状态码、cookie 等。为了处理这种常见的用例，Netty 提供了 ByteBufHolder。ByteBufHolder 也为 Netty 的高级特性提供了支持，如缓冲区池化，其中可以从池中借用 ByteBuf，并且在需要时自动释放。

如果想要实现一个将其有效负载存储在 ByteBuf 中的消息对象，那么 ByteBufHolder 将是个不错的选择。

![image-20201202004333182](img/image-20201202004333182.png)

##### 5.ByteBuf 分配

1. 按需分配：ByteBufAllocator 接口

   为了降低分配和释放内存的开销，Netty 通过 interface ByteBufAllocator 实现了（ByteBuf 的）池化，它可以用来分配我们所描述过的任意类型的 ByteBuf 实例。

   ![image-20201202004731378](img/image-20201202004731378.png)

2. Unpooled 缓冲区

   Netty 提供了一个简单的称为 Unpooled 的工具类，它提供了静态的辅助方法来创建未池化的 ByteBuf实例。

   ![image-20201202004850235](img/image-20201202004850235.png)

3. ByteBufUtil 类

   ByteBufUtil 提供了用于操作 ByteBuf 的静态的辅助方法。

    `hexdump()`方法，它以十六进制的表示形式打印ByteBuf 的内容。这在各种情况下都很有用，例如，出于调试的目的记录 ByteBuf 的内容。十六进制的表示通常会提供一个比字节值的直接表示形式更加有用的日志条目，此外，十六进制的版本还可以很容易地转换回实际的字节表示。

    `boolean equals(ByteBuf, ByteBuf)`，它被用来判断两个 ByteBuf实例的相等性。

##### 6.引用计数

引用计数是一种通过在某个对象所持有的资源不再被其他对象引用时释放该对象所持有的资源来优化内存使用和性能的技术。

引用计数背后的想法并不是特别的复杂；它主要涉及跟踪到某个特定对象的活动引用的数量。一个ReferenceCounted 实现的实例将通常以活动的引用计数为 1 作为开始。只要引用计数大于 0，就能保证对象不会被释放。当活动引用的数量减少到 0 时，该实例就会被释放。

![image-20201202005412596](img/image-20201202005412596.png)

