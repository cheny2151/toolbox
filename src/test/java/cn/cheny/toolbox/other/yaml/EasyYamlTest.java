package cn.cheny.toolbox.other.yaml;

import org.junit.Test;

import java.util.List;

/**
 * @author by chenyi
 * @Date 2021/5/28
 */
public class EasyYamlTest {

    @Test
    public void testYaml() {
        String config = "djl:\n" +
                "  models:\n" +
                "  - file-watch-for-reload: true\n" +
                "    id: lr\n" +
                "    model-config-class: cn.cheny.toolbox.other.yaml.YamlProperties\n" +
                "    root-dir: /Users/chenyi/Downloads/model_test/LR\n" +
                "  - default-model: true\n" +
                "    file-watch-for-reload: true\n" +
                "    id: esmm\n" +
                "    model-config-class: cn.cheny.toolbox.other.yaml.YamlProperties\n" +
                "    root-dir: /Users/chenyi/Downloads/model_test/esmm\n" +
                "  - default-model: true\n" +
                "    file-watch-for-reload: true\n" +
                "    id: esmm2\n" +
                "    model-config-class: cn.cheny.toolbox.other.yaml.YamlProperties\n" +
                "    additional:\n" +
                "      key: value\n" +
                "    root-dir: /Users/chenyi/Downloads/model_test/esmm\n";
        EasyYaml easyYaml = new EasyYaml(config);
        easyYaml.toObject(YamlProperties.class);
        YamlProperties newProperties = easyYaml.getObject(YamlProperties.PRE, YamlProperties.class);
        List<YamlProperties.Model> newModels = newProperties.getModels();
        newModels.forEach(System.out::println);
    }

}
