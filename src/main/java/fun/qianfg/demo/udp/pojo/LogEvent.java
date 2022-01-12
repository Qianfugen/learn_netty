package fun.qianfg.demo.udp.pojo;

import java.net.InetSocketAddress;

public final class LogEvent {
    public static final byte SEPARATOR = (byte) ':';
    private final InetSocketAddress source; //发送LogEvent的源的InetSocketAddress
    private final long received;            //接受LogEvent的时间
    private final String logfile;           //发送的LogEvent的日志文件的名称
    private final String msg;               //消息内容

    public LogEvent(String logfile, String msg) {
        this(null, -1, logfile, msg);
    }

    public LogEvent(InetSocketAddress source, long received, String logfile, String msg) {
        this.source = source;
        this.logfile = logfile;
        this.msg = msg;
        this.received = received;
    }

    public InetSocketAddress getSource() {
        return source;
    }

    public long getReceivedTimestamp() {
        return received;
    }

    public String getLogfile() {
        return logfile;
    }

    public String getMsg() {
        return msg;
    }
}
