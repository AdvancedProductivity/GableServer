package org.advancedproductivity.gable.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.web.entity.Result;

import javax.servlet.ServletOutputStream;

/**
 * @author zzq
 */
public interface CaseService {

    /**
     * get all test case
     * @param nameSpace user's id
     * @param testUuid test's id
     * @return case list
     * */
    JsonNode getAllCase(String nameSpace, String testUuid);

    ObjectNode saveCases(ArrayNode cases, String nameSpace, String uuid);

    ObjectNode getCase(String nameSpace, String uuid, Integer version, String caseId);

    boolean updateCase(String nameSpace, String uuid, Integer version, String caseId, ObjectNode diffAndValidate);

    void handleCase(JsonNode in, ObjectNode caseDetail);

    void saveToExcel(ServletOutputStream out, JsonNode allCase);

    JsonNode generateDemoCase();

    ObjectNode genDefaultDiffJson();
}
