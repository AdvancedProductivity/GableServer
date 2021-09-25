package org.advancedproductivity.gable.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.config.ConfigField;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.core.GlobalVar;
import org.advancedproductivity.gable.framework.runner.RunnerHolder;
import org.advancedproductivity.gable.framework.runner.TestAction;
import org.advancedproductivity.gable.framework.urils.GableFileUtils;
import org.advancedproductivity.gable.framework.urils.PreHandleUtils;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.CaseService;
import org.advancedproductivity.gable.web.service.HistoryService;
import org.advancedproductivity.gable.web.service.UserService;
import org.advancedproductivity.gable.web.service.impl.MenuServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author zzq
 */
@RestController
@RequestMapping("/api/unit")
public class UnitController {
    @Resource
    private HttpServletRequest request;

    @Resource
    private UserService userService;

    @Resource
    private MessageSource messageSource;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private HistoryService historyService;

    @Resource
    private CaseService caseService;

    @GetMapping
    private Result get(@RequestParam String uuid,
                       @RequestParam(required = false) String caseId,
                       @RequestParam(required = false) Integer caseVersion,
                       @RequestParam(required = false) Boolean isPublic) {
        if (isPublic == null) {
            isPublic = false;
        }
        String userId = GableConfig.PUBLIC_PATH;
        if (!isPublic) {
            userId = userService.getUserId(request);
        }
        JsonNode in = GableFileUtils.readFileAsJson(GableConfig.getGablePath(),
                userId,
                UserDataType.UNIT,
                uuid,
                ConfigField.CONFIG_DEFINE_FILE_NAME);
        if (in == null) {
            return Result.error("not find");
        }
        if (!StringUtils.isEmpty(caseId) && caseVersion != null) {
            ObjectNode caseDetail = caseService.getCase(userId, uuid, caseVersion, caseId);
            if (caseDetail != null) {
                caseService.handleCase(in.path("config"), caseDetail);
            }
        }
        return Result.success().setData(in);
    }

    @PostMapping("/run")
    private Result run(@RequestBody ObjectNode data, @RequestParam String uuid, @RequestParam String type
            , @RequestParam(required = false) Boolean isPublic) {
        TestAction testAction = RunnerHolder.HOLDER.get(type);
        if (testAction == null) {
            return Result.error("unknown test type: " + type);
        }
        ObjectNode in = (ObjectNode) data.path("config");
        ObjectNode instance = (ObjectNode) data.path("instance");
        ObjectNode global = GlobalVar.globalVar.deepCopy();
        PreHandleUtils.preHandleInJson(in, instance, global);
        ObjectNode history = objectMapper.createObjectNode();
        ObjectNode out = objectMapper.createObjectNode();
        testAction.execute(in, out, instance, global);
        history.set("in", in);
        history.set("out", out);
        history.set("instance", instance);
        history.set("global", global);
        history.put("recordTime", System.currentTimeMillis());
        if (isPublic == null) {
            isPublic = false;
        }
        String userId = GableConfig.PUBLIC_PATH;
        if (!isPublic) {
            userId = userService.getUserId(request);
        }
        int historyId = historyService.recordUnitTest(userId, uuid, history.toPrettyString());
        out.put("historyId", historyId);
        return Result.success().setData(out);
    }
}
