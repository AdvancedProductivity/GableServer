package org.advancedproductivity.gable.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersionDetector;
import com.networknt.schema.ValidationMessage;
import com.saasquatch.jsonschemainferrer.*;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.HttpResponseField;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.config.ValidateField;
import org.advancedproductivity.gable.framework.core.TestType;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.framework.utils.jsonschema.ConstFeature;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.HistoryService;
import org.advancedproductivity.gable.web.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author zzq
 */
@RestController
@RequestMapping("/api/jsonSchema")
public class JsonSchemaController {
    private static final String JSON_SCHEMA_FILE_NAME = "jsonSchemaRecord.json";
    public static final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
            .setSpecVersion(SpecVersion.DRAFT_07)
            // Requires commons-validator
            .setAdditionalPropertiesPolicy(AdditionalPropertiesPolicies.notAllowed())
            .setRequiredPolicy(RequiredPolicies.nonNullCommonFields())
            .setArrayLengthFeatures(EnumSet.allOf(ArrayLengthFeature.class))
            .addGenericSchemaFeatures(new ConstFeature(new ObjectMapper()))
            .addEnumExtractors(EnumExtractors.validEnum(java.time.Month.class),
                    EnumExtractors.validEnum(java.time.DayOfWeek.class))
            .build();

    @PostMapping
    public Result generate(@RequestBody JsonNode in, @RequestParam String type) {
        if (in.isObject()) {
            ObjectNode o = ((ObjectNode) in);
            o.remove("validate");
            o.remove("historyId");
        }
        if (StringUtils.equals(TestType.HTTP.name(), type)) {
            JsonNode body = in.path(HttpResponseField.CONTENT);
            if (body.isMissingNode() || body.isNull()) {
                return Result.success(inferrer.inferForSample(body));
            }
            return Result.success(inferrer.inferForSample(body));
        }
        return Result.success(inferrer.inferForSample(in));
    }

    @PostMapping("just")
    public Result justGenerate(@RequestBody JsonNode in) {
        return Result.success(inferrer.inferForSample(in));
    }

    @Resource
    private UserService userService;

    @Resource
    private HttpServletRequest request;

    @Resource
    private ObjectMapper objectMapper;

    @GetMapping
    public Result getConfig() {
        String userId = userService.getUserId(null, request);
        JsonNode node = null;
        if (!StringUtils.isEmpty(userId)) {
            node = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), userId, JSON_SCHEMA_FILE_NAME);
        }
        if (node == null) {
            return Result.error();
        }
        return Result.success(node);
    }

    @PutMapping
    public Result validate(@RequestBody JsonNode jsonNode) {
        JsonNode schema = jsonNode.path("schema");
        JsonNode json = jsonNode.path("json");
        String userId = userService.getUserId(null, request);
        if (StringUtils.isEmpty(userId)) {
            return Result.error("validate error");
        }
        if (!schema.isObject()) {
            return Result.error("JsonSchema format error");
        }
        if (json.isMissingNode()) {
            return Result.error("json格式不正确");
        }
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(schema));
        JsonSchema validator = factory.getSchema(schema);
        Set<ValidationMessage> validate = validator.validate(json);
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (ValidationMessage validationMessage : validate) {
            arrayNode.add(validationMessage.getMessage());
        }
        GableFileUtils.saveFile(jsonNode.toPrettyString(), GableConfig.getGablePath(), userId, JSON_SCHEMA_FILE_NAME);
        return Result.success(arrayNode);
    }

    @Resource
    private HistoryService historyService;

    @PostMapping("/run")
    public Result run(@RequestBody JsonNode jsonNode, @RequestParam String uuid) {
        JsonNode schema = jsonNode.path("schema");
        JsonNode json = jsonNode.path("json");
        ObjectNode resp = objectMapper.createObjectNode();
        ArrayNode jsonSchemaError = objectMapper.createArrayNode();
        ObjectNode validateResult = objectMapper.createObjectNode();
        if (!schema.isObject()) {
            jsonSchemaError.add("JsonSchema format error");
            validateResult.put(ValidateField.RESULT, false);
        }
        if (json.isMissingNode()) {
            jsonSchemaError.add("JsonSchema format error");
            validateResult.put(ValidateField.RESULT, false);
        }
        if (jsonSchemaError.size() == 0) {
            try {
                JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(schema));
                JsonSchema validator = factory.getSchema(schema);
                Set<ValidationMessage> validate = validator.validate(json);
                for (ValidationMessage validationMessage : validate) {
                    jsonSchemaError.add(validationMessage.getMessage());
                }
                validateResult.put(ValidateField.RESULT, true);
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
        resp.put("historyId", historyId);
        return Result.success(resp);
    }

    @GetMapping("/history")
    private Result getHistory(@RequestParam String uuid, @RequestParam Integer historyId,
                              @RequestParam(required = false) Boolean isPublic) {
        String userId = userId = userService.getUserId(isPublic, request);
        JsonNode node = GableFileUtils.readFileAsJson(GableConfig.getGablePath(),
                userId, UserDataType.JSON_SCHEMA_HIS,
                uuid,
                UserDataType.HISTORY,
                historyId + ".json");
        return Result.success().setData(node);
    }
}
