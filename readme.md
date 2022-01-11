# Netty学习

### Chapter1-异步和事件驱动

#### 1.什么是Netty?

Java网络编程提供的原生API复杂难用，而Netty将这些复杂难用的API接口优化封装，提供给我们简单易用的API接口。一句话，用较简单的抽象隐藏底层实现的复杂性。

Java原生API示例

![image-20201028233349787](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201028233349787.png)

是阻塞的，一个连接创建一个线程，效率低，最主要的是线程多了，上下文切换的开销很大

![image-20201028233502910](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201028233502910.png)



Netty使用了Java NIO，避免了以上问题

![image-20201028233638169](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201028233638169.png)

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



![image-20201028232710848](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201028232710848.png)

事件被分发给ChannelHandler类中的方法，Netty处理链可以对事件进行过滤筛选，执行相应的动作。



### Chapter2-你的第一款Netty应用程序

#### 1.Netty客户端和服务端示意图

实现功能：客户端发啥消息，服务端返回同样的消息，体现**请求-响应交互模式**

![image-20201104003246013](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201104003246013.png)

#### 2.编写Echo服务器

- ChannelHandler: 处理客户端发送数的据，及业务逻辑

- 引导：配置服务器的启动代码

  很好体现了**解耦**思想，将业务逻辑与网络处理代码分离，分成两部分

##### 2.1 ChannelHandler和业务逻辑

- channelRead() : 对于每个传入的消息都要调用
- channelReadComplete() : 通知ChannelInboundHandler最后一次对channelRead()的调用是当前批量读取中的最后一条消息
- exceptionCaught() :  在读取操作期间，有异常抛出时会调用

**EchoServerHandler**

```java
@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        System.out.println("Server reveived: " + in.toString(CharsetUtil.UTF_8));
        ctx.write(in);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```

ChannelInboundHandlerAdapter 有一个直观的 API，并且**它的每个方法都可以被重写以挂钩到事件生命周期的恰当点上**

##### 2.2 引导服务器

- 绑定监听端口，并接受传入的连接请求
- 配置 Channel ，以将有关的入站消息通知给 EchoServerHandler 实例

**EchoServer**

```java
public class EchoServer {
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(1234))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            channel.pipeline().addLast(new EchoServerHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            System.out.println("Echo Server start...");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully().sync();
            workGroup.shutdownGracefully().sync();
        }
    }
}
```

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

```java
@ChannelHandler.Sharable
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf in) throws Exception {
        System.out.println("Client received:" + in.toString(CharsetUtil.UTF_8));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
    }
}
```

##### 3.2 引导客户端

**EchoClient**

```java
public class EchoClient {
    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new EchoClientHandler());
                        }
                    })
                    .remoteAddress(new InetSocketAddress("127.0.0.1", 1234));
            ChannelFuture channelFuture = bootstrap.connect().sync();
            System.out.println("Echo Client Start...");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
```

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

![image-20201108235943161](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201108235943161.png)

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

![image-20201109002159692](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201109002159692.png)

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

![image-20201109005048076](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201109005048076.png)

解码器：将netty接收的字节数组转成另一种格式，通常是一个Java对象

编码器：跟解码器相反，将一个对象转成字节数组

#### 6. 引导

Netty 的引导类为应用程序的网络层配置提供了容器，这涉及将一个进程绑定到某个指定的端口（服务器引导）

或者将一个进程连接到另一个运行在某个指定主机的指定端口上的进程（客户端引导）。

![image-20201109005829286](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201109005829286.png)

因为服务器需要两组不同的 Channel。第一组将只包含一个 ServerChannel，代表服务器自身的已绑定到某个本地端口的正在监听的套接字。而第二组将包含所有已创建的用来处理传入客户端连接（对于每个服务器已经接受的连接都有一个）的 Channel。

![image-20201109010157044](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201109010157044.png)

与 ServerChannel 相关联的 EventLoopGroup 将分配一个负责为传入连接请求创建Channel 的 EventLoop。一旦连接被接受，第二个 EventLoopGroup 就会给它的 Channel分配一个 EventLoop。



### Chapter4-传输

#### 1. 传输迁移

##### 1.1 未使用netty的OIO网络编程

```java
public class PlainOioServer {
    public static void main(String[] args) throws Exception {
        final ServerSocket serverSocket = new ServerSocket(1234);
        try {
            while (true) {
                final Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OutputStream out = null;
                        try {
                            // 读取客户端发来的消息
                            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            String reveiveMsg = in.readLine();
                            System.out.println("客户端发来的消息是：" + reveiveMsg);

                            // 回传消息给客户端
                            out = clientSocket.getOutputStream();
                            out.write("hello".getBytes(CharsetUtil.UTF_8));
                            out.flush();
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                clientSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

##### 1.2 未使用netty的NIO网络编程

```java
public class PlainNioServer {
    public static void main(String[] args) throws Exception {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        ServerSocket serverSocket = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(1234);
        serverSocket.bind(address);
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer msg = ByteBuffer.wrap("hello".getBytes(CharsetUtil.UTF_8));
        while (true) {
            try {
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, msg.duplicate());
                        System.out.println("Accepted connection from " + client);
                    }
                    if (key.isWritable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        while (buffer.hasRemaining()) {
                            if (client.write(buffer) == 0) {
                                break;
                            }
                        }
                    }

                } catch (IOException e) {
                    key.cancel();
                    key.channel().close();
                }
            }
        }
    }
}
```

##### 1.3 使用netty的OIO网络编程

```java
public class NettyOioServer {
    public static void main(String[] args) throws Exception {
        final ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("hello", CharsetUtil.UTF_8));
        EventLoopGroup group = new OioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(OioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(1234))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    ctx.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE);
                                }
                            });
                        }
                    });
            ChannelFuture cf = b.bind().sync();
            System.out.println("Server Start...");
            cf.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
```

##### 1.4 使用netty的NIO网络编程

```java
public class NettyNioServer {
    public static void main(String[] args) throws Exception {
        final ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("hello", CharsetUtil.UTF_8));
        EventLoopGroup group = new NioEventLoopGroup(); // 改动1
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class) // 改动2
                    .localAddress(new InetSocketAddress(1234))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    ctx.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE);
                                }
                            });
                        }
                    });
            ChannelFuture cf = b.bind().sync();
            System.out.println("Server Start...");
            cf.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
