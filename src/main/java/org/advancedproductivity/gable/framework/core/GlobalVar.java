package org.advancedproductivity.gable.framework.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author zzq
 */
public class GlobalVar {

    public static ObjectNode globalVar = new ObjectMapper().createObjectNode();
}
