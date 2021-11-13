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

package org.advancedproductivity.gable.framework.thread;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author zzq
 */
public interface EnTrustHandle {
    int takeHistory(String uuid, ArrayNode history);

    /**
     * @return is no error (all succeed or ignored)
     * */
    boolean recordHistory(String server, int historyId, String uuid, ArrayNode define);

    ObjectNode getConfig(String nameSpace, String uuid, String env, String caseId, Integer caseVersion);

    ObjectNode runStep(String uuid, JsonNode instance, JsonNode global, JsonNode nextIn, JsonNode lastOut);

    ObjectNode runJsonScheam(String uuid, JsonNode lastOut, JsonNode schema);

    ObjectNode runTest(String nameSpace, String uuid, String type, ObjectNode data);
}
