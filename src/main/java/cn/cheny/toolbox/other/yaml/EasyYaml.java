package cn.cheny.toolbox.other.yaml;

import cn.cheny.toolbox.other.map.EasyMap;
import cn.cheny.toolbox.reflect.TypeUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * 简化yaml转bean操作
 *
 * @author by chenyi
 * @date 2024/3/8
 */
public class EasyYaml {

    private final EasyMap yamlData;

    private final static BiFunction<Map<String, Object>, String, Object> GET_FIELD_VALUE_IN_YAML;

    static {
        GET_FIELD_VALUE_IN_YAML = (map, key) -> {
            Object val = map.get(key);
            if (val != null) {
                return val;
            }
            StringBuilder keyBuilder = new StringBuilder();
            for (char c : key.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    keyBuilder.append("-").append(Character.toLowerCase(c));
                } else {
                    keyBuilder.append(c);
                }
            }
            return map.get(keyBuilder.toString());
        };
    }

    public EasyYaml(String yaml) {
        this.yamlData = new Yaml().loadAs(yaml, EasyMap.class);
    }

    public <T> T toObject(Class<T> type) {
        return TypeUtils.mapToObject(yamlData, type, GET_FIELD_VALUE_IN_YAML);
    }

    public <T> T getObject(String key, Class<T> type) {
        EasyMap value = this.yamlData.getMap(key);
        return TypeUtils.mapToObject(value, type, GET_FIELD_VALUE_IN_YAML);
    }

    public EasyMap getYamlData() {
        return yamlData;
    }
}
