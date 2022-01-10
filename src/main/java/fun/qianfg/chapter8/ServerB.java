package fun.qianfg.chapter8;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Function:第三方服务端
 *
 * @author qianfg
 * @date 2022/1/6 21:44
 * @Email: 287541326@qq.com
 */
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
