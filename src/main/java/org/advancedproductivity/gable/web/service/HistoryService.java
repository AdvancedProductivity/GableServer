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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author zzq
 */
public interface HistoryService {

    /**
     * record the run history log
     * @param nameSpace user info
     * @param content log content
     * @param uuid test uuid
     * @return log id
     * */
    public int recordUnitTest(String nameSpace, String uuid, String content);

    public int recordGroovy(String nameSpace, String uuid, String content);

    public int recordJsonSchemaStep(String nameSpace, String uuid, String content);

    public int recordIntegrateTest(String nameSpace, String uuid, String content);

    public boolean recordIntegrateTest(int historyId, String nameSpace, String uuid, String content);

    ObjectNode analysis(ArrayNode records, String server, String uuid);

    void indexHistory(String uuid, int historyId, boolean noError, String origin, String startAt, String endAt);

    ArrayNode readOverviewHistory(String uuid);
}
