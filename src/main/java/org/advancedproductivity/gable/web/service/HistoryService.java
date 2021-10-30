package org.advancedproductivity.gable.web.service;

import com.fasterxml.jackson.databind.JsonNode;
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