```

显而易见，java原生的OIO和NIO网络编程变化很大，复杂难用。但是netty的OIO和NIO网络编程，为每种传输的实现都暴露了相同的 API，所以无论选用哪一种传输的实现，你的代码都仍然几乎不受影响。

#### 2. 传输API

![image-20201110224333245](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201110224333245.png)

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

![image-20201110224243277](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201110224243277.png)

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

![image-20201110225221714](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201110225221714.png)

![image-20201110232926060](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201110232926060.png)

![image-20201110234209901](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201110234209901.png)

![image-20201110235156473](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201110235156473.png)

![image-20201110235809946](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201110235809946.png)

![image-20201110235856288](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201110235856288.png)



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

![image-20201111002226346](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201111002226346.png)

#### 4. 传输的用例

​																				**应用程序的最佳传输**

| 应用程序的需求                 | 推荐的传输                  |
| ------------------------------ | --------------------------- |
| 非阻塞代码库或者一个常规的起点 | NIO(或者在Linux上使用Epoll) |
| 阻塞代码库                     | OIO                         |
| 在同一个JVM内部的通信          | Local                       |
| 测试ChannelHandler的实现       | Embedded                    |

### Chapter5-ByteBuf

#### 1. ByteBuf API的优点

- 自定义缓冲区类型扩展
- 零拷贝
- 容量按需增长
- 读写模式不需要调用ByteBuffer的flip()切换
- 读写索引分离
- 支持方法的链式调用
- 支持引用计数
- 支持池化
- ...

#### 2.ByteBuf类-Netty的数据容器

**工作模式**

ByteBuf 维护了两个不同的索引：一个用于读取，一个用于写入。当你从 ByteBuf 读取时，它的 readerIndex 将会被递增已经被读取的字节数。同样地，当你写入 ByteBuf 时，它的writerIndex 也会被递增。

![image-20201201232811821](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201201232811821.png)

**使用模式**

1. 堆缓冲区

   最常用的 ByteBuf 模式是将数据存储在 JVM 的堆空间中。这种模式被称为支撑数组（backing array），它能在没有使用池化的情况下提供快速的分配和释放

2. 直接缓冲区

   直接缓冲区是另外一种 ByteBuf 模式。我们期望用于对象创建的内存分配永远都来自于堆中，但这并不是必须的——NIO 在 JDK 1.4 中引入的 ByteBuffer 类允许 JVM 实现通过本地调用来分配内存。

3. 复合缓冲区

   第三种也是最后一种模式使用的是复合缓冲区，它为多个 ByteBuf 提供一个聚合视图。在这里你可以根据需要添加或者删除 ByteBuf 实例，这是一个 JDK 的 ByteBuffer 实现完全缺失的特性。
   Netty 通过一个 ByteBuf 子类——CompositeByteBuf ——实现了这个模式，它提供了一个将多个缓冲区表示为单个合并缓冲区的虚拟表示。

   ![image-20201201234151148](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201201234151148.png)

#### 3.字节级操作

1. 随机访问索引

   ![image-20201201234932610](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201201234932610.png)

2. 顺序访问索引

   虽然 ByteBuf 同时具有读索引和写索引，但是 JDK 的 ByteBuffer 却只有一个索引，这也就是为什么必须调用 flip()方法来在读模式和写模式之间进行切换的原因。图 5-3 展示了ByteBuf 是如何被它的两个索引划分成 3 个区域的。

   ![image-20201201235053015](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201201235053015.png)

3. 可丢弃字节

   在图 5-3 中标记为可丢弃字节的分段包含了已经被读过的字节。通过调用 discardReadBytes()方法，可以丢弃它们并回收空间。这个分段的初始大小为 0，存储在 readerIndex 中，会随着 read 操作的执行而增加。

   缓冲区上调用discardReadBytes()方法后的结果。可以看到，可丢弃字节分段中的空间已经变为可写的了

   ![image-20201201235427423](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201201235427423.png)

4. 可读字节

   ByteBuf 的可读字节分段存储了实际数据。新分配的、包装的或者复制的缓冲区的默认的readerIndex 值为 0。任何名称以 read 或者 skip 开头的操作都将检索或者跳过位于当前readerIndex 的数据，并且将它增加已读字节数。

   ![image-20201201235924039](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201201235924039.png)

5. 可写字节

   可写字节分段是指一个拥有未定义内容的、写入就绪的内存区域。新分配的缓冲区的writerIndex 的默认值为 0。任何名称以 write 开头的操作都将从当前的 writerIndex 处开始写数据，并将它增加已经写入的字节数。如果写操作的目标也是 ByteBuf，并且没有指定源索引的值，则源缓冲区的 readerIndex 也同样会被增加相同的大小。

   ![image-20201202000624487](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201202000624487.png)

6. 索引管理

   可以通过调用markReaderIndex()、markWriterIndex()、resetWriterIndex()和 resetReaderIndex()来标记和重置 ByteBuf 的 readerIndex 和 writerIndex。

   可以通过调用 clear()方法来将 readerIndex 和 writerIndex 都设置为 0。注意，这并不会清除内存中的内容。

   ![image-20201202000916383](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201202000916383.png)

   ![image-20201202000927680](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201202000927680.png)

7. 查找操作

   在 ByteBuf中有多种可以用来确定指定值的索引的方法。最简单的是使用indexOf()方法。

   ![image-20201202001703544](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201202001703544.png)

8. 派生缓冲区

   派生缓冲区为 ByteBuf 提供了以专门的方式来呈现其内容的视图。这类视图是通过以下方法被创建的

   - duplicate()
   - slice()
   - slice(int, int)
   - Unpooled.unmodifiableBuffer(...)
   - order(ByteOrder)
   - readSlice(int)

   ![image-20201202002656706](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201202002656706.png)

   ![image-20201202002943685](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201202002943685.png)

9. 读/写操作

   正如我们所提到过的，有两种类别的读/写操作：

   - get()和 set()操作，从给定的索引开始，并且保持索引不变；
   - read()和 write()操作，从给定的索引开始，并且会根据已经访问过的字节数对索引进行调整。

   ![image-20201202003513031](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201202003513031.png)

   ![image-20201202003702455](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201202003702455.png)

   ![image-20201202003814551](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201202003814551.png)

   ![image-20201202003839878](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201202003839878.png)

#### 4.ByteBufHolder 接口

我们经常发现，除了实际的数据负载之外，我们还需要存储各种属性值。HTTP 响应便是一个很好的例子，除了表示为字节的内容，还包括状态码、cookie 等。为了处理这种常见的用例，Netty 提供了 ByteBufHolder。ByteBufHolder 也为 Netty 的高级特性提供了支持，如缓冲区池化，其中可以从池中借用 ByteBuf，并且在需要时自动释放。

如果想要实现一个将其有效负载存储在 ByteBuf 中的消息对象，那么 ByteBufHolder 将是个不错的选择。

![image-20201202004333182](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201202004333182.png)

#### 5.ByteBuf 分配

1. 按需分配：ByteBufAllocator 接口

   为了降低分配和释放内存的开销，Netty 通过 interface ByteBufAllocator 实现了（ByteBuf 的）池化，它可以用来分配我们所描述过的任意类型的 ByteBuf 实例。

   ![image-20201202004731378](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201202004731378.png)

2. Unpooled 缓冲区

   Netty 提供了一个简单的称为 Unpooled 的工具类，它提供了静态的辅助方法来创建未池化的 ByteBuf实例。

   ![image-20201202004850235](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201202004850235.png)

3. ByteBufUtil 类

   ByteBufUtil 提供了用于操作 ByteBuf 的静态的辅助方法。

    `hexdump()`方法，它以十六进制的表示形式打印ByteBuf 的内容。这在各种情况下都很有用，例如，出于调试的目的记录 ByteBuf 的内容。十六进制的表示通常会提供一个比字节值的直接表示形式更加有用的日志条目，此外，十六进制的版本还可以很容易地转换回实际的字节表示。

    `boolean equals(ByteBuf, ByteBuf)`，它被用来判断两个 ByteBuf实例的相等性。

#### 6.引用计数

引用计数是一种通过在某个对象所持有的资源不再被其他对象引用时释放该对象所持有的资源来优化内存使用和性能的技术。

引用计数背后的想法并不是特别的复杂；它主要涉及跟踪到某个特定对象的活动引用的数量。一个ReferenceCounted 实现的实例将通常以活动的引用计数为 1 作为开始。只要引用计数大于 0，就能保证对象不会被释放。当活动引用的数量减少到 0 时，该实例就会被释放。

![image-20201202005412596](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201202005412596.png)

### Chapter6-ChannelHandler&ChannelPipeline

#### 1. ChannelHandler接口

##### 1.1. Channel的生命周期

| 状态                | 描述                                      |
| ------------------- | ----------------------------------------- |
| ChannelUnregistered | Channel已被创建，但未被注册到EventLoop    |
| ChannelRegistered   | Channel已被注册到EventLoop                |
| ChannelActive       | Channel处于活动状态，可以接收和发送数据了 |
| ChannelInactive     | Channel没有连接到远程节点                 |

![image-20201203234056579](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201203234056579.png)

##### 1.2. ChannelHandler的生命周期

| 类型            | 描述                                              |
| --------------- | ------------------------------------------------- |
| handlerAdded    | 当把ChannelHandler添加到ChannelPipeline中时被调用 |
| handlerRemoved  | 当ChannelHandler被从ChannelPipeline中移除时调用   |
| exceptionCaught | 当处理过程中在ChannelPipeline中有错误产生时被调用 |

##### 1.3.ChannelInboundHandler接口

| 类型                      | 描述                                                         |
| ------------------------- | ------------------------------------------------------------ |
| channelRegistered         | 当Channel注册到它的EventLoop并且能够处理I/O时被调用          |
| channelUnregistered       | 当Channel从它的EventLoop注销并不能处理I/O时调用              |
| channelActive             | 当Channel处于活动状态时被调用；Channel已经连接/绑定并且已经就绪 |
| channelInactive           | 当Channel离开活动状态并且不再连接远程节点时被调用            |
| channelRead               | 当从Channel中读取数据时被调用                                |
| channelReadComplete       | 当Channel上的一个读操作完成时被调用                          |
| userEventTriggered        | 当ChannelInboundHandler.fireUserEventTriggered()方法被调用时调用，因为一个POJO被传经了ChannelPipeline |
| channelWritabilityChanged | 当 Channel 的可写状态发生改变时被调用。                      |

![image-20201203235842546](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201203235842546.png)

ChannelInboundHandlerAdapter需要显示的释放资源，比如ReferenceCountUtil.release(msg)

SimpleChannelInboundHandler则不需要任何显示的资源释放，因为它帮你自动释放了

##### 1.4. ChannelOutboundHandler接口

| 类型                                                         | 描述                                              |
| ------------------------------------------------------------ | ------------------------------------------------- |
| bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) | 当请求将Channel绑定到本地地址时被调用             |
| connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,         SocketAddress localAddress, ChannelPromise promise) | 当请求将Channel连接到远程节点时调用               |
| disconnect(ChannelHandlerContext ctx, ChannelPromise promise) | 当请求从远程节点断开时被调用                      |
| close(ChannelHandlerContext ctx, ChannelPromise promise)     | 当请求关闭Channel时被调用                         |
| deregister(ChannelHandlerContext ctx, ChannelPromise promise) | 当请求从它的EventLoop注销时被调用                 |
| read(ChannelHandlerContext ctx)                              | 当请求从Channel读取更多的数据时被调用             |
| write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) | 当请求通过Channel将数据写到远程节点时被调用       |
| flush(ChannelHandlerContext ctx)                             | 当请求通过Channel将入队数据冲刷到远程节点时被调用 |

**ChannelPromise**: 是ChannelFuture的一个子类，其定义了一些可写的方法，入setSuccess()和setFailure()，从而使ChannelFuture不可变。

##### 1.5 ChannelHandler适配器

![image-20201204004332540](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201204004332540.png)

你可以使用 ChannelInboundHandlerAdapter 和 ChannelOutboundHandlerAdapter类作为自己的ChannelHandler 的起始点。这两个适配器分别提供了ChannelInboundHandler和 ChannelOutboundHandler 的基本实现。通过扩展抽象类 ChannelHandlerAdapter，它们获得了它们共同的超接口ChannelHandler 的方法。

ChannelHandlerAdapter 还提供了实用方法 isSharable()。如果其对应的实现被标注为 Sharable，那么这个方法将返回 true，表示它可以被添加到多个 ChannelPipeline中。

在 ChannelInboundHandlerAdapter 和 ChannelOutboundHandlerAdapter 中所提供的方法体调用了其相关联的 ChannelHandlerContext 上的等效方法，从而将事件转发到了 ChannelPipeline 中的下一个 ChannelHandler 中。

#### 2. ChannelPipeline接口

**ChannleHandlerContext:**  ChannelHandlerContext使得ChannelHandler能够和它的ChannelPipeline以及其他的ChannelHandler交互。ChannelHandler可以通知其 所属的ChannelPipeline中的下一个ChannelHandler，甚至可以动态修改它所属的ChannelPipeline。

![image-20201205191812446](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201205191812446.png)

**ChannelPipeline的过滤筛选**

在 ChannelPipeline 传播事件时，它会测试 ChannelPipeline 中的下一个 ChannelHandler 的类型是否和事件的运动方向相匹配。如果不匹配，ChannelPipeline 将跳过该ChannelHandler 并前进到下一个，直到它找到和该事件所期望的方向相匹配的为止。

##### 2.1 修改ChannelPipeline

​													ChannelHandler的用于修改ChannelPipeline的方法

| 名称                                | 描述                                                         |
| ----------------------------------- | ------------------------------------------------------------ |
| addFirst,addLast,addBefore,addAfter | 将一个 ChannelHandler 添加到 ChannelPipeline 中              |
| remove                              | 将一个 ChannelHandler 从 ChannelPipeline 中移除              |
| replace                             | 将 ChannelPipeline中的一个 ChannelHandler 替换为另一个 ChannelHandler |

![image-20201205195916893](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201205195916893.png)

**ChannelHandler 的执行和阻塞**: 通常 ChannelPipeline 中的每一个 ChannelHandler 都是通过它的 EventLoop（I/O 线程）来处理传递给它的事件的。所以至关重要的是不要阻塞这个线程，因为这会对整体的 I/O 处理产生负面的影响。可以使用netty提供的 `DefaultEventExecutorGroup`进行处理。

##### 2.2 触发事件

**ChannelPipeline的入站操作**

###### 					  ![image-20201205202618498](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201205202618498.png)													

ChannelPipeline的出站操作

![image-20201205202826258](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201205202826258.png)

- ChannelPipeline保存了Channel相关联的ChannelHandler;
- ChannelPipeline可以根据需要，通过添加或删除ChannelHandler来动态地修改；
- ChannelPipeline有着丰富的API可以被调用，以响应入站和出站事件

#### 3. ChannelHandlerContext接口

ChannelHandlerContext 代表了 ChannelHandler 和 ChannelPipeline 之间的关联，每当有 ChannelHandler 添加到 ChannelPipeline 中时，都会创建 ChannelHandlerContext。ChannelHandlerContext 的主要功能是管理它所关联的 ChannelHandler 和在同一个 ChannelPipeline 中的其他 ChannelHandler 之间的交互。

相同方法的调用：

Channel和ChannelPipeline: 会沿着整个ChannelPipeline传播

ChannelHandlerContext: 从当前所关联的 ChannelHandler 开始，并且只会传播给位于该ChannelPipeline 中的下一个能够处理该事件的ChannelHandler

![image-20201205204230623](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201205204230623.png)

- ChannelHandlerContext和ChannelHandler之间的关联是不可变的，所以缓存对它的引用的安全的
- 相对于其他类的同名方法，ChannelHandlerContext的方法将产生更短的事件流，应该尽可能地利用这个特性来获得最大的性能

##### 3.1 使用ChannelHandlerContext

![image-20201205211537126](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201205211537126.png)

**从ChannelHandlerContext访问Channel**

![image-20201205214844754](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201205214844754.png)

**从ChannelHandlerContext访问ChannelPipeline**

![image-20201205215008778](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201205215008778.png)

事件流是一样的，从ChannelHandler的级别上看，事件从一个 ChannelHandler到下一个ChannelHandler 的移动是由 ChannelHandlerContext 上的调用完成的

![image-20201205215228539](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201205215228539.png)

为什么会想要从 ChannelPipeline 中的某个特定点开始传播事件呢？

- 为了减少将事件传经对它不感兴趣的 ChannelHandler 所带来的开销
- 为了避免将事件传经那些可能会对它感兴趣的 ChannelHandler

想从指定ChannelHandler传播事件，获取该ChannelHandler的前一个Channelhandler关联的ChannelHandlerContext，调用write()方法将事件传播给该ChannelHandler

![image-20201205215928052](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201205215928052.png)

![image-20201205215842844](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201205215842844.png)

#### 4. 异常处理

##### 1. 处理入站异常

如果在处理入站事件的过程中有异常被抛出，那么它将从它在 ChannelInboundHandler里被触发的那一点开始流经 ChannelPipeline。重写exceptionCaught()方法，捕获异常即可。如果你不实现任何处理入站异常的逻辑（或者没有消费该异常），那么异常会被传到pipeline末端，Netty将会记录该异常没有被处理的事实。

![image-20201205221959288](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201205221959288.png)

- ChannelHandler.exceptionCaught()的默认实现是简单地将当前异常转发给ChannelPipeline 中的下一个 ChannelHandler
- 如果异常到达了 ChannelPipeline 的尾端，它将会被记录为未被处理
- 要想定义自定义的处理逻辑，你需要重写 exceptionCaught()方法。然后你需要决定是否需要将该异常传播

##### 2. 处理出站异常

用于处理出站操作中的正常完成以及异常的选项，都基于以下的通知机制。

- 每个出站操作都将返回一个 ChannelFuture。注册到 ChannelFuture 的 ChannelFutureListener 将在操作完成时被通知该操作是成功了还是出错了。
- 几乎所有的 ChannelOutboundHandler 上的方法都会传入一个 ChannelPromise的实例。作为 ChannelFuture 的子类，ChannelPromise 也可以被分配用于异步通知的监听器。

第一种方式：**添加ChannelFutureListener到ChannelFuture**

![image-20201205230338069](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201205230338069.png)

第二种方式：**将 ChannelFutureListener 添加到即将作为参数传递给 ChannelOutboundHandler 的方法的ChannelPromise**

![image-20201205230101135](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20201205230101135.png)

在调用出站操作时添加 ChannelFutureListener 更合适（第一种方式），而对于一般的异常处理，自定义的ChannelOutboundHandler 实现的方式更加的简单（第二种方式）。



### Chapter7-EventLoop和线程模型

#### 1.Executor线程模型

- 从池的空闲线程列表选择一个Thread，并且指派它去运行一个已经提交的任务（一个Runable实现）
- 当任务完成时，将该Thread返回列表，使其可被重用

优点：池化和重用线程

缺点：不能消除上下文切换带来的开销，并且随线程数量增加而变得明显

![image-20220104203014131](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20220104203014131.png)

#### 2.EventLoop接口

Netty 的 EventLoop 是协同设计的一部分，它采用了两个基本的 API：并发和网络编程

一个EventLoop由一个永不改变的Thread驱动，同时任务（Runnable或者Callable）可以直接提交给EventLoop实现，以立即执行或者调度执行。

![image-20220104203739603](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20220104203739603.png)

#### 3.FIFO

FIrst In First Out，事件和任务都是以先进先出（FIFO）的顺序执行，保证字节内容时按正确的顺序被处理。

在Netty4中，所有的I/O操作和事件都由已经分配给了EventLoop的那个Thread进行处理。

#### 4.任务调度 

调度一个任务以便稍后（延迟）执行或者周期性地执行

##### 4.1JDK的API

```java
public class Test {
    public static void main(String[] args) {
        //10s后执行任务，一旦调度任务完成，就会关闭ScheduledExecutorService以释放资源
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
        executor.schedule(() -> System.out.println("10 seconds later"), 10, TimeUnit.SECONDS);
        executor.shutdown();
    }
}
```

##### 4.2EventLoop

```java
public class Test2 {
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(1234))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) {
                            //do nothing, just test
                        }
                    });
            ChannelFuture future = b.bind().sync();
            System.out.println("NettyServer start...");
            Channel ch = future.channel();
            //使用 EventLoop 调度任务, 10s后执行任务，只执行一次
