package chapter6;

import io.netty.channel.*;

public class ServerHandler2 extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // 将 ChannelFutureListener 添加到即将作为参数传递给 ChannelOutboundHandler 的方法的ChannelPromise
        promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                    future.channel().close();
                }
            }
        });
    }
}
