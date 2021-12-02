package fun.qianfg.demo.practice;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BioServer {
    public static void main(String[] args) throws IOException {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("server start...");
        while (true) {
            final Socket socket = serverSocket.accept();
            threadPool.execute(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    System.out.println("a client is connected");
                    while (true) {
                        InputStream inputStream = socket.getInputStream();
                        byte[] bytes = new byte[1024];
                        int read = inputStream.read(bytes);
                        if (read != -1) {
                            System.out.printf("receive from client(%s): %s", Thread.currentThread().getName(), new String(bytes, 0, read));
                        }
                    }
                }
            });
        }
    }
}
