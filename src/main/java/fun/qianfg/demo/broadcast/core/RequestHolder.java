package fun.qianfg.demo.broadcast.core;


import fun.qianfg.demo.broadcast.Response.ResponseMessage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于数据存储
 */
@SuppressWarnings("ALL")
public class RequestHolder {
    /*
    请求流水号生成，使用自增
     */
    public static final AtomicInteger Request_ID = new AtomicInteger();
    /*
    适用于RPC请求
     */
    public static final ConcurrentHashMap<Integer, ResponseFuture<ResponseMessage>> REQUEST_MAP = new ConcurrentHashMap();
    /*
    适用于广播请求
     */
    public static ResponseFuture<ResponseMessage> udpFuture;
}
