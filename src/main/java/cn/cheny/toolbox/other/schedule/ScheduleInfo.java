package cn.cheny.toolbox.other.schedule;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 进度信息实体
 *
 * @author cheney
 * @date 2020-08-06
 */
@Data
public class ScheduleInfo {

    /**
     * 进度条监听者
     */
    private Consumer<ScheduleInfo> scheduleListener;

    /**
     * 总进度数
     */
    private int totalStep;

    /**
     * 当前进度
     */
    private int currentStep;

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 是否完成
     */
    private boolean finished;

    /**
     * 其他信息
     */
    private Map<String, Object> otherInfo;

    private ScheduleInfo(int totalStep, Map<String, Object> otherInfo, Consumer<ScheduleInfo> scheduleListener) {
        this.totalStep = totalStep;
        this.otherInfo = otherInfo;
        this.currentStep = 0;
        this.finished = false;
        this.scheduleListener = scheduleListener;
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    /**
     * 更新进度，返回剩余进度
     *
     * @param step      更新进度数
     * @param otherInfo 其他信息
     * @return 剩余进度数
     */
    public int updateStep(int step, Map<String, Object> otherInfo) {
        if (startTime == null) {
            this.start();
        }
        this.otherInfo = otherInfo;
        int newStep = currentStep + step;
        int lave = totalStep - newStep;
        if (lave <= 0) {
            currentStep = totalStep;
            lave = 0;
        } else {
            currentStep = newStep;
        }
        afterUpdate();
        return lave;
    }

    /**
     * 增加总进度，已完成进度将根据新增进度按原比例放大，并保证剩余进度为原剩余进度数+新增进度数
     *
     * @param step 增加的进度数
     * @return
     */
    public int addTotalStep(int step) {
        BigDecimal competeRate = getCompleteRate();
        BigDecimal notCompleteRate = BigDecimal.ONE.subtract(competeRate);
        int notCompleteStep = step + (totalStep - currentStep);
        this.totalStep = new BigDecimal(notCompleteStep).divide(notCompleteRate, 0, RoundingMode.HALF_UP).intValue();
        return this.currentStep = this.totalStep - notCompleteStep;
    }

    /**
     * 设置进度完成
     *
     * @param otherInfo 其他信息
     */
    public void finish(Map<String, Object> otherInfo) {
        this.otherInfo = otherInfo;
        this.finished = true;
        this.currentStep = this.totalStep;
        afterUpdate();
    }

    public BigDecimal getCompleteRate() {
        return new BigDecimal(currentStep).divide(new BigDecimal(totalStep), 2, RoundingMode.HALF_UP);
    }

    private void afterUpdate() {
        if (scheduleListener != null) {
            scheduleListener.accept(this);
        }
    }

}
