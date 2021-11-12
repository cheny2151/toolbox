package cn.cheny.toolbox.coordinator.msg;

import lombok.Data;

/**
 * 重平衡信息
 *
 * @author by chenyi
 * @date 2021/11/11
 */
@Data
public class ReBalanceMessage {

    public final static String TYPE_RE_BALANCE = "1";
    public final static String TYPE_REQUIRED_RE_BALANCE = "2";

    private String type;
    private String sender;

    public ReBalanceMessage() {
    }

    public ReBalanceMessage(String type, String sender) {
        this.type = type;
        this.sender = sender;
    }
}