package org.advancedproductivity.gable.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.config.ValidateField;
import org.advancedproductivity.gable.framework.core.GlobalVar;
import org.advancedproductivity.gable.framework.groovy.GroovyScriptUtils;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.HistoryService;
import org.advancedproductivity.gable.web.service.UserService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * @author zzq
 */
@RequestMapping("/api/groovyCode")
@RestController
@Slf4j
public class GroovyCodeController {

    @Resource
    private HttpServletRequest request;

    @Resource
    private UserService userService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private HistoryService historyService;

    @GetMapping()
    public String getCode(@RequestParam String uuid,
                          @RequestParam(required = false) Boolean isPublic) {
        String userId = userService.getUserId(isPublic, request);
        return GableFileUtils.readFileAsString(GableConfig.getGablePath(), userId, UserDataType.GROOVY, uuid + ".groovy");
    }

    @PutMapping()
    public Result updateCode(@RequestParam String uuid,
                             @RequestBody String code) {
        String userId = userService.getUserId(null, request);
        GableFileUtils.saveFile(code, GableConfig.getGablePath(), userId, UserDataType.GROOVY,
                uuid + ".groovy");
        return Result.success();
    }

    @PostMapping()
    public Result runStep(@RequestParam String uuid,
                          @RequestBody ObjectNode body) {
        JsonNode nextIn = body.path("nextIn");
        JsonNode lastOut = body.path("lastOut");
        JsonNode instance = body.path("instance");
        JsonNode global = GlobalVar.globalVar.deepCopy();
        ObjectNode out = objectMapper.createObjectNode();
        ObjectNode before = objectMapper.createObjectNode();
        before.set("instance", instance.deepCopy());
        before.set("global", global.deepCopy());
        before.set("lastOut", lastOut.deepCopy());
        before.set("nextIn", nextIn.deepCopy());
        out.set("before", before);
        String testFile = uuid + ".groovy";
        ObjectNode validateResult = GroovyScriptUtils.runStep(GableConfig.PUBLIC_PATH, nextIn, lastOut, instance, global, testFile);
        ObjectNode after = objectMapper.createObjectNode();
        after.set("instance", instance);
        after.set("global", global);
        after.set("lastOut", lastOut);
        after.set("nextIn", nextIn);
        out.set("after", after);
        out.set(ValidateField.VALIDATE, validateResult);
        int historyId = historyService.recordGroovy(GableConfig.PUBLIC_PATH, uuid, out.toPrettyString());
        out.put("historyId", historyId);
        return Result.success(out);
    }

    @GetMapping("/history")
    private Result getHistory(@RequestParam String uuid, @RequestParam Integer historyId,
                              @RequestParam(required = false) Boolean isPublic) {
        String userId = userId = userService.getUserId(isPublic, request);
        JsonNode node = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), userId, UserDataType.GROOVY_HIS,
                uuid,
                UserDataType.HISTORY,
                historyId + ".json");
        return Result.success().setData(node);
    }

}
