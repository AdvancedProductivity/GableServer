package org.advancedproductivity.gable.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.CaseField;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.urils.ExcelUtils;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.CaseService;
import org.advancedproductivity.gable.web.service.UserService;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

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
            ArrayNode cases = ExcelUtils.read(file.getOriginalFilename(), 0, file.getInputStream());
            return Result.success().setData(caseService.saveCases(cases, userId, uuid));
        } catch (Exception e) {
            log.error("error happens while read excel", e);
            return Result.error(e.getMessage());
        }
    }

}