//            ch.eventLoop().schedule(() -> System.out.println("10 seconds later"), 10, TimeUnit.SECONDS);
            //使用 EventLoop 调度任务, 10s后任务， 每隔10s后执行任务
            ch.eventLoop().scheduleAtFixedRate(() -> System.out.println("10 seconds later"), 10, 10, TimeUnit.SECONDS);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
```

#### 5.实现细节

##### 5.1线程管理

Netty线程模型的卓越性能取决于对于当前执行的Thread身份的确认，即确定它是否时分配给当前Channel和EventLoop的那一个线程。

如果是，则将所提交的代码直接执行。否则，EventLoop调度该任务以便稍后执行，并将它放入内部队列。

注意，不要将长时间运行的的任务放入执行队列，会阻塞IO线程，建议使用EventExecutor。



![image-20220104205902131](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20220104205902131.png)

##### 5.2EventLoop线程分配

相比于传统的IO 传输（一对一，即一个线程对应一个通道），EventLoop的异步传输（一对多，即一个线程对应多个通道）可以使用少量的线程来支撑大量的Channel，避免过多线程上文切换带来的额外开销。

EventLoopGroup为每个新建的Channel分配一个EventLoop（管理多个Channel），使用事件循环（round-robin）方式进行分配以获取一个均衡的分布。

一旦一个Channel被分配给了一个EventLoop，那么它整个生命周期都是用这个EventLoop及其绑定的线程，避免了线程安全和线程同步问题（有且仅有当前这一个线程）。

![image-20220104211050089](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20220104211050089.png)

### Chapter8-引导

引导，即将ChannelPipeline、ChannelHandler、EventLoop组织配置，成为一个可以实际运行的应用



![image-20220110202427957](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20220110202427957.png)



#### 8.1Bootstrap-引导客户端

![image-20220110203343888](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20220110203343888.png)

![image-20220110203103764](img/image-20220110203103764.png)

![image-20220110203115724](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20220110203115724.png)

```java
public class ClientA {
    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new SimpleChannelInboundHandler<ByteBuf>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
                        System.out.println("Reveive data from ServerA: " + byteBuf.toString(CharsetUtil.UTF_8));
                    }

                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        ctx.channel().eventLoop().scheduleAtFixedRate(() -> {
                            ByteBuf data = Unpooled.copiedBuffer("Hello, I am ClientA", CharsetUtil.UTF_8);
                            ctx.writeAndFlush(data);
                        }, 0, 5, TimeUnit.SECONDS);
                    }
                });
        ChannelFuture future = bootstrap.connect(new InetSocketAddress("127.0.0.1", 8080));
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                System.out.println("Connect success");
            } else {
                System.out.println("Connect failed");
                channelFuture.cause().printStackTrace();
            }
        });

    }
}
```

#### 8.2ServerBootstrap-引导服务器

![image-20220110203638620](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20220110203638620.png)

![image-20220110203746456](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20220110203746456.png)

```java
public class ServerA {
    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        //group、channel、childHandler三者是必须的，不然会导致IllegalStateException
        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    /**
                     * ChannelInitializer是ChannelInboundHandlerAdapter子类，可以添加多个处理器
                     * @param channel
                     * @throws Exception
                     */
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        //可以继续添加处理器，解码器，编码器等等
                        channel.pipeline().addLast(new ServerHandler());
                    }
                });
        ChannelFuture future = serverBootstrap.bind(new InetSocketAddress(8080));
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                System.out.println("Server bound");
            } else {
                System.out.println("Bind attempt failed");
                channelFuture.cause().printStackTrace();
            }
        });
    }

    /**
     * 服务器端处理器
     */
    static class ServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
        private ChannelFuture connectFuture;

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            Bootstrap bootstrap = new Bootstrap();
            //使用与分配给已被接受的子Channel相同的EventLoop
            //ServerA与ServerB通信，ServerA将ServerB的消息转发给ClientA
            bootstrap.group(ctx.channel().eventLoop())
                    .channel(NioSocketChannel.class)
                    .handler(new SimpleChannelInboundHandler<ByteBuf>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                            System.out.println("Reveived data from ServerB：" + byteBuf.toString(CharsetUtil.UTF_8));
                            ByteBuf msg = Unpooled.copiedBuffer("Hello, I am ServerA", CharsetUtil.UTF_8);
                            channelHandlerContext.writeAndFlush(msg);
                            //将ServerB的消息转发给ClientA
//                            System.out.println("before:" + byteBuf.refCnt());
                            //SimpleChannelInboundHandler 它会自动进行一次释放(即引用计数减1),如果不想创建新的数据, 则可以直接在原对象里调用 byteBuf.retain() 进行引用计数加1
                            byteBuf.retain();
                            ctx.writeAndFlush(byteBuf);
//                            System.out.println("after:" + byteBuf.refCnt());
                        }
                    });
            connectFuture = bootstrap.connect(new InetSocketAddress("127.0.0.1", 1234));
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
            if (connectFuture.isDone()) {
                //当连接完成后，执行一些数据操作（如代理）
                System.out.println("Reveived data from ClientA：" + byteBuf.toString(CharsetUtil.UTF_8));
                ByteBuf data = Unpooled.copiedBuffer("Hello, ClientA", CharsetUtil.UTF_8);
                ctx.writeAndFlush(data);
            }
        }
    }
}
```

```java
public class ServerB {
    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new SimpleChannelInboundHandler<ByteBuf>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                        System.out.println("Reveive data from ServerA: " + byteBuf.toString(CharsetUtil.UTF_8));
                    }

                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        ctx.channel().eventLoop().scheduleAtFixedRate(() -> {
                            ByteBuf data = Unpooled.copiedBuffer("Hello, I am ServerB", CharsetUtil.UTF_8);
                            ctx.writeAndFlush(data);
                        }, 0, 5, TimeUnit.SECONDS);
                    }
                });
        ChannelFuture future = serverBootstrap.bind(new InetSocketAddress(1234));
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                System.out.println("Server bound");
            } else {
                System.out.println("Bind attempt failed");
                channelFuture.cause().printStackTrace();
            }
        });
    }

}
```

#### 8.3ChannelOption&Attr

```java
public class ClientB {
    public static void main(String[] args) {
        final AttributeKey<Integer> id = AttributeKey.newInstance("ID");
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new SimpleChannelInboundHandler<ByteBuf>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
                        System.out.println("Reviive data from ServerB: " + byteBuf.toString(CharsetUtil.UTF_8));
                    }

