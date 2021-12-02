package fun.qianfg.chapter3;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class NIOFileCopy2 {
    public static void main(String[] args) throws Exception {
        FileInputStream inputStream = new FileInputStream("file01.txt");
        FileChannel inputChannel = inputStream.getChannel();
        FileOutputStream outputStream = new FileOutputStream("file02.txt");
        FileChannel outputChannel = outputStream.getChannel();

        outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        inputChannel.close();
        outputChannel.close();
        inputStream.close();
        outputStream.close();
    }
}
