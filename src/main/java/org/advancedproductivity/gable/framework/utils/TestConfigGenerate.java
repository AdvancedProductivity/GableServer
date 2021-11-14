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

package org.advancedproductivity.gable.framework.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.config.ConfigField;
import org.advancedproductivity.gable.framework.core.HttpBodyType;
import org.advancedproductivity.gable.framework.core.HttpMethodType;
import org.advancedproductivity.gable.framework.core.TestType;

/**
 * @author zzq
 */
public class TestConfigGenerate {

    public static ObjectNode httpGenerate(HttpMethodType method, String uuid, ObjectMapper objectMapper) {
        ObjectNode mapperObjectNode = objectMapper.createObjectNode();
        ObjectNode detail = objectMapper.createObjectNode()
                .put(ConfigField.HTTP_METHOD, HttpMethodType.GET.name())
                .put(ConfigField.HTTP_PROTOCOL, "http")
                .put(ConfigField.HTTP_HOST, "")
                .put(ConfigField.HTTP_PORT, 80);
        detail.set(ConfigField.HTTP_PATH, objectMapper.createArrayNode());
        detail.set(ConfigField.HTTP_QUERY, objectMapper.createArrayNode());
        detail.set(ConfigField.HTTP_BODY, objectMapper.createObjectNode()
                .put(ConfigField.HTTP_BODY_TYPE, HttpBodyType.NONE.name())
                .put(ConfigField.HTTP_BODY_CONTENT, "")
        );
        detail.set(ConfigField.HTTP_HEADER, objectMapper.createArrayNode());
        detail.set(ConfigField.HTTP_HEADER, objectMapper.createArrayNode());
//        detail.set(ConfigField.HTTP_AUTH, objectMapper.createObjectNode()
//                .put(ConfigField.HTTP_AUTH_TYPE, HttpAuthType.NONE.name())
//        );
        ObjectNode config = mapperObjectNode.put(ConfigField.UUID, uuid)
                .put(ConfigField.TEST_TYPE, TestType.HTTP.name())
                .put(ConfigField.VERSION, 1)
                .set(ConfigField.DETAIL, detail);
        return config;
    }

    public static ObjectNode groovyGenerate(ObjectMapper objectMapper,String uuid) {
        return objectMapper.createObjectNode().put(ConfigField.UUID, uuid)
                .put(ConfigField.TEST_TYPE, TestType.GROOVY_SCRIPT.name())
                .put(ConfigField.VERSION, 1)
                .set(ConfigField.DETAIL, objectMapper.createObjectNode());
    }
}
