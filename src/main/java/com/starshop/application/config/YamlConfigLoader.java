package com.starshop.application.config;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手动 main 方法专用的 YML 配置加载器
 * 设计思路：
 *   1. flatten  → 把 application-dev.yml + application.yml 递归拍扁成一张 "a.b.c = value" 的扁平表
 *                  先拍 dev、再拍 main，这样 main 里有真实值（非占位符）的字段可以覆盖 dev 的同名项
 *   2. resolve  → 去扁平表里查占位符对应的真实值，支持字符串内多个 ${} 占位符 + 默认值语法
 *   3. fillConfig → 把解析完的真实值，填进 AppConfig 实体里
 */
public class YamlConfigLoader {

    private static final String MAIN_YML = "application.yml";

    private static final String DEV_YML = "application-dev.yml";

    /**
     * Spring 占位符正则
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^:}]+)(?::([^}]*))?\\}");

    /**
     * 入口方法：加载 yml + 解析占位符 + 填充实体
     */
    public static AppConfig loadConfig() {
        Yaml yaml = new Yaml();
        AppConfig appConfig = new AppConfig();
        // LinkedHashMap 保持插入顺序，对查找性能没影响但更易读
        Map<String, String> flatConfig = new LinkedHashMap<>();

        // 先拍 dev，再拍 main → main 里的非占位符值会覆盖 dev 的同名项（虽然 main 基本不定义 starShop.* 层级）
        flatten(loadYaml(yaml, DEV_YML), "", flatConfig);
        flatten(loadYaml(yaml, MAIN_YML), "", flatConfig);

        fillConfig(appConfig, flatConfig);
        return appConfig;
    }

    /**
     * 从 classpath 加载 yml 文件，返回 SnakeYAML 解析后的 Map 结构
     */
    private static Map<String, Object> loadYaml(Yaml yaml, String name) {
        InputStream is = YamlConfigLoader.class.getClassLoader().getResourceAsStream(name);
        if (is == null) return null;
        return yaml.load(is);
    }

    /**
     * 递归把嵌套的 YAML Map 拍扁成扁平结构
     */
    @SuppressWarnings("unchecked")
    private static void flatten(Map<String, Object> map, String prefix, Map<String, String> out) {
        if (map == null) return;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            // 非根节点拼上 "父." 前缀
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                // 还有下一层，继续递归
                flatten((Map<String, Object>) value, key, out);
            } else {
                // 叶子节点 → 放入扁平表
                out.put(key, getStr(value));
            }
        }
    }

    /**
     * 从扁平表里取 Spring 标准 key，解析后填入 AppConfig 实体
     */
    private static void fillConfig(AppConfig config, Map<String, String> flat) {
        AppConfig.DbConfig db = config.getDatasource();
        db.setDriverClassName(resolve(flat.get("spring.datasource.driver-class-name"), flat));
        db.setUrl(resolve(flat.get("spring.datasource.url"), flat));
        db.setUsername(resolve(flat.get("spring.datasource.username"), flat));
        db.setPassword(resolve(flat.get("spring.datasource.password"), flat));

        AppConfig.EsConfig es = config.getElasticsearch();
        es.setUris(resolve(flat.get("spring.elasticsearch.uris"), flat));
    }

    /**
     * 占位符解析器
     *
     * 支持两种 Spring 语法：
     *   ${key}         → 去 flat 表里查 "key"，找不到就原样保留
     *   ${key:default} → 去 flat 表里查 "key"，找不到就用 "default"
     *
     * 同时支持**一个字符串里有多个占位符**
     */
    private static String resolve(String value, Map<String, String> flat) {
        if (value == null) return null;
        StringBuffer sb = new StringBuffer();
        Matcher m = PLACEHOLDER_PATTERN.matcher(value);
        while (m.find()) {
            String key = m.group(1);          // 占位符里的配置 key，如 "starShop.datasource.host"
            String defaultValue = m.group(2);  // 默认值（: 后面的部分），可能为 null
            String replacement = flat.get(key);
            if (replacement == null) {
                // 找不到配置值 → 用默认值；没有默认值就原样保留占位符（方便排查）
                replacement = defaultValue != null ? defaultValue : m.group(0);
            }
            // quoteReplacement 防止 replacement 里有 $ 或 \ 导致 appendReplacement 解析异常
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 安全地把 Object 转成 String
     */
    private static String getStr(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }
}