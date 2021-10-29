package org.advancedproductivity.gable.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.web.entity.Result;

/**
 * @author zzq
 */
public interface IntegrateService {

    JsonNode list();

    String addIntegrate(ArrayNode records,String name);

    JsonNode getIntegrateDefine(String uuid);

    boolean addTag(String tagName, String uuid);

    boolean updateIntegrate(ArrayNode records, String uuid);

    /**
     * remove integrate test
     * @param uuid integrate’s uuid
     * @return remove count
     * */
    int delete(String uuid);

    /**
     * get item of the integrate list
     * @param uuid uuid of item
     * @return the define name uuid and status
     * */
    ObjectNode getItem(String uuid);

    Result entrustRun(String uuid, String env, String server);
}
