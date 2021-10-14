package org.advancedproductivity.gable.framework.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;

/**
 * @author zzq
 */
public class GlobalVar {

    public static ObjectNode globalVar = null;

    static{
        try {
            globalVar = (ObjectNode) GableFileUtils.readFileAsJson(GableConfig.getGablePath(),
                    GableConfig.PUBLIC_PATH, "global.json");
        } catch (Exception e) {
            globalVar = new ObjectMapper().createObjectNode();
        }
        if (globalVar == null) {
            globalVar = new ObjectMapper().createObjectNode();
        }
    }
}
