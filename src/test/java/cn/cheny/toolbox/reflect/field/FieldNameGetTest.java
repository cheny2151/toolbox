package cn.cheny.toolbox.reflect.field;

import cn.cheny.toolbox.dom.html.HtmlParse;
import cn.cheny.toolbox.reflect.ReflectUtils;
import org.junit.Test;

/**
 * @author by chenyi
 * @date 2022/6/17
 */
public class FieldNameGetTest {

    @Test
    public void test() {
        String fieldName = ReflectUtils.fieldName(HtmlParse.NodeAndText::getNode);
        System.out.println(fieldName);
    }

}
