package cn.cheny.toolbox.POIUtils.worker;

import cn.cheny.toolbox.POIUtils.entity.SaxReadInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * excel读取结果
 *
 * @author cheney
 * @date 2019-12-19
 */
public class RaxReadResult {

    // 读取结果数据
    private Map<Integer, Map<String, String>> writeBackData;

    // 标题行号(从0开始算)
    private final int titleRowNum;

    // 读取的标题总列数
    private int titleCount;

    // excel源
    private Object source;

    // excel读取信息
    private final SaxReadInfo saxReadInfo;

    public RaxReadResult(SaxReadInfo saxReadInfo) {
        this.titleRowNum = saxReadInfo.getTitleRow();
        this.saxReadInfo = saxReadInfo;
        this.writeBackData = new HashMap<>();
    }

    public Map<Integer, Map<String, String>> getWriteBackData() {
        return writeBackData;
    }

    public int getTitleRowNum() {
        return titleRowNum;
    }

    public int getTitleCount() {
        return titleCount;
    }

    public Object getSource() {
        return source;
    }

    public SaxReadInfo getSaxReadInfo() {
        return saxReadInfo;
    }

    protected void setTitleCount(int titleCount) {
        this.titleCount = titleCount;
    }

    protected void setSource(Object source) {
        this.source = source;
    }

    /**
     * 添加回写数据
     *
     * @param rowNum 回写行
     * @param data   回写数据
     */
    public void addWriteBackData(int rowNum, Map<String, String> data) {
        this.writeBackData.put(rowNum, data);
    }
}
