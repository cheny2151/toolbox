package cn.cheny.toolbox.other;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author by chenyi
 * @date 2021/11/24
 */
@Data
public class IpAndPort {

    private String ip;

    private int port;

    public IpAndPort(String ip, Integer port) {
        if (StringUtils.isEmpty(ip) || port == null) {
            throw new IllegalArgumentException("ip or port can not be null,ip:" + ip + ",port:" + port);
        }
        this.ip = ip;
        this.port = port;
    }
}
