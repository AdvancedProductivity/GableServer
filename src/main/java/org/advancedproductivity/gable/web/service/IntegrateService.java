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
import org.advancedproductivity.gable.web.entity.Result;

/**
 * @author zzq
 */
public interface IntegrateService {

    JsonNode list();

    String addIntegrate(ArrayNode records, String name);

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

    Result stopEntrustRun(String uuid);
}
