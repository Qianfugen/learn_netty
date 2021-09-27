package demo.weatherStationAdapter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Arrays;

/**
 * 服务端处理器
 */
@Sharable
public class WeatherHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String recMsg = ByteBufUtil.hexDump((ByteBuf) msg);
        System.out.print("接收的数据：");
        printHex(recMsg);
        ByteBuf buf = Unpooled.buffer();

        /**
         * 方式一：直接按照协议写死，只有一种天气信息
         */
//        int[] data = new int[]{0x01, 0x03, 0x20, 0x00, 0xF5, 0x00, 0x7D, 0x01, 0x32, 0x00, 0x14, 0x02, 0x58, 0x7F, 0xFF, 0x7F, 0xFF, 0x7F, 0xFF, 0x7F, 0xFF, 0x7F, 0xFF, 0x7F, 0xFF, 0x7F, 0xFF, 0x7F, 0xFF, 0x7F, 0xFF, 0x7F, 0xFF, 0x7F, 0xFF, 0x13, 0x69};

        /**
         * 方式二：
         * 按照协议生成随机气象的相应指令，天气信息随机
         */
        int[] data = generateCmd();
        System.out.println("data数据：" + Arrays.toString(data));

        for (int i = 0; i < data.length; i++) {
            buf.writeByte(data[i]);
        }

        String sendMsg = ByteBufUtil.hexDump(buf);
        System.out.print("发送的数据：");
        printHex(sendMsg);
        ctx.writeAndFlush(buf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    private void printHex(String s) {
        for (int i = 0; i < s.length(); i++) {
            System.out.print(s.charAt(i));
            if (i % 2 == 1) {
                System.out.print(" ");
            }
        }
        System.out.println();
    }

    private int[] generateCmd() {
        // 不包含校验码的数组，头部3个字节+气象信息的32字节，共35字节
        byte[] data = new byte[35];
        data[0] = 0x01;
        data[1] = 0x03;
        data[2] = 0x20;
        int[] info = generateData();
        for (int i = 0; i < info.length; i++) {
            data[3 + i] = (byte) info[i];
        }
        String checkSumStr = getCRC(data);
        int[] checkSum = transferData(Integer.parseInt(checkSumStr, 16));

        int[] rlt = new int[37];
        for (int i = 0; i < data.length; i++) {
            rlt[i] = data[i];
        }
        rlt[35] = checkSum[0];
        rlt[36] = checkSum[1];

        return rlt;
    }

    /**
     * 自动生成气象信息
     *
     * @return
     */
    private int[] generateData() {
        int[] data = new int[32];
        // 只是用了5个通道，其余通道数据同意设置为 0x00,0x00
        int temperature = generateRandomNum(0, 400);  // 气温
        int rainfall = generateRandomNum(0, 1000);   // 降雨量
        int humidity = generateRandomNum(0, 1000);   // 湿度
        int windRate = generateRandomNum(0, 130);    // 风力等级
        int windDirection = generateRandomNum(0, 3600);   // 风向

        data[0] = transferData(temperature)[0];
        data[1] = transferData(temperature)[1];
        data[2] = transferData(rainfall)[0];
        data[3] = transferData(rainfall)[1];
        data[4] = transferData(humidity)[0];
        data[5] = transferData(humidity)[1];
        data[6] = transferData(windRate)[0];
        data[7] = transferData(windRate)[1];
        data[8] = transferData(windDirection)[0];
        data[9] = transferData(windDirection)[1];

        return data;
    }

    /**
     * 生产指定范围随机数
     * 取值范围 [min,max)
     *
     * @param min
     * @param max
     * @return
     */
    public int generateRandomNum(int min, int max) {
        return min + (int) (Math.random() * (max - min));
    }

    /**
     * 将一个气象元素转成2位数组表示
     *
     * @param info
     * @return
     */
    public int[] transferData(int info) {
        int[] data = new int[2];
        String hexStr = Integer.toHexString(info);
        int length = hexStr.length();
        String hex1 = "0";
        String hex2 = "0";
        if (length <= 2) {
            hex2 = hexStr;
        } else if (length == 3) {
            hex1 = hexStr.substring(0, 1);
            hex2 = hexStr.substring(1, 3);
        } else {
            hex1 = hexStr.substring(0, 2);
            hex2 = hexStr.substring(2, 4);
        }
        data[0] = Integer.parseInt(hex1, 16);
        data[1] = Integer.parseInt(hex2, 16);

        return data;
    }

    /**
     * 计算CRC16校验码
     *
     * @param bytes
     * @return
     */
    public static String getCRC(byte[] bytes) {
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;

        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        //高低位互换，输出符合相关工具对Modbus CRC16的运算
        CRC = ((CRC & 0xFF00) >> 8) | ((CRC & 0x00FF) << 8);
        return Integer.toHexString(CRC);
    }
}
