package org.advancedproductivity.gable.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.*;
import org.advancedproductivity.gable.framework.core.GlobalVar;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.ExecuteService;
import org.advancedproductivity.gable.web.service.GroovyScriptService;
import org.advancedproductivity.gable.web.service.UserService;
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
    private ExecuteService executeService;

    @Resource
    private GroovyScriptService groovyScriptService;

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
        JsonNode nextIn = body.path(IntegrateField.NEXT_IN);
        JsonNode lastOut = body.path(IntegrateField.LAST_OUT);
        JsonNode instance = body.path(IntegrateField.INSTANCE);
        JsonNode global = GlobalVar.globalVar.deepCopy();
        return Result.success(executeService.executeStep(uuid, instance, global, nextIn, lastOut));
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

    @GetMapping("/preScriptList")
    private Result getPreScriptList() {
        return Result.success().setData(this.groovyScriptService.getScriptList(GroovyScriptType.PRE));
    }

    @GetMapping("/postScriptList")
    private Result getPostScriptList() {
        return Result.success().setData(this.groovyScriptService.getScriptList(GroovyScriptType.POST));
    }

    @PostMapping("/preScriptGroup")
    private Result addPreScriptGroup(@RequestParam String groupName) {
        groupName = StringUtils.trim(groupName);
        if (StringUtils.isEmpty(groupName)) {
            return Result.error("Group Name Can Not Be Empty");
        }
        return Result.success().setData(this.groovyScriptService.addGroup(GroovyScriptType.PRE, groupName));
    }

    @PostMapping("/postScriptGroup")
    private Result addPostScriptGroup(@RequestParam String groupName) {
        groupName = StringUtils.trim(groupName);
        if (StringUtils.isEmpty(groupName)) {
            return Result.error("Group Name Can Not Be Empty");
        }
        return Result.success().setData(this.groovyScriptService.addGroup(GroovyScriptType.POST, groupName));
    }

    @PostMapping("/preScript")
    private Result addPreScriptItem(@RequestParam String scriptName,
                                    @RequestParam String groupUuid,
                                    @RequestBody String code) {
        scriptName = StringUtils.trim(scriptName);
        if (StringUtils.isEmpty(scriptName)) {
            return Result.error("Group Name Can Not Be Empty");
        }
        if (this.groovyScriptService.haveExist(GroovyScriptType.PRE, scriptName)) {
            return Result.error("Script Name Have Exist");
        }
        return Result.success().setData(this.groovyScriptService.addItem(GroovyScriptType.PRE, groupUuid,
                scriptName, code));
    }

    @PostMapping("/postScript")
    private Result addPostScriptItem(@RequestParam String scriptName,
                                     @RequestParam String groupUuid,
                                     @RequestBody String code) {
        scriptName = StringUtils.trim(scriptName);
        if (StringUtils.isEmpty(scriptName)) {
            return Result.error("Script Name Can Not Be Empty");
        }
        if (this.groovyScriptService.haveExist(GroovyScriptType.POST, scriptName)) {
            return Result.error("Script Name Have Exist");
        }
        return Result.success().setData(this.groovyScriptService.addItem(GroovyScriptType.POST, groupUuid,
                scriptName, code));
    }

    @GetMapping("/readCode")
    private Result readScriptCode(@RequestParam String uuid) {
        return Result.success().setDataString(this.groovyScriptService.readCode(uuid));
    }

    @PutMapping("/updateScript")
    private Result updateScript(@RequestParam String uuid, @RequestBody String code) {
        this.groovyScriptService.updateScript(uuid, code);
        return Result.success();
    }

    @PostMapping("/executePreScript")
    private Result executePreScript( @RequestBody ObjectNode in) {
        if (!in.path("in").isObject()) {
            return Result.error("param not have in obj");
        }
        if (!in.path("instance").isObject()) {
            return Result.error("param not have instance obj");
        }
        if (!in.path("global").isObject()) {
            return Result.error("param not have global obj");
        }
        JsonNode code = in.remove("code");
        if (!code.isTextual()) {
            return Result.error("code not get");
        }
        if (!in.path("param").isObject()) {
            return Result.error("param not have param obj");
        }
        String uuid = "PreScriptDemoCode";
        this.groovyScriptService.updateScript(uuid, code.asText());
        return Result.success().setData(this.groovyScriptService.executePreScript(uuid, in));
    }

    @PostMapping("/executePostScript")
    private Result executePostScript(@RequestBody ObjectNode in) {
        if (!in.path("out").isObject()) {
            return Result.error("param not have in obj");
        }
        if (!in.path("instance").isObject()) {
            return Result.error("param not have instance obj");
        }
        if (!in.path("global").isObject()) {
            return Result.error("param not have global obj");
        }
        if (!in.path("param").isObject()) {
            return Result.error("param not have param obj");
        }
        JsonNode code = in.remove("code");
        if (!code.isTextual()) {
            return Result.error("code not get");
        }
        String uuid = "PostScriptDemoCode";
        this.groovyScriptService.updateScript(uuid, code.asText());
        return Result.success().setData(this.groovyScriptService.executePostScript(uuid, in));
    }

}
