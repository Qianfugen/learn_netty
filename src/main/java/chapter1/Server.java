package chapter1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    static final int port = 1234;

    public static void main(String[] args) throws IOException {
        System.out.println("Server Start...");
        new Server().init();
    }

    public void init() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            Socket socket = serverSocket.accept();
            new HandlerThread(socket);
        }

    }


    class HandlerThread implements Runnable {
        private Socket socket;

        public HandlerThread(Socket socket) {
            this.socket = socket;
            // 启动线程
            new Thread(this).start();
        }

        @Override
        public void run() {
            try {
                // 读取客户端发来的消息
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String reveiveMsg = in.readLine();
                System.out.println("客户端发来的消息是：" + reveiveMsg);

                // 向客户端回复消息
                PrintStream out = new PrintStream(socket.getOutputStream());
                System.out.println("请输入：");
                String sendMsg = new BufferedReader(new InputStreamReader(System.in)).readLine();
                out.println(sendMsg);

                // 关闭流
                out.close();
                in.close();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