                    @Override
                    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                        Integer idValue = ctx.channel().attr(id).get();
                        System.out.println("idValue: " + idValue);
                        //do something with the idValue
                    }
                });
        ChannelFuture future = bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .attr(id, 123456)
                .connect(new InetSocketAddress("127.0.0.1", 1234));
        future.syncUninterruptibly();
    }
}
```

#### 8.4引导DatagramChannel

```java
public class ClientC {
    public static void main(String[] args) {
        EventLoopGroup group = new OioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(OioSocketChannel.class)
                .handler(new SimpleChannelInboundHandler<ByteBuf>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                        System.out.println("Receive data: " + byteBuf.toString(CharsetUtil.UTF_8));
                    }
                });
        //调用bind()方法，因为该协议是无连接的
        ChannelFuture future = bootstrap.bind(new InetSocketAddress(0));
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                System.out.println("Channel bind");
            } else {
                System.out.println("bind attempt failed");
                channelFuture.cause().printStackTrace();
            }
        });
    }
}
```



### Chapter9-单元测试

#### 9.1EmbeddedChannel

Netty提供了一种特殊的Channel实现-EmbeddedChannel，专门用来对ChannelHandler进行单元测试

![image-20220111235435043](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20220111235435043.png)

- 入站数据由 **ChannelInboundHandler** 处理，代表从远程节点读取的数据，使用Inbound()方法
- 出站数据由 **ChannelOutboundHandler** 处理，代表将要写到远程节点的数据，使用Outbound()方法

![image-20220111235804630](https://raw.githubusercontent.com/Qianfugen/blog-img/main/image-20220111235804630.png)

#### 9.2测试入站消息

待测试的入站处理器

```java
public class FixedLenghtFrameDecoder extends ByteToMessageDecoder {
    private final int frameLength;

