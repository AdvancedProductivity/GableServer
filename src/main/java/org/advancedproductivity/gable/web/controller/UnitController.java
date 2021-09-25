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
import org.advancedproductivity.gable.web.service.HistoryService;
import org.advancedproductivity.gable.web.service.UserService;
import org.advancedproductivity.gable.web.service.impl.MenuServiceImpl;
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

    @GetMapping
    private Result get(@RequestParam String uuid, @RequestParam(required = false) Boolean isPublic) {
        if (isPublic == null) {
            isPublic = false;
        }
        String userId = GableConfig.PUBLIC_PATH;
        if (!isPublic) {
            userId = userService.getUserId(request);
        }
        JsonNode node = GableFileUtils.readFileAsJson(GableConfig.getGablePath(),
                userId,
                UserDataType.UNIT,
                uuid,
                ConfigField.CONFIG_DEFINE_FILE_NAME);
        if (node == null) {
            return Result.error("not find");
        } else {
            return Result.success().setData(node);
        }
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
