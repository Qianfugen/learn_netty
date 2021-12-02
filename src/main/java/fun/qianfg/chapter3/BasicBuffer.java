package fun.qianfg.chapter3;

import java.nio.IntBuffer;

public class BasicBuffer {
    public static void main(String[] args) {
        IntBuffer buffer = IntBuffer.allocate(5);
        for (int i = 0; i < buffer.capacity(); i++) {
            buffer.put(i * 2);
        }
        //读写切换
        buffer.flip();

        while (buffer.hasRemaining()) {
            System.out.println(buffer.get());
        }
    }
}