    public FixedLenghtFrameDecoder(int frameLength) {
        if (frameLength <= 0) {
            throw new IllegalArgumentException("frameLength must be a positive integer: " + frameLength);
        }
        this.frameLength = frameLength;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (in.readableBytes() >= frameLength) {
            ByteBuf byteBuf = in.readBytes(frameLength);
            out.add(byteBuf);
        }
    }
}
```

测试类

```java
public class FixedLenghtFrameDecoderTest {

    @Test
    public void testFramesDecoded() {
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            buf.writeByte(i);
        }
        ByteBuf input = buf.duplicate();
        EmbeddedChannel channel = new EmbeddedChannel(new FixedLenghtFrameDecoder(3));

        //write bytes
        assertTrue(channel.writeInbound(input.retain()));
        assertTrue(channel.finish());

        //read bytes
        ByteBuf read = (ByteBuf) channel.readInbound();
        assertEquals(buf.readSlice(3), read);
        read.release();

        read = (ByteBuf) channel.readInbound();
        assertEquals(buf.readSlice(3), read);
        read.release();

        read = (ByteBuf) channel.readInbound();
        assertEquals(buf.readSlice(3), read);
        read.release();

        assertNull(channel.readInbound());

    }

    @Test
    public void testFramesDecoded2() {
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            buf.writeByte(i);
        }
        ByteBuf input = buf.duplicate();
        EmbeddedChannel channel = new EmbeddedChannel(new FixedLenghtFrameDecoder(3));

        //write bytes
        assertFalse(channel.writeInbound(input.readBytes(2)));
        assertTrue(channel.writeInbound(input.readBytes(7)));
        assertTrue(channel.finish());

        //read bytes
        ByteBuf read = (ByteBuf) channel.readInbound();
        assertEquals(buf.readSlice(3), read);
        read.release();

        read = (ByteBuf) channel.readInbound();
        assertEquals(buf.readSlice(3), read);
        read.release();

        read = (ByteBuf) channel.readInbound();
        assertEquals(buf.readSlice(3), read);
        System.out.println(ByteBufUtil.hexDump(read));
        read.release();

        assertNull(channel.readInbound());
        buf.release();
    }
}
```

#### 9.3测试出站消息

待测试的出站处理器

```java
public class AbsIntegerEncoder extends MessageToMessageEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (in.readableBytes() >= 4) {
            int value = Math.abs(in.readInt());
            out.add(value);
        }
    }
}
```

测试类

```java
public class AbsIntegerEncoderTest {

