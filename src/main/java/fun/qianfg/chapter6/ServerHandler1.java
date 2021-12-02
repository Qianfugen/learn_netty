package fun.qianfg.chapter6;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;

public class ServerHandler1 extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        System.out.println("Server received: " + msg.toString(CharsetUtil.UTF_8));
        ChannelPipeline pipeline = ctx.pipeline();
        // 从ChannelHandlerContext访问ChannelPipeline
        pipeline.writeAndFlush(Unpooled.copiedBuffer("hello,client", CharsetUtil.UTF_8));

        // 添加ChannelFutureListener到ChannelFuture
        ChannelFuture future = ctx.write(Unpooled.copiedBuffer("hello,world", CharsetUtil.UTF_8));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                    future.channel().close();
                }
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
