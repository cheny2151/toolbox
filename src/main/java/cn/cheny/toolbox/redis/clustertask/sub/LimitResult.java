package cn.cheny.toolbox.redis.clustertask.sub;


import cn.cheny.toolbox.other.ResultAndFlag;
import cn.cheny.toolbox.other.page.Limit;

/**
 * 获取集群任务分页实体
 *
 * @author cheney
 * @date 2019-09-20
 */
class LimitResult extends ResultAndFlag<Limit> {

    /**
     * 是否是最后的分页
     */
    private boolean last;

    private LimitResult(Limit result, boolean flag, boolean last) {
        super(result, flag);
        this.last = last;
    }

    private LimitResult(Limit result, boolean flag) {
        super(result, flag);
    }

    /**
     * 完成信号
     */
    static LimitResult completed() {
        return new LimitResult(null, false);
    }

    /**
     * 分配新分页信号
     */
    static LimitResult newLimit(Limit result, boolean last) {
        return new LimitResult(result, true, last);
    }

    boolean isLast() {
        return last;
    }

}
