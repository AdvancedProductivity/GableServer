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

package org.advancedproductivity.gable.web.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.IntegrateField;
import org.advancedproductivity.gable.framework.thread.EnTrustHandle;
import org.advancedproductivity.gable.web.service.ExecuteService;
import org.advancedproductivity.gable.web.service.HistoryService;
import org.advancedproductivity.gable.web.service.UnitConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zzq
 */
@Slf4j
@Service("DefaultEnTrustHandleImpl")
public class DefaultEnTrustHandleImpl implements EnTrustHandle {

    @Resource
    private HistoryService historyService;

    @Resource
    private UnitConfigService unitConfigService;

    @Resource
    private ExecuteService executeService;

    @Override
    public int takeHistory(String uuid, ArrayNode history) {
        return historyService.recordIntegrateTest(GableConfig.PUBLIC_PATH, uuid,
                history.toPrettyString());
    }

    @Override
    public boolean recordHistory(String server, int historyId, String uuid, ArrayNode define,
                                 String envUuid, String envName) {
        ObjectNode his = historyService.analysis(define, server, uuid);

        his.put("hisId", historyId);
        his.put("origin", IntegrateField.ENTRUST_ORIGIN);
        his.put("envUuid", envUuid);
        his.put("envName", envName);
        historyService.recordIntegrateTest(historyId, GableConfig.PUBLIC_PATH, uuid,
                his.toPrettyString());
        String startAt = his.path("startAt").asText();
        String endAt = his.path("endAt").asText();
        historyService.indexHistory(uuid, historyId, his.path("noError").asBoolean(),
                IntegrateField.ENTRUST_ORIGIN, startAt, endAt, envName);
        return his.path(IntegrateField.NO_ERROR).asBoolean();
    }

    @Override
    public ObjectNode getConfig(String nameSpace, String uuid, String env, String caseId, Integer caseVersion) {
        JsonNode config = unitConfigService.getConfig(nameSpace, uuid, env, caseId, caseVersion);
        if (config == null) {
            return null;
        }
        return (ObjectNode) config;
    }

    @Override
    public ObjectNode runStep(String uuid, JsonNode instance, JsonNode global, JsonNode nextIn, JsonNode lastOut) {
        return executeService.executeStep(uuid, instance, global, nextIn, lastOut);
    }

    @Override
    public ObjectNode runJsonScheam(String uuid, JsonNode lastOut, JsonNode schema) {
        return executeService.executeJsonSchema(uuid, schema, lastOut);
    }

    @Override
    public ObjectNode runTest(String nameSpace, String uuid, String type, ObjectNode data) {
        return executeService.executeTest(nameSpace, uuid, type, data);
    }

}
