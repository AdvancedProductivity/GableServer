package org.advancedproductivity.gable.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Iterator;

/**
 * @author zzq
 */
@Service
@Slf4j
public class JsonService {

    public void traverFields(JsonNode in, ObjectNode holder, String prefix) {
        if (in.isArray()) {
            for (int i = 0; i < in.size(); i++) {
                traverFields(in.get(i), holder, prefix + "/" + i);
            }
        } else if (in.isObject()) {
            Iterator<String> fieldNames = in.fieldNames();
            while (fieldNames.hasNext()) {
                String field = fieldNames.next();
                JsonNode newNode = in.path(field);
                traverFields(newNode, holder, prefix + "/" + field);
            }
        } else if (in.isValueNode()) {
            holder.set(prefix, in);
        }else {
            log.error("unknown handle json, {}", in.toString());
        }
    }
}
