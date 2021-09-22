package org.advancedproductivity.gable.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.config.ConfigField;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.runner.RunnerHolder;
import org.advancedproductivity.gable.framework.runner.TestAction;
import org.advancedproductivity.gable.framework.urils.GableFileUtils;
import org.advancedproductivity.gable.web.entity.Result;
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

    @GetMapping
    private Result get(@RequestParam String uuid) {
        String userId = userService.getUserId(request);
        JsonNode node = GableFileUtils.readFileAsJson(GableConfig.getGablePath(),
                userId,
                UserDataType.UNIT,
                uuid,
                ConfigField.CONFIG_DEFINE_FILE_NAME);
        if (node == null) {
            return Result.error("not find");
        }else {
            return Result.success().setData(node);
        }
    }

    @PostMapping("/run")
    private Result run(@RequestBody ObjectNode in, @RequestParam String uuid, @RequestParam String type) {
        TestAction testAction = RunnerHolder.HOLDER.get(type);
        if (testAction == null) {
            return Result.error("unknown test type: " + type);
        }
        ObjectNode out = objectMapper.createObjectNode();
        testAction.execute(in, out);
        return Result.success().setData(out);
    }
}