    @Test
    public void testEncoded() {
        ByteBuf buf = Unpooled.buffer();
        for (int i = 1; i < 10; i++) {
            buf.writeInt(i * -1);
        }
        EmbeddedChannel channel = new EmbeddedChannel(new AbsIntegerEncoder());
        assertTrue(channel.writeOutbound(buf));
        assertTrue(channel.finish());

        //read bytes
        for (Integer i = 1; i < 10; i++) {
            assertEquals(i, channel.readOutbound());
        }
        assertNull(channel.readOutbound());
    }
}
```

#### 9.4测试异常处理

待测试的异常处理

```java
public class FrameChunkDecoder extends ByteToMessageDecoder {
    private final int maxFrameSize;

    public FrameChunkDecoder(int maxFrameSize) {
        if (maxFrameSize <= 0) {
            throw new IllegalArgumentException("maxFrameSize must >= 0");
        }
        this.maxFrameSize = maxFrameSize;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int readableBytes = in.readableBytes();
        if (readableBytes > maxFrameSize) {
            //如果帧超过限制，就丢弃
            in.clear();
            throw new TooLongFrameException();
        }
        ByteBuf byteBuf = in.readBytes(readableBytes);
        out.add(byteBuf);
    }
}
```

测试类

```java
public class FrameChunkDecoderTest {

