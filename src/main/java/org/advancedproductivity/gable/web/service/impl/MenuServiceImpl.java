package org.advancedproductivity.gable.web.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.ConfigField;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.core.HttpMethodType;
import org.advancedproductivity.gable.framework.core.TestType;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.framework.utils.TestConfigGenerate;
import org.advancedproductivity.gable.web.service.MenuService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

/**
 * @author zzq
 */
@Slf4j
@Service
public class MenuServiceImpl implements MenuService {
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public ArrayNode getUserUnitMenus(String nameSpace) {
        JsonNode node = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, UnitMenuFileName);
        if (node == null) {
            ArrayNode menu = objectMapper.createArrayNode();
            String uuid = UUID.randomUUID().toString();
            TestConfigGenerate.httpGenerate(HttpMethodType.GET, uuid, objectMapper);
            ObjectNode groups = addGroup("Demo Group");
            menu.add(groups);
            addUnit(menu, "Demo Http Test", groups.path("uuid").asText(), TestType.HTTP.name(), nameSpace);
            GableFileUtils.saveFile(menu.toPrettyString(), GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, UnitMenuFileName);
            return menu;
        }
        return (ArrayNode)node;
    }

    @Override
    public ArrayNode getPublicUnitMenus() {
        JsonNode node = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.UNIT, UnitMenuFileName);
        if (node == null) {
            ArrayNode menu = objectMapper.createArrayNode();
            GableFileUtils.saveFile(menu.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.UNIT, UnitMenuFileName);
        }
        return (ArrayNode)node;
    }

    @Override
    public ObjectNode addGroup(String groupName) {
        ObjectNode group = objectMapper.createObjectNode();
        group.put("uuid", UUID.randomUUID().toString());
        group.put("groupName", groupName);
        group.put("icon", "function");
        group.set("units", group.arrayNode());
        return group;
    }

    @Override
    public String addUnit(ArrayNode userUnitMenus, String unitName, String groupUuid, String type, String nameSpace) {
        for (JsonNode item : userUnitMenus) {
            if (StringUtils.equals(item.path("uuid").asText(), groupUuid)) {
                String uuid = UUID.randomUUID().toString();
                ObjectNode newUnit = objectMapper.createObjectNode();
                newUnit.put("uuid", uuid);
                newUnit.put("unitName", unitName);
                newUnit.put("type", type);
                ObjectNode newConfig = null;
                if (StringUtils.equals(type, TestType.HTTP.name())) {
                    newUnit.put("memo", HttpMethodType.GET.name());
                    newConfig = TestConfigGenerate.httpGenerate(HttpMethodType.GET, uuid, objectMapper);
                } else if (StringUtils.equals(type, TestType.GROOVY_SCRIPT.name())) {
                    newConfig = TestConfigGenerate.groovyGenerate(objectMapper, uuid);
                    newUnit.put("code", "");
                } else {
                    continue;
                }
                ArrayNode units = (ArrayNode) item.path("units");
                units.add(newUnit);
                // generate default uuid config
                GableFileUtils.saveFile(newConfig.toPrettyString(),
                        GableConfig.getGablePath(),
                        nameSpace,
                        UserDataType.UNIT,
                        uuid,
                        ConfigField.CONFIG_DEFINE_FILE_NAME);
                return uuid;
            }
        }
        return null;
    }

    @Override
    public String pushUnit(ArrayNode userUnitMenus, String unitName, String groupUuid, JsonNode config, String nameSpace, String originUuid) {
        for (JsonNode item : userUnitMenus) {
            if (StringUtils.equals(item.path("uuid").asText(), groupUuid)) {
                String uuid = "public_" + UUID.randomUUID().toString();
                log.info("push to new Uuid: {}", uuid);
                ((ObjectNode) config).put("uuid", uuid);
                ObjectNode newUnit = objectMapper.createObjectNode();
                newUnit.put("uuid", uuid);
                newUnit.put("unitName", unitName);
                String type = config.path("type").asText();
                newUnit.put("type", type);
                if (StringUtils.equals(TestType.GROOVY_SCRIPT.name(), type)) {
                    File file = FileUtils.getFile(GableConfig.getGablePath(), nameSpace, UserDataType.GROOVY, originUuid + ".groovy");
                    if (file.exists()) {
                        try {
                            File destFile = FileUtils.getFile(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH,
                                    UserDataType.GROOVY, uuid + ".groovy");
                            FileOutputStream fileOutputStream = FileUtils.openOutputStream(destFile);
                            FileUtils.copyFile(file, fileOutputStream);
                            fileOutputStream.close();
                        } catch (Exception e) {
                            log.error("write file error", e);
                        }
                    }
                }
                ArrayNode units = (ArrayNode) item.path("units");
                units.add(newUnit);
                // generate default uuid config
                GableFileUtils.saveFile(config.toPrettyString(),
                        GableConfig.getGablePath(),
                        GableConfig.PUBLIC_PATH,
                        UserDataType.UNIT,
                        uuid,
                        ConfigField.CONFIG_DEFINE_FILE_NAME);
                ((ObjectNode) config).put("uuid", originUuid).put("from", uuid);
                GableFileUtils.saveFile(config.toPrettyString(),
                        GableConfig.getGablePath(),
                        nameSpace,
                        UserDataType.UNIT,
                        originUuid,
                        ConfigField.CONFIG_DEFINE_FILE_NAME);
                GableFileUtils.saveFile(userUnitMenus.toPrettyString(),
                        GableConfig.getGablePath(),
                        GableConfig.PUBLIC_PATH,
                        UserDataType.UNIT,
                        UnitMenuFileName);
                return uuid;
            }
        }
        return null;
    }

    @Override
    public String cloneUnit(ArrayNode userUnitMenus, String unitName, String groupUuid, JsonNode config, String nameSpace, String originUuid) {
        for (JsonNode item : userUnitMenus) {
            if (StringUtils.equals(item.path("uuid").asText(), groupUuid)) {
                String uuid = UUID.randomUUID().toString();
                log.info("clone to new Uuid: {}", uuid);
                ((ObjectNode) config).put("uuid", uuid).put("from", originUuid);;
                ObjectNode newUnit = objectMapper.createObjectNode();
                newUnit.put("uuid", uuid);
                newUnit.put("unitName", unitName);
                String type = config.path("type").asText();
                newUnit.put("type", type);
                if (StringUtils.equals(TestType.GROOVY_SCRIPT.name(), type)) {
                    File file = FileUtils.getFile(GableConfig.getGablePath(),
                            GableConfig.PUBLIC_PATH, UserDataType.GROOVY, originUuid + ".groovy");
                    if (file.exists()) {
                        try {
                            File destFile = FileUtils.getFile(GableConfig.getGablePath(), nameSpace,
                                    UserDataType.GROOVY, uuid + ".groovy");
                            FileOutputStream fileOutputStream = FileUtils.openOutputStream(destFile);
                            FileUtils.copyFile(file, fileOutputStream);
                            fileOutputStream.close();
                        } catch (Exception e) {
                            log.error("write file error", e);
                        }
                    }
                }
                ArrayNode units = (ArrayNode) item.path("units");
                units.add(newUnit);
                // generate default uuid config
                GableFileUtils.saveFile(config.toPrettyString(),
                        GableConfig.getGablePath(),
                        nameSpace,
                        UserDataType.UNIT,
                        uuid,
                        ConfigField.CONFIG_DEFINE_FILE_NAME);
                GableFileUtils.saveFile(userUnitMenus.toPrettyString(),
                        GableConfig.getGablePath(),
                        nameSpace,
                        UserDataType.UNIT,
                        UnitMenuFileName);
                return uuid;
            }
        }
        return null;
    }

    @Override
    public void updateUserMenu(ArrayNode newMenu,String nameSpace) {
        GableFileUtils.saveFile(newMenu.toPrettyString(), GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, UnitMenuFileName);
    }

    @Override
    public void sync(String from, String to, String userId) {
        JsonNode newConfig = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), userId, UserDataType.UNIT,
                from,
                ConfigField.CONFIG_DEFINE_FILE_NAME);
        JsonNode originConfig = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.UNIT,
                to,
                ConfigField.CONFIG_DEFINE_FILE_NAME);
        if (newConfig == null || !newConfig.isObject()) {
            log.info("from config file not find");
        } else if (originConfig == null || !originConfig.isObject()) {
            log.info("to config file not find");
        } else {
            try {
                ObjectNode ori =  ((ObjectNode) originConfig);
                ori.put(ConfigField.VERSION, ori.path(ConfigField.VERSION).asInt() + 1);
                ori.set(ConfigField.DETAIL, ((ObjectNode) newConfig).path(ConfigField.DETAIL));
                GableFileUtils.saveFile(originConfig.toPrettyString(), GableConfig.getGablePath(),
                        GableConfig.PUBLIC_PATH,
                        UserDataType.UNIT,
                        to,
                        ConfigField.CONFIG_DEFINE_FILE_NAME);
            } catch (Exception e) {
                log.error("update file error", e);
            }
        }
        File fromGroovyFile = FileUtils.getFile(GableConfig.getGablePath(), userId, UserDataType.GROOVY,
                from + ".groovy");
        File toGroovyFile = FileUtils.getFile(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.GROOVY,
                to + ".groovy");

        if (!fromGroovyFile.exists()) {
            log.info("from groovy file not find");
        } else if (!fromGroovyFile.exists()) {
            log.info("to groovy file not find");
        } else {
            try {
                FileUtils.copyFile(fromGroovyFile, toGroovyFile);
            } catch (Exception e) {
                log.error("update file error", e);
            }
        }
    }

    @Override
    public void deleteUnitTest(ArrayNode userUnitMenus, String uuid, String nameSpace) {
        for (JsonNode userUnitMenu : userUnitMenus) {
            JsonNode units = userUnitMenu.path("units");
            ArrayNode newArray = objectMapper.createArrayNode();
            for (JsonNode unit : units) {
                if (StringUtils.equals(uuid, unit.path("uuid").asText())) {
                    continue;
                }
                newArray.add(unit);
            }
            File file = FileUtils.getFile(GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, uuid);
            if (file.exists()) {
                FileUtils.deleteQuietly(file);
            }
            ((ObjectNode)userUnitMenu).set("units", newArray);
        }
    }
}
