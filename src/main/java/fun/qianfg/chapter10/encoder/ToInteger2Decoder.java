package fun.qianfg.chapter10.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * Function: 编码器，继承自ReplayingDecoder，不需要判断字节数
 *
 * @author qianfg
 * @date 2021/12/27 21:22
 * @Email: 287541326@qq.com
 */
public class ToInteger2Decoder extends ReplayingDecoder<Void> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        ByteBufUtil.hexDump(in);
        //字节数不够，报错：java.lang.NegativeArraySizeException
        out.add(in.readInt());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
