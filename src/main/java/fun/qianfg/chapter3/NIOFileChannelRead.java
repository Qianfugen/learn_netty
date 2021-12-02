package fun.qianfg.chapter3;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIOFileChannelRead {
    public static void main(String[] args) throws Exception {
        File file = new File("file01.txt");
        FileInputStream fis = new FileInputStream(file);
        FileChannel fileChannel = fis.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate((int) file.length());
        fileChannel.read(buffer);
        System.out.println(new String(buffer.array()));
        fileChannel.close();
        fis.close();
    }
}
