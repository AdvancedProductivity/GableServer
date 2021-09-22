package org.advancedproductivity.gable.framework.runner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.core.TestType;

/**
 * @author zzq
 */
public interface TestAction {

    /**
     * execute unit test
     * @param in in param
     * @param out out param
     * */
    void execute(JsonNode in, JsonNode out);

    /**
     * implement by sub class
     * @return the Test Type
     * */
    TestType getTestType();
}
