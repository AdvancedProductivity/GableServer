package org.advancedproductivity.gable.web.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.CaseField;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.config.ValidateField;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.framework.utils.JsonDiffUtils;
import org.advancedproductivity.gable.web.service.CaseService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    @Override
    public JsonNode generateDemoCase() {
        ObjectNode root = objectMapper.createObjectNode();
        root.set(CaseField.HEADERS, objectMapper.createArrayNode().add("id").add("title"));
        root.set(CaseField.RECORD, objectMapper.createArrayNode()
                .add(objectMapper.createObjectNode().put("id", "case_1")
                        .put("title", "title_A"))
                .add(objectMapper.createObjectNode().put("id", "case_2")
                        .put("title", "title_B"))
        );
        return root;
    }

    @Override
    public ObjectNode genDefaultDiffJson() {
        ObjectNode mapperObjectNode = objectMapper.createObjectNode();
        mapperObjectNode.set(CaseField.DIFF_REPLACE, objectMapper.createObjectNode());
        mapperObjectNode.set(CaseField.DIFF_ADD, objectMapper.createObjectNode());
        mapperObjectNode.set(CaseField.DIFF_REMOVE, objectMapper.createObjectNode());
        mapperObjectNode.set(CaseField.DIFF_REMOVE_BY_INDEX, objectMapper.createObjectNode());
        return mapperObjectNode;
    }

    public ArrayNode getHeaders(JsonNode node) {
        ArrayNode headers = objectMapper.createArrayNode();
        if (node != null && !node.isMissingNode()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String headerName = fieldNames.next();
                if (StringUtils.equals(headerName, CaseField.CASE_TITLE)) {
                    continue;
                }
                headers.add(headerName);
            }
        }
        return headers;
    }

    @Override
    public ObjectNode saveCases(ArrayNode cases, String nameSpace, String uuid) {
        File file = FileUtils.getFile(GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, uuid, UserDataType.CASE);
        int version = 0;
        if (file.exists()) {
            String[] list = file.list();
            if (list != null && list.length > 0) {
                version = list.length - 1;
            }
        }
        ArrayNode newArr = cases.arrayNode();
        for (int i = 0; i < cases.size(); i++) {
            JsonNode aCase = cases.get(i);
            if (!aCase.isObject()) {
                continue;
            }
            if (!aCase.has(CaseField.ID)) {
                continue;
            }
            ObjectNode item = (ObjectNode) aCase;
            if (item.has(CaseField.CHINESE_TITLE)) {
                item.set(CaseField.CASE_TITLE, item.path(CaseField.CHINESE_TITLE));
            }
            if (item.has(CaseField.TITLE)) {
                item.set(CaseField.CASE_TITLE, item.path(CaseField.TITLE));
            }
            String id = item.path(CaseField.ID).asText();
            ObjectNode caseContent = objectMapper.createObjectNode();
            JsonNode diffNode = item.remove(CaseField.DIFF);
            if (diffNode == null){
                caseContent.set(CaseField.DIFF, genDefaultDiffJson());
            }else if (diffNode.isTextual()) {
                String diffStr = diffNode.asText();
                diffStr = StringUtils.remove(diffStr, " ");
                caseContent.set(CaseField.DIFF, getDiffByString(diffStr));
            } else {
                if (diffNode.isObject() && !diffNode.isEmpty()) {
                    caseContent.set(CaseField.DIFF, diffNode);
                }else {
                    caseContent.set(CaseField.DIFF, genDefaultDiffJson());
                }
            }
            JsonNode jsonSchemaNodes = item.remove(CaseField.JSON_SCHEMA);
            if (jsonSchemaNodes != null && jsonSchemaNodes.isTextual()) {
                String jsonSchemaStr = jsonSchemaNodes.asText();
                jsonSchemaStr = StringUtils.remove(jsonSchemaStr, " ");
                caseContent.put(CaseField.JSON_SCHEMA, jsonSchemaStr);
            } else {
                caseContent.set(CaseField.JSON_SCHEMA, jsonSchemaNodes);
            }
            newArr.add(item);
            GableFileUtils.saveFile(caseContent.toPrettyString(), GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, uuid,
                    UserDataType.CASE, version + "", id + ".json");
        }
        ObjectNode caseInfo = objectMapper.createObjectNode();
        ArrayNode headers = getHeaders(cases.get(0));
        caseInfo.set(CaseField.HEADERS, headers);
        caseInfo.set(CaseField.RECORD, newArr);
        caseInfo.put(CaseField.VERSION, version);
        GableFileUtils.saveFile(caseInfo.toPrettyString(), GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, uuid, CASE_FILE_NAME);
        return caseInfo;
    }

    private JsonNode getDiffByString(String diffStr) {
        try {
            JsonNode diffJson = objectMapper.readTree(diffStr);
            if (diffJson.isObject() && !diffJson.isEmpty()) {
                return diffJson;
            }else {
                return genDefaultDiffJson();
            }
        } catch (JsonProcessingException e) {
            return genDefaultDiffJson();
        }
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
        handleDiff(in, caseDetail);
        handleJsonSchema(in, caseDetail);
    }

    @Override
    public void saveToExcel(ServletOutputStream out, JsonNode allCase) {
        SXSSFWorkbook wb = new SXSSFWorkbook();
        Sheet wbSheet = wb.createSheet();
        wbSheet.setDefaultColumnWidth(20);
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        Font fontStyle = wb.createFont();
        fontStyle.setBold(true);
        fontStyle.setFontHeightInPoints((short) 16);
        cellStyle.setFont(fontStyle);
        JsonNode headerJson = allCase.path(CaseField.HEADERS);
        List<String> headers = new ArrayList<>();
        if (headerJson.isArray()) {
            Row headerRow = wbSheet.createRow(0);
            for (int i = 0; i < headerJson.size(); i++) {
                String s = headerJson.path(i).asText();
                headers.add(s);
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(s);
            }
        }
        JsonNode items = allCase.path(CaseField.RECORD);
        if (items.isArray() && items.size() > 0) {
            int index = 1;
            for (int i = 0; i < items.size(); i++) {
                JsonNode item = items.path(i);
                if (!item.isObject()) {
                    continue;
                }
                Row itemRow = wbSheet.createRow(index);
                for (int j = 0; j < headers.size(); j++) {
                    String headerName = headers.get(j);
                    String content = item.path(headerName).asText();
                    Cell cell = itemRow.createCell(j);
                    cell.setCellValue(content);
                }
                index++;
            }
        }

        try {
            wb.write(out);
        } catch (Exception e) {
            log.error("error happens while write xlsx file", e);
        }
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

    private void handleDiff(JsonNode in, ObjectNode caseDetail) {
        JsonNode diffDefine = caseDetail.path(CaseField.DIFF);
        if (diffDefine.isTextual()) {
            try {
                diffDefine = objectMapper.readTree(diffDefine.asText());
            } catch (Exception e) {
                log.error("error happens while handle case ", e);
            }
        }
        JsonDiffUtils.doDiffHandle(in, diffDefine);
    }
}
