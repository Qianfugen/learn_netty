package fun.qianfg.chapter3;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIOFileChannelWrite {
    public static void main(String[] args) throws Exception {
        String s = "I am qianfg!";
        FileOutputStream fos = new FileOutputStream("file01.txt");
        FileChannel fileChannel = fos.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(s.getBytes());
        buffer.flip();
        fileChannel.write(buffer);
        fileChannel.close();
        fos.close();
    }
}
