package chapter5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class Test {
    public static void main(String[] args) {

        // 1. 访问数据
//        ByteBuf buffer = Unpooled.copiedBuffer("hello,world", CharsetUtil.UTF_8);
//        for (int i = 0; i < buffer.capacity(); i++) {
//            byte b = buffer.getByte(i);
//            System.out.println((char) b);
//        }

        // 2.读取所有数据
//        ByteBuf buffer = Unpooled.copiedBuffer("hello,world", CharsetUtil.UTF_8);
//        while (buffer.isReadable()) {
//            System.out.println((char) buffer.readByte());
//        }

        // 3.可写字节
//        ByteBuf buffer = Unpooled.copiedBuffer("hello,world", CharsetUtil.UTF_8);
//        while (buffer.writableBytes() >= 4) {
//            buffer.writeInt(1);
//        }

        // 4.查找
//        ByteBuf buffer = Unpooled.copiedBuffer("hello,world", CharsetUtil.UTF_8);
//        int index = buffer.indexOf(0, buffer.capacity(), (byte) ',');
//        System.out.println(index);

        // 5.切片
//        ByteBuf buffer = Unpooled.copiedBuffer("hello,world", CharsetUtil.UTF_8);
//        ByteBuf slice = buffer.slice(0, 5);
//        System.out.println(slice.toString(CharsetUtil.UTF_8));
//        buffer.setByte(0, (byte) 'J');
//        // true, 数据是共享的，对其中一个所做的更改对另外一个也是可见的
//        System.out.println(buffer.getByte(0) == slice.getByte(0));

        // 6.复制
//        ByteBuf buffer = Unpooled.copiedBuffer("hello,world", CharsetUtil.UTF_8);
//        ByteBuf copyBuffer = buffer.copy();
//        buffer.setByte(0, (byte) 'J');
//        // false,数据不是共享的
//        System.out.println(buffer.getByte(0) == copyBuffer.getByte(0));


    }
}
