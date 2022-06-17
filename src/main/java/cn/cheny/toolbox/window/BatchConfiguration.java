package cn.cheny.toolbox.window;

import cn.cheny.toolbox.reflect.ReflectUtils;
import cn.cheny.toolbox.spring.SpringUtils;
import cn.cheny.toolbox.window.output.BatchResultSplitter;
import org.apache.commons.lang3.StringUtils;

/**
 * 批处理配置
 *
 * @author by chenyi
 * @date 2022/1/24
 */
public class BatchConfiguration {

    private String group;

    private int batchArgIndex;

    private long winTime;

    private int threshold;

    private int threadPoolSize;

    private Class<? extends BatchResultSplitter> splitterClass;

    private String splitterName;

    private final BatchMethod batchMethod;

    private final CollectedMethod collectedMethod;

    public BatchConfiguration(BatchMethod batchMethod, CollectedMethod collectedMethod) {
        Batch batch = batchMethod.getBatch();
        this.group = batch.group();
        this.batchArgIndex = batch.batchArgIndex();
        this.winTime = batch.winTime();
        this.threshold = batch.threshold();
        this.threadPoolSize = batch.threadPoolSize();
        this.splitterClass = batch.splitter();
        this.splitterName = batch.splitterName();
        this.batchMethod = batchMethod;
        this.collectedMethod = collectedMethod;
    }

    public synchronized CollectedParams buildParams(Object[] args) {
        return new CollectedParams(args, this);
    }

    public String getGroup() {
        return group;
    }

    public int getBatchArgIndex() {
        return batchArgIndex;
    }

    public long getWinTime() {
        return winTime;
    }

    public int getThreshold() {
        return threshold;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public BatchMethod getBatchMethod() {
        return batchMethod;
    }

    public CollectedMethod getCollectedMethod() {
        return collectedMethod;
    }

    public BatchResultSplitter getBatchResultSplitter() {
        if (StringUtils.isNotEmpty(splitterName)) {
            return SpringUtils.getBean(splitterName, BatchResultSplitter.class);
        } else {
            return ReflectUtils.newObject(splitterClass, null, null);
        }
    }

    public CollectedStaticParams buildStaticParams(Object[] args, BatchConfiguration batchConfiguration) {
        return new CollectedStaticParams(args, batchConfiguration);
    }

}
