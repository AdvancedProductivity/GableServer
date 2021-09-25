package org.advancedproductivity.gable.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
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
        String userId = GableConfig.PUBLIC_PATH;
        if (!isPublic) {
            userId = userService.getUserId(request);
        }
        JsonNode allCase = caseService.getAllCase(userId, uuid);
        if (allCase == null) {
            return Result.error();
        }
        return Result.success().setData(allCase);
    }


    @PostMapping("/upload")
    private Result upload(@RequestParam(value = "file") MultipartFile file
            , @RequestParam String uuid
            , @RequestParam(required = false) Boolean isPublic) {
        try {
            String userId = GableConfig.PUBLIC_PATH;
            if (!isPublic) {
                userId = userService.getUserId(request);
            }
            ArrayNode cases = ExcelUtils.read(file.getOriginalFilename(), 0, file.getInputStream());
            return Result.success().setData(caseService.saveCases(cases, userId, uuid));
        } catch (Exception e) {
            log.error("error happens while read excel", e);
            return Result.error(e.getMessage());
        }
    }

}
