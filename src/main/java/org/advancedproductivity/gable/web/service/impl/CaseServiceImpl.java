package org.advancedproductivity.gable.web.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
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
            if (StringUtils.isEmpty(id)) {
                id = item.path("用例编号").asText();
            }
            ObjectNode caseContent = objectMapper.createObjectNode();
            caseContent.set("diff", item.remove("diff"));
            caseContent.set("jsonSchema", item.remove("jsonSchema"));
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
}
