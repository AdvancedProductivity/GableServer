package org.advancedproductivity.gable.framework.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.web.entity.Result;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author zzq
 */
public class GableConfig {
    private static ObjectNode config = null;
    public static final String PERSISTENCE = "operationDir";

    public static void initConfig(){
        ObjectMapper mapper = new ObjectMapper();
        String jarPath = SystemUtils.getUserDir().getAbsolutePath();
        System.out.println("jar path is: " + jarPath);
        File file = FileUtils.getFile(jarPath, "config.json");
        try {
            if (!file.exists()) {
                config = generateDefaultConfig(jarPath, mapper);
            }else {
                config = (ObjectNode) mapper.readTree(file);
            }
            checkNecessaryFile();
            saveConfigToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveConfigToFile() {
        try {
            String jarPath = SystemUtils.getUserDir().getAbsolutePath();
            File file = FileUtils.getFile(jarPath, "config.json");
            FileUtils.write(file, config.toPrettyString(), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkNecessaryFile() {
        File persistenceFilePath = FileUtils.getFile(getGablePath() );
        if (!persistenceFilePath.exists()) {
            persistenceFilePath.mkdir();
        }
    }

    public static ObjectNode getConfig() {
        if (config == null) {
            return null;
        }
        return config.deepCopy();
    }

    public static void updateConfig(ObjectNode config) {
        GableConfig.config = config;
        checkNecessaryFile();
        saveConfigToFile();
    }

    public static String checkRequired(ObjectNode config) {
        if (!config.path(PERSISTENCE).isTextual()) {
            return PERSISTENCE;
        }
        return null;
    }

    private static ObjectNode generateDefaultConfig(String jarPath, ObjectMapper mapper) {
        ObjectNode config = mapper.createObjectNode();
        String persistenceFolderName = "Persistence";
        config.put(PERSISTENCE, jarPath + File.separator + persistenceFolderName);
        return config;
    }

    public static String getAsString(String key) {
        if (config == null) {
            return "";
        }
        return config.path(key).asText();
    }

    public static String getGablePath(){
        return getAsString(PERSISTENCE);
    }
}
