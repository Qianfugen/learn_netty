package fun.qianfg.chapter10.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Function: 编码器，继承自MessageToByteEncoder,将消息转成字节流
 *
 * @author qianfg
 * @date 2021/12/28 20:39
 * @Email: 287541326@qq.com
 */
public class ShortToByteEncoder extends MessageToByteEncoder<Short> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Short msg, ByteBuf out) {
        out.writeShort(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
