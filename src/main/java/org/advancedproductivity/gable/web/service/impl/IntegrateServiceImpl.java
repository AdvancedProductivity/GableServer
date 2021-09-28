package org.advancedproductivity.gable.web.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.service.IntegrateService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @author zzq
 */
@Service
@Slf4j
public class IntegrateServiceImpl implements IntegrateService {
    private static final String INTEGRATE_TEST_FILE = "integrate.json";

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public ArrayNode list() {
        JsonNode node = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, INTEGRATE_TEST_FILE);
        if (node == null) {
            node = objectMapper.createArrayNode();
            GableFileUtils.saveFile(node.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, INTEGRATE_TEST_FILE);
        }
        return (ArrayNode) node;
    }

    @Override
    public String addIntegrate(ArrayNode records, String name) {
        records.toPrettyString();
        String uuid = UUID.randomUUID().toString();
        GableFileUtils.saveFile(records.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.INTEGRATE, uuid, "define.json");
        JsonNode node = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, INTEGRATE_TEST_FILE);
        ObjectNode integrateItem = objectMapper.createObjectNode();
        integrateItem.put("uuid", uuid);
        integrateItem.put("name", name);
        if (node == null) {
            node = objectMapper.createArrayNode().add(integrateItem);
        }else {
            ((ArrayNode) node).add(integrateItem);
        }
        GableFileUtils.saveFile(node.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, INTEGRATE_TEST_FILE);
        return uuid;
    }

    @Override
    public JsonNode getOne(String uuid) {
        return GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.INTEGRATE, uuid, "define.json");
    }
}
