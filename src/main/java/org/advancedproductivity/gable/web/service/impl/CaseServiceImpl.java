package org.advancedproductivity.gable.web.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.CaseField;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.config.ValidateField;
import org.advancedproductivity.gable.framework.urils.GableFileUtils;
import org.advancedproductivity.gable.web.service.CaseService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.Iterator;

/**
 * @author zzq
 */
@Service
@Slf4j
public class CaseServiceImpl implements CaseService {
    private static final String CASE_FILE_NAME = "case.json";

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public JsonNode getAllCase(String nameSpace, String testUuid) {
        return GableFileUtils.readFileAsJson(GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, testUuid, CASE_FILE_NAME);
    }

    public ArrayNode getHeaders(JsonNode node) {
        ArrayNode headers = objectMapper.createArrayNode();
        if (node != null && !node.isMissingNode()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                headers.add(fieldNames.next());
            }
        }
        return headers;
    }

    @Override
    public ObjectNode saveCases(ArrayNode cases,String nameSpace, String uuid) {
        File file = FileUtils.getFile(GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, uuid, UserDataType.CASE);
        int version = 0;
        if (file.exists()) {
            String[] list = file.list();
            if (list != null) {
                version = list.length;
            }
        }
        for (int i = 0; i < cases.size(); i++) {
            ObjectNode item = (ObjectNode) cases.get(i);
            String id = item.path("id").asText();
            ObjectNode caseContent = objectMapper.createObjectNode();
            JsonNode diffNode = item.remove(CaseField.DIFF);
            if (diffNode != null && diffNode.isTextual()) {
                String diffStr = diffNode.asText();
                diffStr = StringUtils.remove(diffStr, " ");
                caseContent.put(CaseField.DIFF, diffStr);
            } else {
                caseContent.set(CaseField.DIFF, diffNode);
            }
            JsonNode jsonSchemaNodes = item.remove(CaseField.JSON_SCHEMA);
            if (jsonSchemaNodes != null && jsonSchemaNodes.isTextual()) {
                String jsonSchemaStr = jsonSchemaNodes.asText();
                jsonSchemaStr = StringUtils.remove(jsonSchemaStr, " ");
                caseContent.put(CaseField.JSON_SCHEMA, jsonSchemaStr);
            } else {
                caseContent.set(CaseField.JSON_SCHEMA, jsonSchemaNodes);
            }
            GableFileUtils.saveFile(caseContent.toPrettyString(), GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, uuid,
                    UserDataType.CASE, version + "", id + ".json");
        }
        ObjectNode caseInfo = objectMapper.createObjectNode();
        ArrayNode headers = getHeaders(cases.get(0));
        caseInfo.set("headers", headers);
        caseInfo.set("record", cases);
        caseInfo.put("version", version);
        GableFileUtils.saveFile(caseInfo.toPrettyString(), GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, uuid, CASE_FILE_NAME);
        return caseInfo;
    }

    @Override
    public ObjectNode getCase(String nameSpace, String uuid, Integer version, String caseId) {
        return (ObjectNode) GableFileUtils.readFileAsJson(GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, uuid, UserDataType.CASE, version + "", caseId + ".json");
    }

    @Override
    public boolean updateCase(String nameSpace, String uuid, Integer version, String caseId, ObjectNode diffAndValidate) {
        return GableFileUtils.saveFile(diffAndValidate.toPrettyString(), GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, uuid,
                UserDataType.CASE, version + "", caseId + ".json");
    }

    @Override
    public void handleCase(JsonNode in, ObjectNode caseDetail) {
        handlePreHandle(in, caseDetail);
        handleJsonSchema(in, caseDetail);
    }

    private void handleJsonSchema(JsonNode in, ObjectNode caseDetail) {
        JsonNode jsonNode = caseDetail.path(CaseField.JSON_SCHEMA);
        if (jsonNode.isMissingNode() || jsonNode.isNull()) {
            return;
        }
        ObjectNode validateNode = null;
        if (in.has(ValidateField.VALIDATE)) {
            validateNode = (ObjectNode) in.path(ValidateField.VALIDATE);
        } else {
            validateNode = objectMapper.createObjectNode();
        }
        validateNode.set(ValidateField.JSON_SCHEMA, jsonNode);
        ((ObjectNode) in).set(ValidateField.VALIDATE, validateNode);
    }

    private void handlePreHandle(JsonNode in, ObjectNode caseDetail) {
        JsonNode diffHandle = caseDetail.path(CaseField.DIFF);
        if (diffHandle.isTextual()) {
            try {
                diffHandle = objectMapper.readTree(diffHandle.asText());
            } catch (Exception e) {
                log.error("error happens while handle case ", e);
            }
        }
        Iterator<String> fieldNames = diffHandle.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            if (!StringUtils.startsWith(key, "/")) {
                continue;
            }
            String rootNodeKey = StringUtils.substringBeforeLast(key, "/");
            String aimField = StringUtils.substringAfterLast(key, "/");
            if (StringUtils.isEmpty(rootNodeKey) || StringUtils.isEmpty(aimField)) {
                continue;
            }
            JsonNode at = in.at(rootNodeKey);
            if (at.isObject()) {
                ((ObjectNode) at).set(aimField, diffHandle.path(key));
            }
        }
    }
}
