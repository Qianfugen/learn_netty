package fun.qianfg.chapter4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

public class PlainOioServer {
    public static void server(int port) throws Exception {
        final ServerSocket serverSocket = new ServerSocket(port);
        try {
            while (true) {
                final Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);

                new Thread(new Runnable() {
                    public void run() {
                        OutputStream out = null;
                        try {
                            // 读取客户端发来的消息
                            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            String reveiveMsg = in.readLine();
                            System.out.println("客户端发来的消息是：" + reveiveMsg);

                            // 回传消息给客户端
                            out = clientSocket.getOutputStream();
                            out.write("hello".getBytes(Charset.forName("UTF-8")));
                            out.flush();
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                clientSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        server(1234);
    }
}
