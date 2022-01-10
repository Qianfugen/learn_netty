package fun.qianfg.chapter8;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Function:
 *
 * @author qianfg
 * @date 2022/1/6 22:32
 * @Email: 287541326@qq.com
 */
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
