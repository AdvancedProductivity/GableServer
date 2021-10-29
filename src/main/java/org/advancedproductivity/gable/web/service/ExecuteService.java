package org.advancedproductivity.gable.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author zzq
 */
public interface ExecuteService {

    ObjectNode executeStep(String uuid, JsonNode instance, JsonNode global, JsonNode nextIn, JsonNode lastOut);

    ObjectNode executeJsonSchema(String uuid, JsonNode schema, JsonNode json);

    ObjectNode executeTest(String userId, String uuid, String type, ObjectNode data);
}
