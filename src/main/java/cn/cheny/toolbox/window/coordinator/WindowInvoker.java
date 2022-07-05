package cn.cheny.toolbox.window.coordinator;

import cn.cheny.toolbox.exception.WindowElementEmptyException;
import cn.cheny.toolbox.window.BatchConfiguration;
import cn.cheny.toolbox.window.BatchMethod;
import cn.cheny.toolbox.window.CollectedParams;
import cn.cheny.toolbox.window.WindowElement;
import cn.cheny.toolbox.window.output.BatchResultSplitter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 批处理窗口执行单元
 *
 * @author by chenyi
 * @date 2021/9/22
 */
@Slf4j
public class WindowInvoker {

    private final CollectedParams collectedParams;
    private final BatchMethod batchMethod;
    private final BatchResultSplitter splitter;
    private Object target;

    public WindowInvoker(CollectedParams collectedParams, BatchConfiguration batchConfiguration, Object target) {
        this.collectedParams = collectedParams;
        this.batchMethod = batchConfiguration.getBatchMethod();
        this.splitter = batchConfiguration.getBatchResultSplitter();
        this.target = target;
    }

    public WindowElement buildElement(Object[] args) throws WindowElementEmptyException {
        WindowElement element = collectedParams.buildElement(args);
        int size = element.size();
        if (size == 0) {
            throw new WindowElementEmptyException();
        }
        return element;
    }

    public void doWindow(List<WindowElement> batch) {
        List<Object> inputs = new ArrayList<>();
        batch.forEach(e -> e.collectInput(inputs));
        long l = System.currentTimeMillis();
        Object outputs;
        try {
            outputs = doBatch(inputs);
        } catch (Exception e) {
            batch.forEach(element -> element.setError(e));
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Do batch size:{}, use time:{}", inputs.size(), System.currentTimeMillis() - l);
        }
        for (int i = 0, index = 0; i < batch.size(); i++) {
            WindowElement element = batch.get(i);
            if (outputs == null) {
                element.setOutput(null);
            } else {
                try {
                    Object output = splitter.split(outputs, element, index);
                    element.setOutput(output);
                } catch (Exception e) {
                    element.setError(e);
                }
                index += element.size();
            }
        }
    }

    private Object doBatch(List<Object> inputs) throws Exception {
        Object[] args = collectedParams.buildArgs(inputs);
        return batchMethod.doBatch(target, args);
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

}
