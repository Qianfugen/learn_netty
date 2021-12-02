package fun.qianfg.chapter1;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BIOServer {
    public static void main(String[] args) throws Exception {
        ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("server start...");
        while (true) {
            System.out.println(String.format("serverThreadId = %s, serverThreadName = %s", Thread.currentThread().getId(), Thread.currentThread().getName()));
            System.out.println("waitting for connecting...");
            final Socket socket = serverSocket.accept();
            System.out.println("a client is connected");
            newCachedThreadPool.execute(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    handler(socket);
                }
            });
        }
    }

    public static void handler(Socket socket) throws IOException {
        byte[] bytes = new byte[1024];
        InputStream inputStream = socket.getInputStream();
        while (true) {
            System.out.println(String.format("clientThreadId = %s, clientThreadName = %s", Thread.currentThread().getId(), Thread.currentThread().getName()));
            System.out.println("reading...");
            int read = inputStream.read(bytes);
            if (read != -1) {
                System.out.print(new String(bytes, 0, read));
            } else {
                break;
            }
        }
    }
}
