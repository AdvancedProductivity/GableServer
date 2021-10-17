package org.advancedproductivity.gable.framework.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.config.CaseField;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;

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
        JsonNode waifForRemove = diffDefine.path(CaseField.DIFF_REMOVE);
        if (waifForRemove.isObject()) {
            doRemove(in, waifForRemove);
        }
    }

    private static void doRemove(JsonNode in, JsonNode waifForRemove) {
        Iterator<String> fieldNames = waifForRemove.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            if (!StringUtils.startsWith(key, "/")) {
                continue;
            }
            String rootNodeKey = StringUtils.substringBeforeLast(key, "/");
            String aimField = StringUtils.substringAfterLast(key, "/");
            if (StringUtils.isEmpty(rootNodeKey) || StringUtils.isEmpty(aimField)) {
                continue;
            }
            JsonNode at = in.at(rootNodeKey);
            if (at.isObject()) {
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
            if (StringUtils.isEmpty(rootNodeKey) || StringUtils.isEmpty(aimField)) {
                continue;
            }
            JsonNode at = in.at(rootNodeKey);
            if (at.isObject()) {
                ((ObjectNode) at).set(aimField, waifForADD.path(key));
            } else if (at.isArray()) {
                if (StringUtils.isNumeric(aimField)) {
                    ((ArrayNode) at).set(Integer.parseInt(aimField), waifForADD.path(key));
                }else {
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
            if (StringUtils.isEmpty(rootNodeKey) || StringUtils.isEmpty(aimField)) {
                continue;
            }
            JsonNode at = in.at(rootNodeKey);
            if (at.isObject()) {
                ((ObjectNode) at).set(aimField, waifForReplace.path(key));
            }
        }
    }
}
