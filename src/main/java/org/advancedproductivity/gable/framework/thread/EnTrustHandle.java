package org.advancedproductivity.gable.framework.thread;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author zzq
 */
public interface EnTrustHandle {
    int takeHistory(String uuid, ArrayNode history);

    void recordHistory(String server, int historyId, String uuid, ArrayNode define);

    ObjectNode getConfig(String nameSpace, String uuid, String env, String caseId, Integer caseVersion);

    ObjectNode runStep(String uuid, JsonNode instance, JsonNode global, JsonNode nextIn, JsonNode lastOut);

    ObjectNode runJsonScheam(String uuid, JsonNode lastOut, JsonNode schema);

    ObjectNode runTest(String nameSpace, String uuid, String type, ObjectNode data);
}
