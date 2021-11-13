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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersionDetector;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.*;
import org.advancedproductivity.gable.framework.core.GlobalVar;
import org.advancedproductivity.gable.framework.groovy.GroovyScriptUtils;
import org.advancedproductivity.gable.framework.runner.GroovyCodeRunner;
import org.advancedproductivity.gable.framework.runner.RunnerHolder;
import org.advancedproductivity.gable.framework.runner.TestAction;
import org.advancedproductivity.gable.framework.utils.PreHandleUtils;
import org.advancedproductivity.gable.framework.utils.jsonschema.JsonSchemaUtils;
import org.advancedproductivity.gable.web.service.ExecuteService;
import org.advancedproductivity.gable.web.service.GroovyScriptService;
import org.advancedproductivity.gable.web.service.HistoryService;
import org.advancedproductivity.gable.web.service.JsonSchemaService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Set;

@Service
@Slf4j
public class ExecuteServiceImpl implements ExecuteService {
    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private HistoryService historyService;

    @Resource
    private JsonSchemaService jsonSchemaService;

    @Resource
    private GroovyScriptService groovyScriptService;

    @Override
    public ObjectNode executeStep(String uuid, JsonNode instance, JsonNode global, JsonNode nextIn, JsonNode lastOut) {
        ObjectNode out = objectMapper.createObjectNode();
        ObjectNode before = objectMapper.createObjectNode();
        before.set(IntegrateField.INSTANCE, instance.deepCopy());
        before.set(IntegrateField.GLOBAL, global.deepCopy());
        before.set(IntegrateField.LAST_OUT, lastOut.deepCopy());
        before.set(IntegrateField.NEXT_IN, nextIn.deepCopy());
        out.set(IntegrateField.BEFORE, before);
        String testFile = uuid + ".groovy";
        ObjectNode validateResult = GroovyScriptUtils.runStep(GableConfig.PUBLIC_PATH, nextIn, lastOut, instance, global, testFile);
        ObjectNode after = objectMapper.createObjectNode();
        after.set(IntegrateField.INSTANCE, instance);
        after.set(IntegrateField.GLOBAL, global);
        after.set(IntegrateField.LAST_OUT, lastOut);
        after.set(IntegrateField.NEXT_IN, nextIn);
        out.set(IntegrateField.AFTER, after);
        out.set(ValidateField.VALIDATE, validateResult);
        int historyId = historyService.recordGroovy(GableConfig.PUBLIC_PATH, uuid, out.toPrettyString());
        out.put(ConfigField.HISTORY_ID, historyId);
        return out;
    }

    @Override
    public ObjectNode executeJsonSchema(String uuid, JsonNode schema, JsonNode json) {
        ObjectNode resp = objectMapper.createObjectNode();
        ArrayNode jsonSchemaError = objectMapper.createArrayNode();
        ObjectNode validateResult = objectMapper.createObjectNode();
        if (schema == null || !schema.isObject()) {
            jsonSchemaError.add("JsonSchema format error");
            validateResult.put(ValidateField.RESULT, false);
        }
        if (json == null || (!json.isObject() && !json.isArray())) {
            jsonSchemaError.add("the json wait for validate must be array or object");
            validateResult.put(ValidateField.RESULT, false);
        }
        if (jsonSchemaError.size() == 0) {
            try {
                JsonSchemaFactory factory = JsonSchemaUtils.getInstance(SpecVersionDetector.detect(schema));
                JsonSchema validator = factory.getSchema(schema);
                Set<ValidationMessage> validate = validator.validate(json);
                for (ValidationMessage validationMessage : validate) {
                    jsonSchemaError.add(validationMessage.getMessage());
                }
                validateResult.put(ValidateField.RESULT, jsonSchemaError.size() == 0);
            } catch (Exception e) {
                jsonSchemaError.add(e.getMessage());
                validateResult.put(ValidateField.RESULT, false);
            }
        }
        resp.set("json", json);
        resp.set("schema", schema);
        validateResult.set(ValidateField.JSON_SCHEMA, jsonSchemaError);
        resp.set(ValidateField.VALIDATE, validateResult);
        int historyId = historyService.recordJsonSchemaStep(GableConfig.PUBLIC_PATH, uuid, resp.toPrettyString());
        resp.put(ConfigField.HISTORY_ID, historyId);
        return resp;
    }


