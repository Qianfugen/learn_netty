package fun.qianfg.demo.broadcast.Response;

import lombok.Data;

@Data
public class DeviceInformation {
    /*
     *设备类型
     */
    private Integer type;
    /*
    序列号长度
     */
    private Integer serialLength;
    /*
    序列号
     */
    private String deviceSerialNumber;
    /*
    设备名称/编号长度
     */
    private Integer numberLength;
    /*
    设备名称/编号
     */
    private String number;
    /*
    mac地址
     */
    private String macAddress;
    private String ipAddress;
    /*
    网关
     */
    private String gatewayAddress;
    /*
    子网掩码
     */
    private String addressMask;
    /*
    dns
     */
    private String dnsAddress;
    /*
    485主机Id
     */
    private Integer device485Id;
    /*
    指纹模块型号
     */
    private String fingerPrintModel;
    /*
    固件版本号
     */
    private String firmwareVersion;

}
