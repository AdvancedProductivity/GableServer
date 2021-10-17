package org.advancedproductivity.gable.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author zzq
 */
public interface IntegrateService {

    JsonNode list();

    String addIntegrate(ArrayNode records,String name);

    JsonNode getOne(String uuid);

    boolean addTag(String tagName, String uuid);

    boolean updateIntegrate(ArrayNode records, String uuid);
}
