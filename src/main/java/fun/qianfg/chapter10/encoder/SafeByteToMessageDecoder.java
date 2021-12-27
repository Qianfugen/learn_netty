package fun.qianfg.chapter10.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;

import java.util.List;

/**
 * Function: TooLongFrameException类, 解码器在帧超出指定的大小限制时抛出
 *
 * @author qianfg
 * @date 2021/12/27 23:17
 * @Email: 287541326@qq.com
 */
public class SafeByteToMessageDecoder extends ByteToMessageDecoder {
    private static final int MAX_FRAME_SIZE = 10;

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