    @Override
    public ObjectNode executeTest(String userId, String uuid, String type, ObjectNode data) {
        TestAction testAction = RunnerHolder.HOLDER.get(type);
        ObjectNode in = (ObjectNode) data.path(ConfigField.DETAIL);
        ObjectNode originIn = in.deepCopy();
        if (testAction instanceof GroovyCodeRunner) {
            in.put("userId", userId);
        }
        ObjectNode instance = (ObjectNode) data.path(IntegrateField.INSTANCE);
        ObjectNode global = GlobalVar.globalVar.deepCopy();
        executePreScriptAndPreHandle(in, instance, global);
        ObjectNode afterPreHanlde = in.deepCopy();
        ObjectNode history = objectMapper.createObjectNode();
        ObjectNode out = objectMapper.createObjectNode();
        testAction.execute(in, out, instance, global);
        // validate json schema
        ArrayNode jsonSchemaError = jsonSchemaService.validate(in, out, type, objectMapper);
        ObjectNode validateResult = objectMapper.createObjectNode();
        if (jsonSchemaError.size() == 0) {
            validateResult.put(ValidateField.RESULT, true);
        }else {
            validateResult.put(ValidateField.RESULT, false);
        }
        validateResult.set(ValidateField.JSON_SCHEMA, jsonSchemaError);
        out.set(ValidateField.VALIDATE, validateResult);
        executePostScriptAndPreHandle(originIn, out, instance, global);
        history.set(IntegrateField.IN, originIn);
        history.set(IntegrateField.REAL_IN, afterPreHanlde);
        history.set(IntegrateField.OUT, out);
        history.set(IntegrateField.INSTANCE, instance);
        history.set(IntegrateField.GLOBAL, global);
        history.put(ConfigField.TEST_TYPE, type);
        history.put("recordTime", System.currentTimeMillis());
        int historyId = historyService.recordUnitTest(userId, uuid, history.toPrettyString());
        out.put(ConfigField.HISTORY_ID, historyId);
        return out;
    }

    private void executePostScriptAndPreHandle(ObjectNode in, ObjectNode out, ObjectNode instance, ObjectNode global) {
        JsonNode postScripts = in.path(ConfigField.POST_SCRIPT);
        if (!postScripts.isArray()) {
            return;
        }
        for (JsonNode postScript : postScripts) {
            String preScriptName = postScript.path(GroovyScriptField.NAME).asText();
            if (!StringUtils.isEmpty(preScriptName)) {
                String scriptUuid = this.groovyScriptService.getUuidByName(preScriptName, GroovyScriptType.POST);
                if (!StringUtils.isEmpty(scriptUuid)) {
                    JsonNode param = postScript.path(GroovyScriptField.PARAM);
                    if (!param.isObject()) {
                        param = objectMapper.createObjectNode();
                    }
                    GroovyScriptUtils.runPostScript(scriptUuid, out, (ObjectNode) param, instance, global);
                }
            }
        }

    }

    public void executePreScriptAndPreHandle(ObjectNode in, ObjectNode instance, ObjectNode global) {
        PreHandleUtils.preHandleInJson(in, instance, global);
        JsonNode preScripts = in.path(ConfigField.PRE_SCRIPT);
        if (!preScripts.isArray()) {
            return;
        }
        for (JsonNode preScript : preScripts) {
            String preScriptName = preScript.path(GroovyScriptField.NAME).asText();
            if (!StringUtils.isEmpty(preScriptName)) {
                String scriptUuid = this.groovyScriptService.getUuidByName(preScriptName, GroovyScriptType.PRE);
                if (!StringUtils.isEmpty(scriptUuid)) {
                    JsonNode param = preScript.path(GroovyScriptField.PARAM);
                    if (!param.isObject()) {
                        param = objectMapper.createObjectNode();
                    }
                    GroovyScriptUtils.runPreScript(scriptUuid, in, (ObjectNode)param, instance, global);
                }
            }
        }
    }
}
