package cn.cheny.toolbox.scan.filter;

import cn.cheny.toolbox.scan.asm.AnnotationDesc;
import cn.cheny.toolbox.scan.asm.MethodDesc;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author by chenyi
 * @date 2021/12/27
 */
@Data
public class FilterResult {
    private boolean pass;
    private Map<MethodDesc, List<AnnotationDesc>> annotationDescMap;

    public FilterResult(boolean pass) {
        this.pass = pass;
    }

}
