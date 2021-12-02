package fun.qianfg.chapter5;

import java.io.*;
import java.nio.channels.FileChannel;

public class NIOFileChannel04 {
    public static void main(String[] args) throws IOException {
        File srcFile = new File(".\\src\\main\\resources\\a1.jpg");
        FileInputStream fileInputStream = new FileInputStream(srcFile);
        FileChannel srcFileChannel = fileInputStream.getChannel();

        File dstFile = new File(".\\src\\main\\resources\\a2.jpg");
        FileOutputStream fileOutputStream = new FileOutputStream(dstFile);
        FileChannel dstFileChannel = fileOutputStream.getChannel();

        dstFileChannel.transferFrom(srcFileChannel, 0, srcFileChannel.size());

        srcFileChannel.close();
        dstFileChannel.close();
        fileInputStream.close();
        fileOutputStream.close();
    }
}
