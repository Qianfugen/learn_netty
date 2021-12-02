package fun.qianfg.chapter1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Client {
    static final int port = 1234;
    static final String host = "localhost";

    public static void main(String[] args) throws Exception {
        System.out.println("Client Start...");
        Socket socket = null;

        while (true) {
            socket = new Socket(host, port);

            //向服务器发送消息
            PrintStream out = new PrintStream(socket.getOutputStream());
            System.out.println("请输入：");
            String sendMsg = new BufferedReader(new InputStreamReader(System.in)).readLine();
            out.println(sendMsg);

            // 接收服务器发来的消息
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String receiveMsg = in.readLine();
            System.out.println("服务器回传的消息：" + receiveMsg);

            // 关闭流
            out.close();
            in.close();

            //如果接收道“OK”，则断开连接
            if ("OK".equals(receiveMsg)) {
                break;
            }
        }

        if (socket != null) {
            System.out.println("Client Shutdown...");
            socket.close();
        }
    }
}