    @Test
    public void testFrameDecoded() {
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            buf.writeByte(i);
        }
        ByteBuf input = buf.duplicate();
        EmbeddedChannel channel = new EmbeddedChannel(new FrameChunkDecoder(3));

        //write bytes
        assertTrue(channel.writeInbound(input.readBytes(2)));
        try {
            assertTrue(channel.writeInbound(input.readBytes(4)));
            Assert.fail();
        } catch (TooLongFrameException e) {
//            e.printStackTrace();
        }
        assertTrue(channel.writeInbound(input.readBytes(3)));
        assertTrue(channel.finish());

        //read Bytes
        ByteBuf read = (ByteBuf) channel.readInbound();
        assertEquals(buf.readSlice(2), read);

        read = (ByteBuf) channel.readInbound();
        assertEquals(buf.skipBytes(4).readSlice(3), read);
        read.release();
        buf.release();
    }

}
```



### Chapter10-编解码器

#### 1.概念

编码器是将消息转换为适合于传输的格式（最有可能的就是字节流），

解码器则是将网络字节流转换回应用程序的消息格式。

#### 2.解码器

- 将字节解码为消息：ByteToMessageDecoder 和 ReplayingDecoder
- 将一种消息类型解码为另一种：MessageToMessageDecoder
- 都需要重写**decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)**方法；

##### 2.1ByteToMessageDecoder 

```java
public class ToIntegerDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        System.out.println(ByteBufUtil.hexDump(in));
        //在调用 readInt()方法前不得不验证所输入的 ByteBuf 是否具有足够的数据有点繁琐
        if (in.readableBytes() >= 4) {
            out.add(in.readInt());
        }
        out.forEach(System.out::println);
    }
}
```

##### 2.2ReplayingDecoder

```java
public class ToInteger2Decoder extends ReplayingDecoder<Void> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        ByteBufUtil.hexDump(in);
        //字节数不够，报错：java.lang.NegativeArraySizeException
        out.add(in.readInt());
    }
}
```

##### 2.3MessageToMessageDecoder

```java
public class IntegerToStringDecoder extends MessageToMessageDecoder<Integer> {
    @Override
    protected void decode(ChannelHandlerContext ctx, Integer in, List<Object> out) {
        System.out.println("数字" + in + "转成字符串");
        out.add(String.valueOf(in));
    }
}
```

##### 2.4其他

还有netty自带的**LineBasedFrameDecoder**、**HttpObjectAggregator**等；

**TooLongFrameException 类**

由于 Netty 是一个异步框架，所以需要在字节可以解码之前在内存中缓冲它们。因此，不能 让解码器缓冲大量的数据以至于耗尽可用的内存。为了解除这个常见的顾虑，Netty 提供了 TooLongFrameException 类，其将由解码器在帧超出指定的大小限制时抛出。

```java
public class SafeByteToMessageDecoder extends ByteToMessageDecoder {
    private static final int MAX_FRAME_SIZE = 1024;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int size = in.readableBytes();
        if (size > MAX_FRAME_SIZE) {
            in.skipBytes(size);
            throw new TooLongFrameException("Frame too big");
        }
        out.add(in);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```

#### 3.编码器

- 将消息编码为字节
- 将消息编码为消息
- 都需要重写**encode(ChannelHandlerContext ctx, Short msg, ByteBuf out)**方法

##### 3.1MessageToByteEncoder

```java
public class ShortToByteEncoder extends MessageToByteEncoder<Short> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Short msg, ByteBuf out) {
        out.writeShort(msg);
    }
}
```

##### 3.2MessageToMessageEncoder

```java
public class IntegerToStringEncoder extends MessageToMessageEncoder<Integer> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Integer msg, List<Object> out) {
        out.add(String.valueOf(msg));
    }
}
```

##### 3.3其他

还有netty自带的**ProtobufEncoder**等，处理了Google 的 Protocol Buffers 规范所定义的数据格式

#### 4.编解码器

同时实现了 ChannelInboundHandler 和 ChannelOutboundHandler 接口

##### 4.1抽象类 ByteToMessageCodec

任何的请求/响应协议都可以作为使用ByteToMessageCodec的理想选择。例如，在某个 SMTP的实现中，编解码器将读取传入字节，并将它们解码为一个自定义的消息类型，如 SmtpRequest, 而在接收端，当一个响应被创建时，将会产生一个SmtpResponse，其将被 编码回字节以便进行传输

```java
  @Override
    public SMTPClientFutureListener<FutureResult<SMTPResponse>> getListener(SMTPClientSession session, SMTPRequest request) throws SMTPException {

        String cmd = request.getCommand().toUpperCase(Locale.UK);
        String arg = request.getArgument();
        if (arg != null) {
            arg = arg.toUpperCase(Locale.UK);
        }
        if (SMTPRequest.EHLO_COMMAND.equals(cmd)) {
            return EhloResponseListener.INSTANCE;
        } else if (SMTPRequest.HELO_COMMAND.equals(cmd)) {
            return HeloResponseListener.INSTANCE;
        } else if (SMTPRequest.MAIL_COMMAND.equals(cmd)) {
            return MailResponseListener.INSTANCE;
        } else if (SMTPRequest.RCPT_COMMAND.equals(cmd)) {
            return RcptResponseListener.INSTANCE;
        } else if (SMTPRequest.DATA_COMMAND.equals(cmd)) {
            return DataResponseListener.INSTANCE;
        } else if (SMTPRequest.STARTTLS_COMMAND.equals(cmd)) {
            return StartTlsResponseListener.INSTANCE;
        } else if (SMTPRequest.AUTH_COMMAND.equals(cmd) && arg != null) {
            if (arg.equals(SMTPRequest.AUTH_PLAIN_ARGUMENT)) {
                return AuthPlainResponseListener.INSTANCE;
            } else if (arg.equals(SMTPRequest.AUTH_LOGIN_ARGUMENT)) {
                return AuthLoginResponseListener.INSTANCE;
            }
        } else if (SMTPRequest.QUIT_COMMAND.equals(cmd)) {
            return QuitResponseListener.INSTANCE;
        }

        throw new SMTPException("No valid callback found for request " + request);
    }
