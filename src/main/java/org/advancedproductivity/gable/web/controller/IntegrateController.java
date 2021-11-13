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
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.IntegrateField;
import org.advancedproductivity.gable.framework.config.IntegrateStepStatus;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.HistoryService;
import org.advancedproductivity.gable.web.service.IntegrateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author zzq
 */
@RestController
@RequestMapping("/api/integrate")
public class IntegrateController {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private IntegrateService integrateService;

    @GetMapping
    public Result get() {
        JsonNode list = integrateService.list();
        return Result.success().setData(list);
    }

    @GetMapping("/detail")
    public Result getDetail(@RequestParam String uuid) {
        JsonNode list = integrateService.getIntegrateDefine(uuid);
        return Result.success().setData(list);
    }

    @PutMapping
    public Result addIntegrate(@RequestBody ArrayNode records, @RequestParam String name) {
        String uuid = integrateService.addIntegrate(records, name);
        return Result.success();
    }

    @DeleteMapping
    public Result deleteIntegrate(@RequestParam String uuid) {
        ObjectNode item = integrateService.getItem(uuid);
        if (item != null && Objects.equals(item.path(IntegrateField.STATUS).asInt(), IntegrateStepStatus.RUNNING.getValue())) {
            return Result.error("Is Running");
        }
        int c = integrateService.delete(uuid);
        return Result.success(String.valueOf(c));
    }

    @GetMapping("/entrust")
    private Result entrustRun(@RequestParam String uuid,
                              @RequestParam String env,
                              @RequestParam String server) {
        return integrateService.entrustRun(uuid, env, server);
    }

    @DeleteMapping("/entrust")
    private Result stopEntrustRun(@RequestParam String uuid) {
        return integrateService.stopEntrustRun(uuid);
    }

    @PostMapping
    public Result updateIntegrate(@RequestBody ArrayNode records,@RequestParam String uuid) {
        integrateService.updateIntegrate(records, uuid);
        return Result.success();
    }

    @Resource
    private HistoryService historyService;

    @PostMapping("/addHistory")
    public Result saveIntegrate(@RequestBody ArrayNode records, @RequestParam String uuid,
                                @RequestParam String server) {
        ObjectNode mapperObjectNode = historyService.analysis(records, server, uuid);
        mapperObjectNode.put("hisId", IntegrateField.PAGE_ORIGIN);
        int i = historyService.recordIntegrateTest(GableConfig.PUBLIC_PATH, uuid, mapperObjectNode.toPrettyString());
        mapperObjectNode.put("hisId", i);
        String startAt = mapperObjectNode.path("startAt").asText();
        String endAt = mapperObjectNode.path("endAt").asText();
        historyService.indexHistory(uuid, i, mapperObjectNode.path("noError").asBoolean(),
                IntegrateField.PAGE_ORIGIN, startAt, endAt);
        return Result.success(mapperObjectNode);
    }

    @GetMapping("/history")
    private Result getHistoryDetail(@RequestParam String uuid, @RequestParam Integer historyId,
                                    @RequestParam(required = false) Boolean isPublic) {
        JsonNode node = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.INTEGRATE,
                uuid,
                UserDataType.HISTORY,
                historyId + ".json");
        return Result.success().setData(node);
    }

    @GetMapping("/historyList")
    private Result getHistoryList(@RequestParam String uuid) {
        JsonNode node = historyService.readOverviewHistory(uuid);
        return Result.success().setData(node);
    }

}
