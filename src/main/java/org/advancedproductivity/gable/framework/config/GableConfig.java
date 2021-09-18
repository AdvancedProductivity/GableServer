package org.advancedproductivity.gable.framework.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

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
            checkNecesseryFile();
            FileUtils.write(file, config.toPrettyString(), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkNecesseryFile() {
        File persistenceFilePath = FileUtils.getFile(getGablePath() );
        if (!persistenceFilePath.exists()) {
            persistenceFilePath.mkdir();
        }
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
