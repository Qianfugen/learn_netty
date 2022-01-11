package fun.qianfg.chapter9;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;

import java.util.List;

/**
 * Function: 帧大小限制
 *
 * @author qianfg
 * @date 2022/1/11 23:25
 * @Email: 287541326@qq.com
 */
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
