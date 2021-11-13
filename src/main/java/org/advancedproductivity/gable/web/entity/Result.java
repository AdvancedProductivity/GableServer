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

package org.advancedproductivity.gable.web.entity;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

/**
 * @author zzq
 */
public class Result extends ObjectNode {
    private static final ObjectMapper mapper = new ObjectMapper();
    private boolean success = true;

    public Result(JsonNodeFactory nc) {
        super(nc);
    }

    public boolean isSuccess() {
        return success;
    }

    public Result(JsonNodeFactory nc, Map<String, JsonNode> kids) {
        super(nc, kids);
    }

    public static Result success(String message) {
        Result result = new Result(mapper.getNodeFactory());
        result.success = true;
        result.put("result", true)
                .put("message", message);
        return result;
    }

    public static Result success() {
        Result result = new Result(mapper.getNodeFactory());
        result.success = true;
        result.put("result", true);
        return result;
    }

    public static Result success(JsonNode data) {
        Result result = new Result(mapper.getNodeFactory());
        result.success = true;
        result.put("result", true)
                .set("data", data);
        return result;
    }

    public Result setData(JsonNode data) {
        this.set("data", data);
        return this;
    }

    public Result setDataString(String data) {
        this.put("data", data);
        return this;
    }

    public static Result error(String message) {
        Result result = new Result(mapper.getNodeFactory());
        result.success = false;
        result.put("result", false)
                .put("message", message);
        return result;
    }

    public static Result error(String message, JsonNode data) {
        Result result = new Result(mapper.getNodeFactory());
        result.success = false;
        result.put("result", false)
                .put("message", message).set("data", data);
        return result;
    }

    public static Result error() {
        Result result = new Result(mapper.getNodeFactory());
        result.success = false;
        result.put("result", false)
                .put("message", "error");
        return result;
    }

    public ObjectNode emptyArray(String field) {
        set(field, mapper.createArrayNode());
        return this;
    }

    public JsonNode makeFailed() {
        this.put("result", false);
        return this;
    }
}
