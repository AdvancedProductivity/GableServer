package org.advancedproductivity.gable.web.service;

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
    public int recordIntegrateTest(String nameSpace, String uuid, String content);
}
