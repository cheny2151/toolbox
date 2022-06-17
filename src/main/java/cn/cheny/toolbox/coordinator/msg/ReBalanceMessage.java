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

    /**
     * 已重平衡
     */
    public final static String TYPE_REBALANCED = "1";
    /**
     * 需要执行重平衡
     */
    public final static String TYPE_NEED_TO_REBALANCE = "2";

    private String type;
    private String sender;
    private String resourceKey;

    public ReBalanceMessage() {
    }

    public ReBalanceMessage(String resourceKey, String type, String sender) {
        this.resourceKey = resourceKey;
        this.type = type;
        this.sender = sender;
    }
}
