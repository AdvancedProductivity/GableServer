package org.advancedproductivity.gable.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author zzq
 */
public interface JsonSchemaService {

    ArrayNode validate(JsonNode in, JsonNode waitForValidate, String type, ObjectMapper objectMapper);
}
