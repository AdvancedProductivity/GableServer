package org.advancedproductivity.gable.framework.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.config.CaseField;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author zzq
 */
public class JsonDiffUtils {

    public static void doDiffHandle(JsonNode in, JsonNode diffDefine) {
        JsonNode waifForReplace = diffDefine.path(CaseField.DIFF_REPLACE);
        if (waifForReplace.isObject()) {
            doReplace(in, waifForReplace);
        }
        JsonNode waifForAadd = diffDefine.path(CaseField.DIFF_ADD);
        if (waifForAadd.isObject()) {
            doAdd(in, waifForAadd);
        }
        JsonNode waifForRemoveOfObj = diffDefine.path(CaseField.DIFF_REMOVE);
        if (waifForRemoveOfObj.isObject()) {
            doRemoveOfObj(in, waifForRemoveOfObj);
        }
        JsonNode waifForRemoveOfArray = diffDefine.path(CaseField.DIFF_REMOVE_BY_INDEX);
        if (waifForRemoveOfArray.isObject()) {
            doRemoveOfArray(in, waifForRemoveOfArray);
        }
    }

    /**
     * "removeByIndex" :  {
     *     "/field/arrayField": [
     *          0,
     *          1,
     *          2
     *     ]
     * }
     *
     * */
    private static void doRemoveOfArray(JsonNode in, JsonNode waifForRemoveOfArray) {
        if (!waifForRemoveOfArray.isObject()) {
            return;
        }
        ArrayNode tmpArray = ((ObjectNode) waifForRemoveOfArray).arrayNode();
        Iterator<String> fieldNames = waifForRemoveOfArray.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            JsonNode array = in.at(key);
            JsonNode removeArrayIndex = waifForRemoveOfArray.path(key);
            if (!removeArrayIndex.isArray() || !array.isArray()) {
                continue;
            }
            Set<Integer> waitForRemoveIndex = new HashSet<>();
            for (JsonNode arrayIndex : removeArrayIndex) {
                if (arrayIndex.isInt()) {
                    waitForRemoveIndex.add(arrayIndex.asInt());
                }
            }
            tmpArray.removeAll();
            ArrayNode newArray = (ArrayNode) array;
            for (int i = 0; i < newArray.size(); i++) {
                if (waitForRemoveIndex.contains(i)) {
                    continue;
                }
                tmpArray.add(newArray.get(i));
            }
            newArray.removeAll();
            for (int i = 0; i < tmpArray.size(); i++) {
                newArray.add(tmpArray.get(i));
            }
        }
    }

    private static void doRemoveOfObj(JsonNode in, JsonNode waifForRemove) {
        Iterator<String> fieldNames = waifForRemove.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            if (!StringUtils.startsWith(key, "/")) {
                continue;
            }
            String rootNodeKey = StringUtils.substringBeforeLast(key, "/");
            String aimField = StringUtils.substringAfterLast(key, "/");
            if (StringUtils.isEmpty(aimField)) {
                continue;
            }
            JsonNode at = null;
            if (StringUtils.isEmpty(rootNodeKey)) {
                at = in;
            }else {
                at = in.at(rootNodeKey);
            }
            if (at == null) {
            } else if (at.isObject()) {
                ((ObjectNode) at).remove(aimField);
            } else if (at.isArray()) {
                if (StringUtils.isNumeric(aimField)) {
                    ((ArrayNode) at).remove(Integer.parseInt(aimField));
                }
            }
        }
    }

    private static void doAdd(JsonNode in, JsonNode waifForADD) {
        Iterator<String> fieldNames = waifForADD.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            if (!StringUtils.startsWith(key, "/")) {
                continue;
            }
            String rootNodeKey = StringUtils.substringBeforeLast(key, "/");
            String aimField = StringUtils.substringAfterLast(key, "/");
            if (StringUtils.isEmpty(aimField)) {
                continue;
            }
            JsonNode at = null;
            if (StringUtils.isEmpty(rootNodeKey)) {
                at = in;
            }else {
                at = in.at(rootNodeKey);
            }
            if (at == null) {
            } else if (at.isObject()) {
                ((ObjectNode) at).set(aimField, waifForADD.path(key));
            } else if (at.isArray()) {
                if (StringUtils.isNumeric(aimField)) {
                    ((ArrayNode) at).set(Integer.parseInt(aimField), waifForADD.path(key));
                } else {
                    ((ArrayNode) at).add(waifForADD.path(key));
                }
            }
        }
    }

    private static void doReplace(JsonNode in, JsonNode waifForReplace) {
        Iterator<String> fieldNames = waifForReplace.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            if (!StringUtils.startsWith(key, "/")) {
                continue;
            }
            String rootNodeKey = StringUtils.substringBeforeLast(key, "/");
            String aimField = StringUtils.substringAfterLast(key, "/");
            if (StringUtils.isEmpty(aimField)) {
                continue;
            }
            JsonNode at = null;
            if (StringUtils.isEmpty(rootNodeKey)) {
                at = in;
            }else {
                at = in.at(rootNodeKey);
            }
            if (at != null && at.isObject()) {
                ((ObjectNode) at).set(aimField, waifForReplace.path(key));
            }
        }
    }
}
