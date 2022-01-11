package fun.qianfg.chapter9;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Function: 帧大小测试
 *
 * @author qianfg
 * @date 2022/1/11 20:55
 * @Email: 287541326@qq.com
 */
public class FixedLenghtFrameDecoder extends ByteToMessageDecoder {
    private final int frameLength;

    public FixedLenghtFrameDecoder(int frameLength) {
        if (frameLength <= 0) {
            throw new IllegalArgumentException("frameLength must be a positive integer: " + frameLength);
        }
        this.frameLength = frameLength;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (in.readableBytes() >= frameLength) {
            ByteBuf byteBuf = in.readBytes(frameLength);
            out.add(byteBuf);
        }
    }
}
