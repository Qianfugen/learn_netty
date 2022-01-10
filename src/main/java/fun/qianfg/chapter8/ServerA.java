package fun.qianfg.chapter8;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * Function:BootStrap引导类示例
 * 情景：
 * 假设你的服务器正在处理一个客户端的请求，这个请求需要它充当第三方系统的客户端。当
 * 一个应用程序（如一个代理服务器）必须要和组织现有的系统（如 Web 服务或者数据库）集成
 * 时，就可能发生这种情况。在这种情况下，将需要从已经被接受的子 Channel 中引导一个客户
 * 端 Channel。
 * 方案：
 * 通过将已被接受的子 Channel 的 EventLoop 传递给 Bootstrap
 * 的 group()方法来共享该 EventLoop。因为分配给 EventLoop 的所有 Channel 都使用同一
 * 个线程，所以这避免了额外的线程创建，以及前面所提到的相关的上下文切换。
 * 目的：
 * 尽可能地重用 EventLoop，以减少线程创建所带来的开销
 *
 * @author qianfg
 * @date 2022/1/6 21:44
 * @Email: 287541326@qq.com
 */
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
