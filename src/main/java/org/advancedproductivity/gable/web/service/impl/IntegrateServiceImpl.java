package org.advancedproductivity.gable.web.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.IntegrateField;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.service.IntegrateService;
import org.apache.commons.lang3.StringUtils;
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
        String uuid = UUID.randomUUID().toString();
        for (JsonNode record : records) {
            if (StringUtils.equals(record.path("type").asText(), "STEP")) {
                JsonNode codeNode = record.path("code");
                if (codeNode.isMissingNode() || codeNode.isNull()) {
                    continue;
                }
                String code = codeNode.asText();
                String stepUuid = record.path("uuid").asText();
                if (StringUtils.isEmpty(stepUuid)) {
                    stepUuid = "public_" + UUID.randomUUID().toString();
                    ((ObjectNode) record).put("uuid", stepUuid);
                }
                GableFileUtils.saveFile(code, GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.GROOVY, stepUuid + ".groovy");
            }
        }
        GableFileUtils.saveFile(records.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.INTEGRATE, uuid, "define.json");
        JsonNode node = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, INTEGRATE_TEST_FILE);
        ObjectNode integrateItem = objectMapper.createObjectNode();
        integrateItem.put(IntegrateField.UUID, uuid);
        integrateItem.put(IntegrateField.NAME, name);
        integrateItem.set(IntegrateField.TAG, objectMapper.createArrayNode());
        if (node == null) {
            node = objectMapper.createArrayNode().add(integrateItem);
        }else {
            ((ArrayNode) node).add(integrateItem);
        }
        GableFileUtils.saveFile(node.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, INTEGRATE_TEST_FILE);
        return uuid;
    }

    @Override
    public boolean updateIntegrate(ArrayNode records, String uuid) {
        for (JsonNode record : records) {
            if (StringUtils.equals(record.path("type").asText(), "STEP")) {
                JsonNode codeNode = record.path("code");
                if (codeNode.isMissingNode() || codeNode.isNull()) {
                    continue;
                }
                String code = codeNode.asText();
                String stepUuid = record.path("uuid").asText();
                if (StringUtils.isEmpty(stepUuid)) {
                    stepUuid = "public_" + UUID.randomUUID().toString();
                    ((ObjectNode) record).put("uuid", stepUuid);
                }
                GableFileUtils.saveFile(code, GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.GROOVY, stepUuid + ".groovy");
            }
        }
        GableFileUtils.saveFile(records.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.INTEGRATE, uuid, "define.json");
        return true;
    }

    @Override
    public JsonNode getOne(String uuid) {
        return GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.INTEGRATE, uuid, "define.json");
    }

    @Override
    public boolean addTag(String tagName, String uuid) {
        JsonNode allTests = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, INTEGRATE_TEST_FILE);
        if (allTests == null) {
            return false;
        }
        for (int i = 0; i < allTests.size(); i++) {
            JsonNode node = allTests.get(i);
            if (!node.isObject()) {
                continue;
            }
            ObjectNode item = (ObjectNode) node;
            if (!StringUtils.equals(item.path(IntegrateField.UUID).asText(), uuid)) {
                continue;
            }
            JsonNode tags = item.path(IntegrateField.TAG);
            if (!tags.isArray()) {
                ArrayNode newTags = objectMapper.createArrayNode();
                newTags.add(tagName);
                item.set(IntegrateField.TAG, newTags);
            }else {
                ArrayNode newTags = (ArrayNode) tags;
                for (JsonNode newTag : newTags) {
                    if (StringUtils.equals(newTag.asText(), tagName)) {
                        return false;
                    }
                }
                newTags.add(tagName);
            }
            GableFileUtils.saveFile(allTests.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, INTEGRATE_TEST_FILE);
            return true;
        }
        return false;
    }
}
