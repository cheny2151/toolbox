package cn.cheny.toolbox.redis.clustertask;

import com.alibaba.fastjson.JSON;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 集群分页任务信息实体
 * v1.1.1 更新 {@link cn.cheny.toolbox.redis.clustertask.TaskInfo} 新增header任务信息头，方便拓展
 *
 * @author cheney
 * @date 2019-09-03
 */
public class TaskInfo extends HashMap<String, String> {

    public TaskInfo(String taskId, int dataNums, int StepCount, int StepSize, boolean desc, Map<String, Object> header) {
        super(8);
        setStepCount(StepCount);
        setDataNums(dataNums);
        setTaskId(taskId);
        setStepSize(StepSize);
        setDesc(desc);
        if (header != null) {
            setHeader(header);
        }
    }

    public TaskInfo(Map<String, String> params) {
        super(params);
    }

    /**
     * 总数居量
     */
    public Integer getDataNums() {
        return Integer.parseInt(get("dataNums"));
    }

    public void setDataNums(int dataNums) {
        put("dataNums", String.valueOf(dataNums));
    }

    /**
     * 步数
     */
    public Integer getStepCount() {
        return Integer.parseInt(get("stepCount"));
    }

    public void setStepCount(int StepCount) {
        put("stepCount", String.valueOf(StepCount));
    }

    /**
     * 任务ID
     */
    public String getTaskId() {
        return (String) get("taskId");
    }

    public void setTaskId(String taskId) {
        put("taskId", taskId);
    }

    /**
     * 步长
     */
    public Integer getStepSize() {
        return Integer.parseInt(get("stepSize"));
    }

    public void setStepSize(int StepSize) {
        put("stepSize", String.valueOf(StepSize));
    }

    /**
     * 是否倒序
     */
    public boolean isDesc() {
        return Boolean.parseBoolean(get("desc"));
    }

    public void setDesc(boolean StepSize) {
        put("desc", String.valueOf(StepSize));
    }

    /**
     * 头部信息
     */
    public void setHeader(Map<String, Object> header) {
        put("header", JSON.toJSONString(header));
    }

    public Map<String, Object> getHeader() {
        String header = get("header");
        return header != null ? JSON.parseObject(header) : Collections.emptyMap();
    }

    public boolean isValid() {
        return getTaskId() != null && getStepCount() != null
                && getStepSize() != null && getStepCount() != null;
    }
}
