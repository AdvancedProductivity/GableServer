package org.advancedproductivity.gable.framework.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.config.ConfigField;
import org.advancedproductivity.gable.framework.core.HttpAuthType;
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
        detail.set(ConfigField.HTTP_AUTH, objectMapper.createObjectNode()
                .put(ConfigField.HTTP_AUTH_TYPE, HttpAuthType.NONE.name())
        );
        ObjectNode config = mapperObjectNode.put(ConfigField.UUID, uuid)
                .put(ConfigField.TEST_TYPE, TestType.HTTP.name())
                .put(ConfigField.VERSION, 1)
                .set(ConfigField.DETAIL, detail);
        return config;
    }
}