```

##### 4.2抽象类 MessageToMessageCodec

```java
public class WebSocketConvertHandler extends MessageToMessageCodec<WebSocketFrame, WebSocketConvertHandler.MyWebSocketFrame> {
    @Override
    protected void encode(ChannelHandlerContext ctx, MyWebSocketFrame msg, List<Object> out) {
        ByteBuf payload = msg.getData().duplicate().retain();
        switch (msg.getType()) {
            case BINARY:
                out.add(new BinaryWebSocketFrame(payload));
                break;
            case CLOSE:
                out.add(new CloseWebSocketFrame(true, 0, payload));
                break;
            case PING:
                out.add(new PingWebSocketFrame(payload));
                break;
            case PONG:
                out.add(new PongWebSocketFrame(payload));
                break;
            case TEXT:
                out.add(new TextWebSocketFrame(payload));
                break;
            case CONTINUATION:
                out.add(new ContinuationWebSocketFrame(payload));
                break;
            default:
                throw new IllegalStateException("Unsupported websocket msg: " + msg);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) {
        ByteBuf payload = msg.content().duplicate().retain();
        if (msg instanceof BinaryWebSocketFrame) {
            out.add(new MyWebSocketFrame(MyWebSocketFrame.FrameType.BINARY, payload));
        } else if (msg instanceof CloseWebSocketFrame) {
            out.add(new MyWebSocketFrame(MyWebSocketFrame.FrameType.CLOSE, payload));
        } else if (msg instanceof PingWebSocketFrame) {
            out.add(new MyWebSocketFrame(MyWebSocketFrame.FrameType.PING, payload));
        } else if (msg instanceof PongWebSocketFrame) {
            out.add(new MyWebSocketFrame(MyWebSocketFrame.FrameType.PING, payload));
        } else if (msg instanceof TextWebSocketFrame) {
            out.add(new MyWebSocketFrame(MyWebSocketFrame.FrameType.TEXT, payload));
        } else if (msg instanceof ContinuationWebSocketFrame) {
            out.add(new MyWebSocketFrame(MyWebSocketFrame.FrameType.CONTINUATION, payload));
        } else {
            throw new IllegalStateException("Unsupported websocket msg: " + msg);
        }

    }

    public static final class MyWebSocketFrame {
        enum FrameType {
            BINARY,
            CLOSE,
            PING,
            PONG,
            TEXT,
            CONTINUATION
        }

        private final FrameType type;
        private final ByteBuf data;

        public MyWebSocketFrame(FrameType type, ByteBuf data) {
            this.type = type;
            this.data = data;
        }

        public FrameType getType() {
            return type;
        }

        public ByteBuf getData() {
            return data;
        }
    }
}
```

##### 4.3CombinedChannelDuplexHandler 类

```java
public class CombineByteCharCodec extends CombinedChannelDuplexHandler<CombineByteCharCodec.ByteToCharDecoder, CombineByteCharCodec.CharToByteEncoder> {

    public CombineByteCharCodec() {
        super(new ByteToCharDecoder(), new CharToByteEncoder());
    }

    /**
     * 解码器
     */
    static class ByteToCharDecoder extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            while (in.readableBytes() >= 2) {
                out.add(in.readChar());
            }
        }
    }

    /**
     * 编码器
     */
    static class CharToByteEncoder extends MessageToByteEncoder<Character> {
        @Override
        protected void encode(ChannelHandlerContext ctx, Character msg, ByteBuf out) {
            out.writeChar(msg);
        }
    }

}
```

