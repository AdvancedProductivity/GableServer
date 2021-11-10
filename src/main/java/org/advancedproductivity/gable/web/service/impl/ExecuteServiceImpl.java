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
import org.advancedproductivity.gable.framework.config.ConfigField;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.IntegrateField;
import org.advancedproductivity.gable.framework.config.ValidateField;
import org.advancedproductivity.gable.framework.core.GlobalVar;
import org.advancedproductivity.gable.framework.groovy.GroovyScriptUtils;
import org.advancedproductivity.gable.framework.runner.GroovyCodeRunner;
import org.advancedproductivity.gable.framework.runner.RunnerHolder;
import org.advancedproductivity.gable.framework.runner.TestAction;
import org.advancedproductivity.gable.framework.utils.PreHandleUtils;
import org.advancedproductivity.gable.framework.utils.jsonschema.JsonSchemaUtils;
import org.advancedproductivity.gable.web.service.ExecuteService;
import org.advancedproductivity.gable.web.service.HistoryService;
import org.advancedproductivity.gable.web.service.JsonSchemaService;
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
        PreHandleUtils.preHandleInJson(in, instance, global);
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
        history.set(IntegrateField.IN, originIn);
        history.set(IntegrateField.OUT, out);
        history.set(IntegrateField.INSTANCE, instance);
        history.set(IntegrateField.GLOBAL, global);
        history.put(ConfigField.TEST_TYPE, type);
        history.put("recordTime", System.currentTimeMillis());
        int historyId = historyService.recordUnitTest(userId, uuid, history.toPrettyString());
        out.put(ConfigField.HISTORY_ID, historyId);
        return out;
    }
}
