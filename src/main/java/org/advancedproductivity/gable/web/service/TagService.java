package org.advancedproductivity.gable.web.service;

import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author zzq
 */
public interface TagService {
    boolean addTagForIntegrateTest(String testUuid, String tagName);

    ArrayNode getTestByTag(String tagName);
}
