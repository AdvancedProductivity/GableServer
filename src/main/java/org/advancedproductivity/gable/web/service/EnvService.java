package org.advancedproductivity.gable.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author zzq
 */
public interface EnvService {

    /**
     * get all env config
     * */
    JsonNode getEnvConfigMenu();

    JsonNode getEnv(String uuid);

    boolean addEnv(String name, ObjectNode config);

    boolean updateEnv(String uuid, String name, ObjectNode config);

    /**
     * handle unit test config json by env config
     * */
    void handleConfig(JsonNode in, JsonNode envConfig);
}
