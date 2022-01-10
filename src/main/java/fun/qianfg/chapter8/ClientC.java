package fun.qianfg.chapter8;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * Function:引导 DatagramChannel
 * 无连接协议，与TCP协议不同的是，不再调用connect()方法，而是调用bind()方法
 *
 * @author qianfg
 * @date 2022/1/10 21:25
 * @Email: 287541326@qq.com
 */
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
