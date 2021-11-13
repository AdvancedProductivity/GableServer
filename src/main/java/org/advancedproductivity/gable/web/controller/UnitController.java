/*
 *  Copyright (c) 2021 AdvancedProductivity
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.advancedproductivity.gable.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.config.CaseField;
import org.advancedproductivity.gable.framework.config.ConfigField;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.runner.RunnerHolder;
import org.advancedproductivity.gable.framework.runner.TestAction;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.*;
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

    @Resource
    private EnvService envService;

    @Resource
    private MenuService menuService;

    @Resource
    private UnitConfigService unitConfigService;

    @GetMapping("/history")
    private Result getHistory(@RequestParam String uuid, @RequestParam Integer historyId,
                              @RequestParam(required = false) Boolean isPublic) {
        String userId = userId = userService.getUserId(isPublic, request);
        JsonNode node = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), userId, UserDataType.UNIT,
                uuid,
                UserDataType.HISTORY,
                historyId + ".json");
        return Result.success().setData(node);
    }

    @PutMapping()
    private Result update(@RequestParam String uuid, @RequestBody JsonNode newConfig) {
        String userId  = userService.getUserId(null, request);
        JsonNode node = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), userId, UserDataType.UNIT,
                uuid,
                ConfigField.CONFIG_DEFINE_FILE_NAME);
        if (node == null || !node.isObject()) {
            return Result.error();
        }
        int version = node.path(ConfigField.VERSION).asInt();
        GableFileUtils.saveFile(node.toPrettyString(),
                GableConfig.getGablePath(),
                userId,
                UserDataType.UNIT,
                uuid,
                ConfigField.VERSION,
                version + ".json");
        ((ObjectNode) node).put(ConfigField.VERSION, version + 1)
                .set(ConfigField.DETAIL, newConfig);
        GableFileUtils.saveFile(node.toPrettyString(),
                GableConfig.getGablePath(),
                userId,
                UserDataType.UNIT,
                uuid,
                ConfigField.CONFIG_DEFINE_FILE_NAME);
        return Result.success().setData(node);
    }

    @GetMapping
    private Result get(@RequestParam String uuid,
                       @RequestParam(required = false) String caseId,
                       @RequestParam(required = false) Integer caseVersion,
                       @RequestParam(required = false) Boolean isPublic,
                       @RequestParam(required = false) String env) {
        if (isPublic == null) {
            isPublic = false;
        }
        String userId = userService.getUserId(isPublic, request);
        JsonNode in = unitConfigService.getConfig(userId, uuid, env, caseId, caseVersion);
        if (in == null) {
            return Result.error("not find");
        }
        return Result.success().setData(in);
    }

    @GetMapping("/diff")
    private Result getDiff(@RequestParam String uuid,
                           @RequestParam(required = false) String caseId,
                           @RequestParam(required = false) Integer caseVersion,
                           @RequestParam(required = false) Boolean isPublic,
                           @RequestParam(required = false) String env) {
        if (isPublic == null) {
            isPublic = false;
        }
        String userId = userService.getUserId(isPublic, request);
        JsonNode in = GableFileUtils.readFileAsJson(GableConfig.getGablePath(),
                userId,
                UserDataType.UNIT,
                uuid,
                ConfigField.CONFIG_DEFINE_FILE_NAME);
        if (in == null) {
            return Result.error("not find");
        }
        in = in.deepCopy();
        if (!StringUtils.isEmpty(env)) {
            JsonNode envConfig = envService.getEnv(env);
            if (envConfig != null && !envConfig.isMissingNode()) {
                envService.handleConfig(in, envConfig);
            }
        }
        ObjectNode res = objectMapper.createObjectNode();
        res.set("before", in.path("config").deepCopy());
        if (!StringUtils.isEmpty(caseId) && caseVersion != null) {
            ObjectNode caseDetail = caseService.getCase(userId, uuid, caseVersion, caseId);
            if (caseDetail != null) {
                caseService.handleCase(in.path("config"), caseDetail);
            }
        }
        res.set("after", in.path("config"));
        return Result.success().setData(res);
    }

    @Resource
    private JsonService jsonService;

    @GetMapping("/allField")
    private Result allField(@RequestParam String uuid,
                            @RequestParam(required = false) String caseId,
                            @RequestParam(required = false) Boolean isPublic,
                            @RequestParam(required = false) String env) {
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
        ObjectNode data = objectMapper.createObjectNode();
        if (in == null) {
            data.put("error", "config not find");
            return Result.success(data);
        }
        JsonNode inNode = in.path(ConfigField.DETAIL);
        jsonService.traverFields(inNode, data, "");
        ObjectNode result = objectMapper.createObjectNode();
        result.set(CaseField.ALL_FIELD, data);
        result.set(CaseField.IN, inNode);
        return Result.success().setData(result);
    }


    @Resource
    private ExecuteService executeService;

    @PostMapping("/run")
    private Result run(@RequestBody ObjectNode data,
                       @RequestParam String uuid,
                       @RequestParam String type,
                       @RequestParam(required = false) Boolean isPublic) {
        String userId = userService.getUserId(isPublic, request);
        TestAction testAction = RunnerHolder.HOLDER.get(type);
        if (testAction == null) {
            return Result.error("unknown test type: " + type);
        }
        ObjectNode out = executeService.executeTest(userId, uuid, type, data);
        return Result.success().setData(out);
    }


    @PostMapping("/push")
    public Result push(@RequestBody ObjectNode info) {
        String uuid = info.path("from").asText();
        if (StringUtils.isEmpty(uuid)) {
            return Result.error();
        }
        String groupUuid = info.path("toGroup").asText();
        if (StringUtils.isEmpty(groupUuid)) {
            return Result.error();
        }
        String testName = info.path("testName").asText();
        if (StringUtils.isEmpty(testName)) {
            return Result.error();
        }
        String userId = userService.getUserId(null, request);
        JsonNode in = GableFileUtils.readFileAsJson(GableConfig.getGablePath(),
                userId,
                UserDataType.UNIT,
                uuid,
                ConfigField.CONFIG_DEFINE_FILE_NAME);
        ArrayNode publicUnitMenus = menuService.getPublicUnitMenus();
        String newUuid = menuService.pushUnit(publicUnitMenus, testName, groupUuid, in, userId, uuid);
        return Result.success(newUuid);
    }

    @PostMapping("/clone")
    public Result clone(@RequestBody ObjectNode info) {
        String uuid = info.path("uuid").asText();
        if (StringUtils.isEmpty(uuid)) {
            return Result.error();
        }
        String groupUuid = info.path("toGroup").asText();
        if (StringUtils.isEmpty(groupUuid)) {
            return Result.error();
        }
        String testName = info.path("testName").asText();
        if (StringUtils.isEmpty(testName)) {
            return Result.error();
        }
        String userId = userService.getUserId(null, request);
        JsonNode in = GableFileUtils.readFileAsJson(GableConfig.getGablePath(),
                GableConfig.PUBLIC_PATH,
                UserDataType.UNIT,
                uuid,
                ConfigField.CONFIG_DEFINE_FILE_NAME);
        if (in == null) {
            return Result.error();
        }
        ArrayNode userUnitMenus = menuService.getUserUnitMenus(userId);
        String newUuid = menuService.cloneUnit(userUnitMenus, testName, groupUuid, in, userId, uuid);
        return Result.success(newUuid);
    }

    @PostMapping("/update")
    public Result update(@RequestBody ObjectNode info) {
        String from = info.path("from").asText();
        if (StringUtils.isEmpty(from)) {
            return Result.error();
        }
        String to = info.path("to").asText();
        if (StringUtils.isEmpty(from)) {
            return Result.error();
        }
        String userId = userService.getUserId(null, request);
        menuService.sync(from, to, userId);
        return Result.success();
    }
}
