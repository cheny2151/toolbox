package cn.cheny.toolbox.window;

import cn.cheny.toolbox.reflect.ReflectUtils;
import cn.cheny.toolbox.spring.SpringUtils;
import cn.cheny.toolbox.window.output.BatchResultSplitter;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

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

    private boolean argIsCollection;

    private Class<? extends BatchResultSplitter> splitterClass;

    private String splitterName;

    private BatchMethod batchMethod;

    public BatchConfiguration(Batch batch, Method batchMethod, Collected collected) {
        this.group = batch.group();
        this.batchArgIndex = batch.batchArgIndex();
        this.winTime = batch.winTime();
        this.threshold = batch.threshold();
        this.threadPoolSize = batch.threadPoolSize();
        this.splitterClass = batch.splitter();
        this.splitterName = batch.splitterName();
        if (collected != null) {
            this.argIsCollection = collected.argIsCollection();
        } else {
            this.argIsCollection = true;
        }
        this.batchMethod = new BatchMethod(batchMethod);
    }

    public Params buildParams(Object[] args) {
        return new Params(args, this);
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

    public boolean isArgIsCollection() {
        return argIsCollection;
    }

    public BatchMethod getBatchMethod() {
        return batchMethod;
    }

    public BatchResultSplitter getBatchResultSplitter() {
        if (StringUtils.isNotEmpty(splitterName)) {
            return SpringUtils.getBean(splitterName, BatchResultSplitter.class);
        } else {
            return ReflectUtils.newObject(splitterClass, null, null);
        }
    }
}
