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
import org.advancedproductivity.gable.framework.urils.GableFileUtils;
import org.advancedproductivity.gable.framework.urils.TestConfigGenerate;
import org.advancedproductivity.gable.web.service.MenuService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
            GableFileUtils.saveFile(menu.toPrettyString(), GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, UnitMenuFileName);
            return menu;
        }
        return (ArrayNode)node;
    }

    @Override
    public ArrayNode getPublicUnitMenus() {
        JsonNode node = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.UNIT, UnitMenuFileName);
        if (node == null) {
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
                if (StringUtils.equals(type, TestType.HTTP.name())) {
                    newUnit.put("memo", HttpMethodType.GET.name());
                }
                ArrayNode units = (ArrayNode) item.path("units");
                units.add(newUnit);
                // generate default uuid config
                ObjectNode newConfig = TestConfigGenerate.httpGenerate(HttpMethodType.GET, uuid, objectMapper);
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
    public void updateUserMenu(ArrayNode newMenu,String nameSpace) {
        GableFileUtils.saveFile(newMenu.toPrettyString(), GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, UnitMenuFileName);
    }
}
