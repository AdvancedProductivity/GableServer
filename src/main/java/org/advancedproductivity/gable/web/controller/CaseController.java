package org.advancedproductivity.gable.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.CaseField;
import org.advancedproductivity.gable.framework.utils.ExcelReadUtils;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.CaseService;
import org.advancedproductivity.gable.web.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * @author zzq
 */
@RestController
@RequestMapping("/api/case")
@Slf4j
public class CaseController {
    @Resource
    private HttpServletRequest request;

    @Resource
    private UserService userService;

    @Resource
    private CaseService caseService;

    @Resource
    private ObjectMapper objectMapper;

    @GetMapping
    public Result get(@RequestParam String uuid, @RequestParam(required = false) Boolean isPublic) {
        String userId = userService.getUserId(isPublic, request);
        JsonNode allCase = caseService.getAllCase(userId, uuid);
        if (allCase == null) {
            return Result.error();
        }
        return Result.success().setData(allCase);
    }

    @GetMapping("export")
    public void export(@RequestParam String uuid, @RequestParam(required = false) Boolean isPublic,
                       HttpServletResponse response) {
        String userId = userService.getUserId(isPublic, request);
        JsonNode allCase = caseService.getAllCase(userId, uuid);
        JsonNode header = allCase.path(CaseField.HEADERS);
        if (header.isArray()) {
            ArrayNode headers = (ArrayNode) header;
            headers.add(CaseField.DIFF);
            headers.add(CaseField.JSON_SCHEMA);
        }
        JsonNode items = allCase.path(CaseField.RECORD);
        int version = allCase.path(CaseField.VERSION).asInt();
        if (items.isArray() && items.size() > 0) {
            ArrayNode details = (ArrayNode) items;
            for (int i = 0; i < details.size(); i++) {
                JsonNode d = details.path(i);
                if (!d.isObject()) {
                    log.error("find un expected json type in case array {}", d.getNodeType());
                    continue;
                }
                ObjectNode item = (ObjectNode) d;
                String caseId = item.path(CaseField.ID).asText();
                ObjectNode caseDetail = caseService.getCase(userId, uuid, version, caseId);
                if (caseDetail == null) {
                    log.error("case not find {} {} {} {}", userId, uuid, version, caseId);
                }
                JsonNode diffJson = caseDetail.path(CaseField.DIFF);
                String diffStr = "";
                if (diffJson.isObject()) {
                    diffStr = diffJson.toString();
                } else if (diffJson.isTextual()) {
                    diffStr = diffJson.asText();
                }
                item.put(CaseField.DIFF, diffStr);
                String jsonSchemaStr = "";
                JsonNode jsonSchemaJson = caseDetail.path(CaseField.JSON_SCHEMA);
                if (jsonSchemaJson.isObject()) {
                    jsonSchemaStr = jsonSchemaJson.toString();
                } else if (jsonSchemaJson.isTextual()) {
                    jsonSchemaStr = jsonSchemaJson.asText();
                }
                item.put(CaseField.JSON_SCHEMA, jsonSchemaStr);
            }
        }
        response.setHeader("Content-type", "application/xlsx");
        response.setHeader("Content-Disposition", "attachment; filename=Case_" + version + ".xlsx");
        try {
            ServletOutputStream out = response.getOutputStream();
            caseService.saveToExcel(out, allCase);
            out.close();
        } catch (Exception e) {
            log.error("error happens while export case excel", e);
        }
    }

    @GetMapping("/item")
    public Result get(@RequestParam String uuid,
                      @RequestParam String caseId,
                      @RequestParam Integer version,
                      @RequestParam(required = false) Boolean isPublic) {
        String userId = userService.getUserId(isPublic, request);
        ObjectNode data = caseService.getCase(userId, uuid, version, caseId);
        return Result.success().setData(data);
    }

    @PutMapping
    public Result updateCaseInfo(@RequestParam String uuid,
                                 @RequestParam Integer version,
                                 @RequestParam String caseId,
                                 @RequestBody ObjectNode diffAndValidate,
                                 @RequestParam(required = false) Boolean isPublic) {
        String userId = userService.getUserId(isPublic, request);
        try {
            String diffJson = diffAndValidate.path(CaseField.DIFF).asText();
            String jsonSchemaJson = diffAndValidate.path(CaseField.JSON_SCHEMA).asText();
            objectMapper.readTree(diffJson);
            objectMapper.readTree(jsonSchemaJson);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
        boolean r = caseService.updateCase(userId, uuid, version, caseId, diffAndValidate);
        return Result.success();
    }


    @PostMapping("/upload")
    private Result upload(@RequestParam(value = "file") MultipartFile file
            , @RequestParam String uuid
            , @RequestParam(required = false) Boolean isPublic) {
        try {
            String userId = userService.getUserId(isPublic, request);
            ArrayNode cases = ExcelReadUtils.read(file.getOriginalFilename(), 0, file.getInputStream());
            return Result.success().setData(caseService.saveCases(cases, userId, uuid));
        } catch (Exception e) {
            log.error("error happens while read excel", e);
            return Result.error(e.getMessage());
        }
    }

}
