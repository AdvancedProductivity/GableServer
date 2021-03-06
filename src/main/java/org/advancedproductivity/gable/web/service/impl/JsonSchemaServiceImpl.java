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
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersionDetector;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.HttpResponseField;
import org.advancedproductivity.gable.framework.config.ValidateField;
import org.advancedproductivity.gable.framework.core.TestType;
import org.advancedproductivity.gable.framework.utils.jsonschema.JsonSchemaUtils;
import org.advancedproductivity.gable.web.service.JsonSchemaService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author zzq
 */
@Slf4j
@Service
public class JsonSchemaServiceImpl implements JsonSchemaService {

    @Override
    public ArrayNode validate(JsonNode in, JsonNode waitForValidate, String type, ObjectMapper objectMapper) {
        ArrayNode result = objectMapper.createArrayNode();
        JsonNode jsonSchemaNode = in.at("/" + ValidateField.VALIDATE + "/" + ValidateField.JSON_SCHEMA);
        if (jsonSchemaNode == null || jsonSchemaNode.isMissingNode()) {
            return result;
        }
        if (StringUtils.equals(type, TestType.HTTP.name())) {
            waitForValidate = waitForValidate.path(HttpResponseField.CONTENT);
        }
        JsonSchema schema = null;
        if (jsonSchemaNode.isTextual()) {
            try {
                JsonNode node = objectMapper.readTree(jsonSchemaNode.asText());
                if (!node.isEmpty()) {
                    JsonSchemaFactory factory = JsonSchemaUtils.getInstance(SpecVersionDetector.detect(node));
                    schema = factory.getSchema(node);
                }
            } catch (Exception e) {
                log.error("error happens while parser json schema string", e);
            }
        } else if (jsonSchemaNode.isObject() && !jsonSchemaNode.isEmpty()) {
            try {
                JsonSchemaFactory factory = JsonSchemaUtils.getInstance(SpecVersionDetector.detect(jsonSchemaNode));
                schema = factory.getSchema(jsonSchemaNode);
            } catch (Exception e) {
                log.error("parser json schema error", e);
            }
        }
        if (schema != null) {
            Set<ValidationMessage> validate = schema.validate(waitForValidate);
            if (!validate.isEmpty()) {
                for (ValidationMessage validationMessage : validate) {
                    result.add(validationMessage.getMessage());
                }
            }
        }
        return result;
    }
}
