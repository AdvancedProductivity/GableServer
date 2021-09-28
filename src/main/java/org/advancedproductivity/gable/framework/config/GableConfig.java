package org.advancedproductivity.gable.framework.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.service.MenuService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;

/**
 * @author zzq
 */
@Slf4j
public class GableConfig {
    private static ObjectNode config = null;
    public static final String PERSISTENCE = "operationDir";
    public static final String PUBLIC_PATH = "public";
    public static final String CONFIG_FILE_NAME = "config.json";

    public static void initConfig(){
        String jarPath = getConfigPath();
        final JsonNode configJsonNode = GableFileUtils.readFileAsJson(jarPath, CONFIG_FILE_NAME);
        if (configJsonNode == null || !configJsonNode.isObject()) {
            config = generateDefaultConfig(jarPath, new ObjectMapper());
            GableFileUtils.saveFile(config.toPrettyString(), jarPath, CONFIG_FILE_NAME);
        } else {
            config = (ObjectNode) configJsonNode;
        }
        checkNecessaryFile();
    }

    private static void checkNecessaryFile() {
        File persistenceFilePath = new File(getGablePath());
        if (!persistenceFilePath.exists()) {
            persistenceFilePath.mkdirs();
        }
        File publicPath = FileUtils.getFile(getGablePath(), PUBLIC_PATH, UserDataType.UNIT);
        if (!publicPath.exists()) {
            publicPath.mkdirs();
        }
        File publicUnitMenu = FileUtils.getFile(getGablePath(), PUBLIC_PATH, UserDataType.UNIT, MenuService.UnitMenuFileName);
        if (!publicUnitMenu.exists()) {
            GableFileUtils.saveFile("[]",getGablePath(), PUBLIC_PATH, UserDataType.UNIT, MenuService.UnitMenuFileName);
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
        GableFileUtils.saveFile(config.toPrettyString(), getConfigPath(), CONFIG_FILE_NAME);
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

    private static String getConfigPath(){
        return SystemUtils.getUserDir().getAbsolutePath();
    }
}
