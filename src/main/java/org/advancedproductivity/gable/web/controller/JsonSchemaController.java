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
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersionDetector;
import com.networknt.schema.ValidationMessage;
import com.saasquatch.jsonschemainferrer.*;
import org.advancedproductivity.gable.framework.config.*;
import org.advancedproductivity.gable.framework.core.TestType;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.framework.utils.PreHandleUtils;
import org.advancedproductivity.gable.framework.utils.jsonschema.ConstFeature;
import org.advancedproductivity.gable.framework.utils.jsonschema.JsonSchemaUtils;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.ExecuteService;
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
            o.remove(ValidateField.VALIDATE);
            o.remove(ConfigField.HISTORY_ID);
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
        JsonSchemaFactory factory = JsonSchemaUtils.getInstance(SpecVersionDetector.detect(schema));
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

    @Resource
    private ExecuteService executeService;

    @PostMapping("/run")
    public Result run(@RequestBody JsonNode jsonNode, @RequestParam String uuid) {
        JsonNode schema = jsonNode.path("schema");
        JsonNode json = jsonNode.path("json");
        return Result.success(executeService.executeJsonSchema(uuid, schema, json));
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

    @PostMapping("/preHandle")
    private Result preHandle(@RequestBody ObjectNode data) {
        JsonNode instance = data.path("instance");
        if (!instance.isObject()) {
            return Result.error("instance must be a object json");

        }
        JsonNode global = data.path("global");
        if (!global.isObject()) {
            return Result.error("global must be a object json");
        }
        JsonNode express = data.path("express");
        PreHandleUtils.preHandleInJson(express, (ObjectNode) instance, (ObjectNode) global);
        return Result.success().setData(express);
    }
}
