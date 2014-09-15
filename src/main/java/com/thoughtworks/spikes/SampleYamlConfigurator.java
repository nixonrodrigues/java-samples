package com.thoughtworks.spikes;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class SampleYamlConfigurator {

    public static void main(String[] args) {
        SampleYamlConfigurator sampleYamlConfigurator = new SampleYamlConfigurator();
        sampleYamlConfigurator.load();
    }

    private void load() {
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.yaml");
        Yaml yaml = new Yaml();
        Map<String, Object> configuration = (Map<String, Object>) yaml.load(resourceAsStream);
        for (String key : configuration.keySet()) {
            System.out.println(key);
        }
    }
}
