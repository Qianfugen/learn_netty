package fun.qianfg.demo.broadcast.codec;

import fun.qianfg.demo.broadcast.Response.ResponseMessage;
import fun.qianfg.demo.broadcast.bodydecoder.BaseMessageBodyDecoder;
import fun.qianfg.demo.broadcast.core.MessageBodyDecoderFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * 用于校验和、转义处理
 */
public class MessageDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final int IDENTIFIER = 0x7e;

    @Override
    public boolean acceptInboundMessage(Object msg) {
        ByteBuf in = (ByteBuf) msg;
        if (in.readableBytes() < 1) {
            return false;
        }

        if (in.readUnsignedByte() != IDENTIFIER) {
            return false;
        }

        in.markReaderIndex();

        if (in.readUnsignedByte() != 1) {
            return false;
        }

        in.resetReaderIndex();
        return true;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        ByteBuf outBuf = partitionByteBuf(byteBuf);
        validateCheckSum(outBuf);
        ResponseMessage response = decodeByteBuf(outBuf);
        list.add(response);
    }


    private ByteBuf partitionByteBuf(ByteBuf in) {
        ByteBuf outBuf = Unpooled.buffer();

        // 是否读到了一条完整的消息
        boolean isFullMsg = false;

        while (in.readableBytes() > 0) {
            int b = in.readUnsignedByte();

            if (b == IDENTIFIER) {
                // 读到了标识位，已经获取了一条完整的信息
                isFullMsg = true;
                break;
            }
            int writeByte = b;
            if (b == 0x7d) {
                // 字节为0x7d的情况，需要进行转义
                int nextByte = in.readUnsignedByte();
                if (nextByte == 0x01) {
                    writeByte = 0x7d;
                } else if (nextByte == 0x02) {
                    writeByte = 0x7e;
                }
            }
            outBuf.writeByte(writeByte);
        }

        // 消息不完整，但没有可读字节了，回滚ByteBuf的读索引
        if (isFullMsg == false) {
            throw new RuntimeException("可读字节不够");
        }
        return outBuf;
    }

    private void validateCheckSum(ByteBuf buf) {
        ByteBuf copyBuf = buf.copy();
        byte[] byteArr = new byte[copyBuf.readableBytes()];
        copyBuf.readBytes(byteArr);

        byte checkSum = 0;
        for (int i = 0; i < byteArr.length - 1; i++) {
            checkSum ^= byteArr[i];
        }

        if (checkSum != byteArr[byteArr.length - 1]) {
            throw new RuntimeException("收到的消息校验和不正确");
        }

        ReferenceCountUtil.release(copyBuf);
    }

    @SuppressWarnings("rawtypes")
    private ResponseMessage decodeByteBuf(ByteBuf buf) {
        // 从byteBuf中解码出信息
        int versionNo = buf.readUnsignedByte();
        int msgID = buf.readUnsignedShort();

        int msgBodyProperty = buf.readUnsignedShort();
        boolean isSubPackage = getIsSubPackageByMsgBodyProperty(msgBodyProperty);
        int encryptType = getEncryptTypeByMsgBodyProperty(msgBodyProperty);
        int bodyLength = getBodyLengthByMsgBodyProperty(msgBodyProperty);

        int deviceType = buf.readUnsignedByte();
        int[] deviceAddress = new int[6];
        for (int i = 0; i < 6; i++) {
            deviceAddress[i] = buf.readUnsignedByte();
        }

        int serialNumber = buf.readUnsignedShort();
        int[] msgBody = new int[bodyLength];
        for (int i = 0; i < bodyLength; i++) {
            msgBody[i] = buf.readUnsignedByte();
        }

        BaseMessageBodyDecoder decoder = MessageBodyDecoderFactory.Instance.getMessage(msgID);
        // 初始化消息
        ResponseMessage msg = new ResponseMessage(decoder.decode(msgBody));
        msg.setVersionNO(versionNo);
        msg.setMsgID(msgID);
        msg.setDeviceType(deviceType);
        msg.setDeviceAddress(deviceAddress);
        msg.setSerialNO(serialNumber);
        msg.setSubpackage(isSubPackage);
        msg.setEncryptType(encryptType);
        msg.setBodyLength(bodyLength);
        return msg;
    }

    private boolean getIsSubPackageByMsgBodyProperty(int msgBodyProperty) {
        return (msgBodyProperty >> 12) > 0;
    }

    private int getEncryptTypeByMsgBodyProperty(int msgBodyProperty) {
        return (msgBodyProperty >> 9) & 0b0111;
    }

    private int getBodyLengthByMsgBodyProperty(int msgBodyProperty) {
        return msgBodyProperty & 0x01FF;
    }

}
