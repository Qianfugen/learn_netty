package chapter5;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIOFileChannel03 {
    public static void main(String[] args) throws IOException {
        File srcFile = new File(".\\src\\main\\resources\\1.txt");
        FileInputStream fileInputStream = new FileInputStream(srcFile);
        FileChannel srcFileChannel = fileInputStream.getChannel();

        File dstFile = new File(".\\src\\main\\resources\\2.txt");
        FileOutputStream fileOutputStream = new FileOutputStream(dstFile);
        FileChannel dstFileChannel = fileOutputStream.getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(512);

        while (true) {
            // 重置byteBuffer
            byteBuffer.clear();
            int read = srcFileChannel.read(byteBuffer);
            if (read == -1) {
                break;
            }
            byteBuffer.flip();
            dstFileChannel.write(byteBuffer);
        }

        fileInputStream.close();
        fileOutputStream.close();

    }
}
