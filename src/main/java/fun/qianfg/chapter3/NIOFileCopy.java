package fun.qianfg.chapter3;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIOFileCopy {
    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("1.mp4");
        FileChannel inputChannel = fis.getChannel();
        FileOutputStream fos = new FileOutputStream("2.mp4");
        FileChannel outputChannel = fos.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        long startTime = System.currentTimeMillis();
        while (true) {
            buffer.clear();
            int read = inputChannel.read(buffer);
            if (read == -1) {
                break;
            }
            buffer.flip();
            outputChannel.write(buffer);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("take time: " + (endTime - startTime) + "ms");
        inputChannel.close();
        outputChannel.close();
        fis.close();
        fos.close();
    }
}
