/*
 *  Copyright (c) 2021 AdvancedProductivity
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.advancedproductivity.gable.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
