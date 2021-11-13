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

package org.advancedproductivity.gable.framework.utils.jsonschema;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.saasquatch.jsonschemainferrer.GenericSchemaFeature;
import com.saasquatch.jsonschemainferrer.GenericSchemaFeatureInput;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zzq
 * add const for if the json value is Success
 */
public class ConstFeature implements GenericSchemaFeature {
    private ObjectMapper mapper;
    private static final String STRING_TYPE = "string";
    private static final String INTEGER_TYPE = "integer";
    private static final String CONST_NODE = "const";
    private static final String ENUM_NODE = "enum";
    private static final Set<String> constEnums = new HashSet<>();
    private static final String SUCCESS_VALUE = "Success";
    private static final String FAILURE_VALUE = "Failure";

    static {
        // you can custom the field you want generate const for jsonschema here
        constEnums.add(SUCCESS_VALUE);
        constEnums.add(FAILURE_VALUE);
    }

    public ConstFeature(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    @Nullable
    @Override
    public ObjectNode getFeatureResult(@NotNull GenericSchemaFeatureInput input) {
        if (constEnums.size() == 0) {
            return null;
        }
        if (!StringUtils.equals(STRING_TYPE, input.getType()) && !StringUtils.equals(INTEGER_TYPE, input.getType())) {
            return null;
        } else {
            Collection<? extends JsonNode> samples = input.getSamples();
            if (samples.isEmpty()) {
                return null;
            }
            ObjectNode result = getMapper().createObjectNode();
            if (samples.size() == 1) {
                input.getSamples().stream().findFirst().ifPresent((item) -> {
                    if (item.isInt()) {
                        result.put(CONST_NODE, item.asInt());
                    } else {
                        String theValue = item.asText();
                        if (constEnums.contains(theValue)) {
                            result.put(CONST_NODE, theValue);
                        }
                    }
                });
            } else {
                ArrayNode arrayNode = mapper.createArrayNode();
                for (JsonNode item : samples) {
                    if (item.isInt()) {
                        arrayNode.add(item.asInt());
                    } else {
                        arrayNode.add(item.asText());
                    }
                }
                result.set(ENUM_NODE, arrayNode);
            }
            return result;
        }
    }
}
