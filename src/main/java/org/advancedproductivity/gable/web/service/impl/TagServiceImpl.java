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

package org.advancedproductivity.gable.web.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.service.TagService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class TagServiceImpl implements TagService {
    private static final String TAG_INTEGRATE = "tags_integrate.json";

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean addTagForIntegrateTest(String testUuid, String tagName) {
        JsonNode originTag = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, TAG_INTEGRATE);
        if (originTag == null || !originTag.isObject()) {
            originTag = objectMapper.createObjectNode();
        }
        ObjectNode tag = (ObjectNode) originTag;
        if (tag.has(tagName)) {
            JsonNode tagItems = tag.path(tagName);
            if (tagItems.isArray()) {
                ArrayNode testUuids = ((ArrayNode) tagItems);
                for (JsonNode uuid : testUuids) {
                    if (StringUtils.equals(uuid.asText(), testUuid)) {
                        return true;
                    }
                }
                testUuids.add(testUuid);
            } else {
                tagItems = objectMapper.createArrayNode();
                ((ArrayNode) tagItems).add(testUuid);
                tag.set(tagName, tagItems);
            }
        }else {
            ArrayNode tags = objectMapper.createArrayNode();
            tags.add(testUuid);
            tag.set(tagName, tags);
        }
        return GableFileUtils.saveFile(tag.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, TAG_INTEGRATE);
    }

    @Override
    public ArrayNode getTestByTag(String tagName) {
        JsonNode originTag = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, TAG_INTEGRATE);
        JsonNode tests = originTag.path(tagName);
        if (tests.isArray()) {
            return (ArrayNode) tests;
        }else {
            return objectMapper.createArrayNode();
        }
    }
}
