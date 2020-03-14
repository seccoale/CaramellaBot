package com.seccoale.caramellabot.config;

import org.yaml.snakeyaml.Yaml;

public class ConfigProvider {

    private static Config config = null;

    public static Config getConfig() {
        if(config == null){
            synchronized (ConfigProvider.class) {
                if(config == null) {
                    Yaml yaml = new Yaml();
                    config = yaml.load(ConfigProvider.class.getClassLoader().getResourceAsStream("properties.yml"));
                }
            }
        }
        return config;
    }
}
