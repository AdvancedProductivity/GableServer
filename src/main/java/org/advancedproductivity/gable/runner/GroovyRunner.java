package org.advancedproductivity.gable.runner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author zzq
 */
public class GroovyRunner {
    private static final String GROOVY_PATH_NAME = "groovyDemoCode";
    private static final String GROOVY_PATH_CODE = "code";
    private static final String GROOVY_PATH_JSON = "configJson";
    private static final String RUNNER_PATH = SystemUtils.getUserDir().getAbsolutePath();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        String file = "MysqlDelete";
        try {
            JsonNode in = getInConfig(file);
            GroovyScriptEngine engine = new GroovyScriptEngine(
                    RUNNER_PATH + File.separator +
                            GROOVY_PATH_NAME + File.separator + GROOVY_PATH_CODE);
            Binding binding = new Binding();
            ObjectNode out = mapper.createObjectNode();
            binding.setVariable("in", in);
            binding.setVariable("out", out);
            System.out.println("groovy start running=====================>");
            Object response = engine.run(file + ".groovy", binding);
            System.out.println("response is =====================>");
            System.out.println("response: " + response);
            System.out.println("out is =====================>");
            System.out.println((out.toPrettyString()));
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static JsonNode getInConfig(String jsonFileName) throws IOException {
        File file = FileUtils.getFile(RUNNER_PATH, GROOVY_PATH_NAME, GROOVY_PATH_JSON, jsonFileName + ".json");
        return mapper.readTree(file);
    }
}
