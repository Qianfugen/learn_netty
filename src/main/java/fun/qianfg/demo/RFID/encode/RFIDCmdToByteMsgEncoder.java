package fun.qianfg.demo.RFID.encode;

import fun.qianfg.demo.RFID.RFIDCmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RFIDCmdToByteMsgEncoder extends MessageToByteEncoder<RFIDCmd> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RFIDCmd msg, ByteBuf out) throws Exception {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(msg.getBootCode());
        buf.writeByte(msg.getLength());
        buf.writeByte(msg.getCmd());
        for (int param : msg.getData()) {
            buf.writeByte(param);
        }
        buf.writeByte(msg.getCheckSum());
        System.out.println(ByteBufUtil.hexDump(buf));
        ctx.writeAndFlush(buf);
    }

}
