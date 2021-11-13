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
import org.advancedproductivity.gable.framework.config.GroovyScriptField;
import org.advancedproductivity.gable.framework.config.GroovyScriptType;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.groovy.GroovyScriptUtils;
import org.advancedproductivity.gable.framework.groovy.GroovyType;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.service.GroovyScriptService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @author zzq
 */
@Service
@Slf4j
public class GroovyScriptServiceImpl implements GroovyScriptService {
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public String getSampleScript(String namespace) {
        String scriptContent = GableFileUtils.readFileAsString(GableConfig.getGablePath(), namespace, UserDataType.GROOVY, GroovyType.SAMPLE_TYPE);
        if (StringUtils.isEmpty(scriptContent)) {
            scriptContent = "def a = 10\nassert a == 100";
            GableFileUtils.saveFile(scriptContent, GableConfig.getGablePath(), namespace, UserDataType.GROOVY, GroovyType.SAMPLE_TYPE);
        }
        return scriptContent;
    }

    @Override
    public boolean saveSampleScript(String namespace, String scriptContent) {
        return GableFileUtils.saveFile(scriptContent, GableConfig.getGablePath(), namespace, UserDataType.GROOVY, GroovyType.SAMPLE_TYPE);
    }

    @Override
    public JsonNode getScriptList(GroovyScriptType type) {
        JsonNode jsonNode = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, type.name() + ".json");
        if (jsonNode == null) {
            ArrayNode arrayNode = objectMapper.createArrayNode();
            arrayNode.add(objectMapper.createObjectNode()
                    .put(GroovyScriptField.GROUP_NAME, "Default Group")
                    .put(GroovyScriptField.UUID, UUID.randomUUID().toString())
                    .set(GroovyScriptField.ITEM, objectMapper.createArrayNode())
            );
            jsonNode = arrayNode;
            GableFileUtils.saveFile(jsonNode.toPrettyString(),GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, type.name() + ".json");
        }
        return jsonNode;
    }

    @Override
    public JsonNode addGroup(GroovyScriptType type, String groupName) {
        ArrayNode menuList = (ArrayNode) getScriptList(type);
        menuList.add(objectMapper.createObjectNode()
                .put(GroovyScriptField.GROUP_NAME, groupName)
                .put(GroovyScriptField.UUID, UUID.randomUUID().toString())
                .set(GroovyScriptField.ITEM, objectMapper.createArrayNode())
        );
        GableFileUtils.saveFile(menuList.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, type.name() + ".json");
        return menuList;
    }

    @Override
    public boolean haveExist(GroovyScriptType type, String scriptName) {
        ArrayNode menuList = (ArrayNode) getScriptList(type);
        for (JsonNode jsonNode : menuList) {
            JsonNode items = jsonNode.path(GroovyScriptField.ITEM);
            if (items.isArray()) {
                for (JsonNode item : items) {
                    if (StringUtils.equals(item.path(GroovyScriptField.NAME).asText(), scriptName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public JsonNode addItem(GroovyScriptType type, String groupUuid, String scriptName, String code) {
        ArrayNode menuList = (ArrayNode) getScriptList(type);
        String uuid = type.name() + "_" + UUID.randomUUID().toString();
        boolean success = false;
        for (JsonNode jsonNode : menuList) {
            if (StringUtils.equals(jsonNode.path(GroovyScriptField.UUID).asText(), groupUuid)) {
                JsonNode items = jsonNode.path(GroovyScriptField.ITEM);
                ArrayNode arrays = null;
                if (!items.isArray()) {
                    continue;
                }else {
                    arrays = (ArrayNode) items;
                }
                arrays.add(objectMapper.createObjectNode()
                        .put(GroovyScriptField.NAME, scriptName)
                        .put(GroovyScriptField.UUID, uuid)
                );
                success = true;
                break;
            }
        }
        if (success) {
            GableFileUtils.saveFile(menuList.toPrettyString(), GableConfig.getGablePath(),
                    GableConfig.PUBLIC_PATH, type.name() + ".json");
            GableFileUtils.saveFile(code, GableConfig.getGablePath(),
                    GableConfig.PUBLIC_PATH, UserDataType.GROOVY, uuid + ".groovy");

        }else {
            log.error("not add to script menu list {} {}", groupUuid, scriptName);
        }
        return menuList;
    }

    @Override
    public String readCode(String uuid) {
        return GableFileUtils.readFileAsString(GableConfig.getGablePath(),
                GableConfig.PUBLIC_PATH, UserDataType.GROOVY, uuid + ".groovy");
    }

    @Override
    public void updateScript(String uuid, String code) {
        GableFileUtils.saveFile(code, GableConfig.getGablePath(),
                GableConfig.PUBLIC_PATH, UserDataType.GROOVY, uuid + ".groovy");
    }

    @Override
    public JsonNode executePreScript(String uuid, ObjectNode param) {
        JsonNode in = param.path("in");
        JsonNode instance = param.path("instance");
        JsonNode global = param.path("global");
        JsonNode paramJson = param.path("param");
        GroovyScriptUtils.runPreScript(uuid, in, (ObjectNode) paramJson, instance, global);
        return param;
    }

    @Override
    public JsonNode executePostScript(String uuid, ObjectNode param) {
        JsonNode out = param.path("out");
        JsonNode instance = param.path("instance");
        JsonNode global = param.path("global");
        JsonNode paramJson = param.path("param");
        GroovyScriptUtils.runPostScript(uuid, out, (ObjectNode) paramJson, instance, global);
        return param;
    }

    @Override
    public String getUuidByName(String preScriptName, GroovyScriptType type) {
        ArrayNode menuList = (ArrayNode) getScriptList(type);
        for (JsonNode jsonNode : menuList) {
            JsonNode items = jsonNode.path(GroovyScriptField.ITEM);
            if (items.isArray()) {
                for (JsonNode item : items) {
                    if (StringUtils.equals(item.path(GroovyScriptField.NAME).asText(), preScriptName)) {
                        return item.path(GroovyScriptField.UUID).asText();
                    }
                }
            }
        }
        return null;
    }
}
