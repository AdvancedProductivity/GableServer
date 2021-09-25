package org.advancedproductivity.gable.framework.auth;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author zzq
 */
public interface AuthHandler {

    void handle(JsonNode in, JsonNode instance, JsonNode global);

    AuthType getAuthType();
}
