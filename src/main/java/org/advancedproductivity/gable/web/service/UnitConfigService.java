package org.advancedproductivity.gable.web.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface UnitConfigService {

    /**
     * get the unit test's in json
     * because of the cache.the result that got is copied value
     * */
    JsonNode getConfig(String nameSpace, String uuid, String env, String caseId, Integer caseVersion);
}
