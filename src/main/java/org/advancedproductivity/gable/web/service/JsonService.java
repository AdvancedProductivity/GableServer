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
