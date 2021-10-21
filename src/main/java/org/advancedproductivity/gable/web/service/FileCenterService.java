package org.advancedproductivity.gable.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author zzq
 */
public interface FileCenterService {

    void addFile(ObjectNode fileInfo);

    JsonNode list();

    int delete(String uuid);
}
