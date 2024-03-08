package cn.cheny.toolbox.other.yaml;

import cn.cheny.toolbox.other.map.EasyMap;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class YamlProperties {

    public final static String PRE = "djl";

    /**
     * 模型配置
     */
    private List<Model> models = new ArrayList<>();

    @Data
    public static class Model {

        /**
         * 模型id
         */
        private String id;

        /**
         * 模型名称
         */
        private String name = "my_model";

        /**
         * 模型根目录
         */
        private String rootDir;

        /**
         * 模型配置名
         */
        private String modelConfigName = "model_config.yml";

        /**
         * 模型版本
         */
        private String version;

        /**
         * 是否开启文件监听进行模型更新
         */
        private Boolean fileWatchForReload = false;

        /**
         * 额外配置
         */
        private EasyMap additional;

        /**
         * 模型额外配置类
         */
        private Class<?> modelConfigClass;

        /**
         * 模型options
         */
        private Map<String, String> options;

        /**
         * model source is s3
         */
        private boolean s3Enable;

        /**
         * 默认模型
         */
        private boolean defaultModel;
    }

}
