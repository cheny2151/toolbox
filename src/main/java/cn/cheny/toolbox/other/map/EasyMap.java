package cn.cheny.toolbox.other.map;

import cn.cheny.toolbox.other.order.Orders;
import cn.cheny.toolbox.property.token.ParseTokenException;
import cn.cheny.toolbox.property.token.TokenParser;
import cn.cheny.toolbox.reflect.ReflectUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 简化map获取操作
 *
 * @Date 2021/2/8
 * @Created by chenyi
 */
public class EasyMap extends HashMap<String, Object> {

    public String getString(String key) {
        return null;
    }

    public Object getObject(String key) {
        TokenParser tokenParser = new TokenParser(key);
        Object cur = this;
        do {
            Class<?> curClass = cur.getClass();
            String property = tokenParser.getProperty();
            if (cur instanceof Map) {
                cur = ((Map<String, Object>) cur).get(property);
            } else if (cur instanceof Collection || curClass.isArray() || curClass.isPrimitive()) {
                throw new ParseTokenException("property " + property + "is " + curClass + " ,expect map or object");
            } else {
                cur = ReflectUtils.readValue(cur, property);
            }
            if (tokenParser.isCollection()) {
                for (Integer index : tokenParser.getCollectionIndexes()) {
                    if (cur instanceof Collection) {
                        cur = ((Collection<?>) cur).toArray()[index];
                    } else if (cur.getClass().isArray()) {
                        cur = ((Object[]) cur)[index];
                    } else {
                        throw new ParseTokenException("property " + property + "is " + cur.getClass() + " ,expect collection or array");
                    }
                }
            }
        } while ((tokenParser = tokenParser.next()) != null);
        return cur;
    }

    public enum Test {
        test1(1),
        test2(2);
        private int val;

        Test(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }
    }

    public static void main(String[] args) {
        EasyMap easyMap = new EasyMap();
        HashMap<String, Object> test = new HashMap<>();
        test.put("test1", Test.test1);
        easyMap.put("test", test);
        System.out.println(easyMap.getObject("test.test1"));
    }

}
