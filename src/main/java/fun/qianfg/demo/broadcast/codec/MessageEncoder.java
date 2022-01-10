package fun.qianfg.demo.broadcast.codec;


import fun.qianfg.demo.broadcast.Request.RequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * 用于检验和、头尾处理标志位及转义处理
 */
public class MessageEncoder extends MessageToMessageEncoder<RequestMessage> {


    @Override
    protected void encode(ChannelHandlerContext ctx, RequestMessage msg, List<Object> list) throws Exception {
        preprocessMsg(msg);

        int msgBodyProperty = getMsgBodyProperty(msg);

        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(msg.getVersionNO());
        buf.writeShort(msg.getMsgID());
        buf.writeShort(msgBodyProperty);
        buf.writeByte(msg.getDeviceType());

        int[] deviceAddress = msg.getDeviceAddress();
        for (int b : deviceAddress) {
            buf.writeByte(b);
        }


        buf.writeShort(msg.getSerialNO());

        if (msg.getIsSubpackage()) {
            buf.writeShort(msg.getSubPackageAmount());
            buf.writeShort(msg.getSubPackageNumber());
        }

        if (msg.getMsgBody() != null) {
            int[] msgBody = msg.getMsgBody();
            for (int b : msgBody) {
                buf.writeByte(b);
            }
        }

        addCheckSum(buf);

        ByteBuf transferdBytebuf = transfer(buf);

        ByteBuf writeBuf = addIdentifier(transferdBytebuf);
        list.add(writeBuf);
    }

    private void preprocessMsg(RequestMessage msg) {
        msg.encode();
    }

    /**
     * 将分包、数据加密方式、消息体长度封装在一起作为消息体属性。长度2字节
     */
    private int getMsgBodyProperty(RequestMessage msg) {
        int msgBodyProperty = 0;

        if (msg.getIsSubpackage()) {
            msgBodyProperty = msgBodyProperty >> 13;
            msgBodyProperty = 1;
            msgBodyProperty = msgBodyProperty << 13;
        }

        msgBodyProperty = msgBodyProperty >> 10;
        msgBodyProperty = msgBodyProperty | msg.getEncryptType();
        msgBodyProperty = msgBodyProperty << 10;

        msgBodyProperty = msgBodyProperty | msg.getBodyLength();

        return msgBodyProperty;
    }


    private void addCheckSum(ByteBuf srcMsg) {
        byte checkSum = calculateCheckSum(srcMsg);
        srcMsg.writeByte(checkSum);
    }

    private byte calculateCheckSum(ByteBuf srcMsg) {
        byte checkSum = 0;
        while (srcMsg.isReadable()) {
            byte b = srcMsg.readByte();
            checkSum = (byte) (checkSum ^ b);
        }
        srcMsg.readerIndex(0);

        return checkSum;
    }

    // 转义
    private ByteBuf transfer(ByteBuf srcMsg) {
        ByteBuf transferdBytebuf = Unpooled.buffer();

        while (srcMsg.isReadable()) {
            byte b = srcMsg.readByte();
            if (b == 0x7e) {
                transferdBytebuf.writeByte((byte) 0x7d);
                transferdBytebuf.writeByte((byte) 0x02);
            } else if (b == 0x7d) {
                transferdBytebuf.writeByte((byte) 0x7d);
                transferdBytebuf.writeByte((byte) 0x01);
            } else {
                transferdBytebuf.writeByte(b);
            }
        }

        return transferdBytebuf;
    }

    // 增加标识位
    private ByteBuf addIdentifier(ByteBuf transferdBytebuf) {
        ByteBuf writeBuf = Unpooled.buffer();
        writeBuf.writeByte((byte) 0x7e);
        writeBuf.writeBytes(transferdBytebuf);
        writeBuf.writeByte((byte) 0x7e);
        return writeBuf;
    }
}
