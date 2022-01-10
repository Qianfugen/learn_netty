package fun.qianfg.chapter8;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * Function:使用 Netty 的 ChannelOption 和属性
 *
 * @author qianfg
 * @date 2022/1/10 20:42
 * @Email: 287541326@qq.com
 */
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
