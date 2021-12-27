package fun.qianfg.chapter10.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Function: 解码器 继承自ByteToMessageDecoder，需要判断字节数
 *
 * @author qianfg
 * @date 2021/12/27 20:50
 * @Email: 287541326@qq.com
 */
public class ToIntegerDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        System.out.println(ByteBufUtil.hexDump(in));
        //在调用 readInt()方法前不得不验证所输入的 ByteBuf 是否具有足够的数据有点繁琐
        if (in.readableBytes() >= 4) {
            out.add(in.readInt());
        }
        out.forEach(System.out::println);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
