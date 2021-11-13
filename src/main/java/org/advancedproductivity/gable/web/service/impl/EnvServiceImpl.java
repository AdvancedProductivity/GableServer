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
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.config.*;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.framework.utils.JsonDiffUtils;
import org.advancedproductivity.gable.web.service.EnvService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zzq
 */
@Service
public class EnvServiceImpl implements EnvService {
    private static final String ENV_FILE_NAME = "env.json";
    private static final String ALL_ENV = "all_env";
    private static final ConcurrentHashMap<String, JsonNode> ENV_HOLDER = new ConcurrentHashMap<>();
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public JsonNode getEnvConfigMenu() {
        JsonNode envs = ENV_HOLDER.get(ALL_ENV);
        if (envs != null) {
            return envs;
        }
        envs = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, ENV_FILE_NAME);
        if (envs == null) {
            envs  = objectMapper.createArrayNode();
            GableFileUtils.saveFile(envs.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, ENV_FILE_NAME);
            ENV_HOLDER.put(ALL_ENV, envs);
            ObjectNode demo = objectMapper.createObjectNode();
            demo.set(CaseField.DIFF_REPLACE, objectMapper.createObjectNode());
            demo.set(CaseField.DIFF_ADD, objectMapper.createObjectNode());
            demo.set(CaseField.DIFF_REMOVE, objectMapper.createObjectNode());
            demo.set(CaseField.DIFF_REMOVE_BY_INDEX, objectMapper.createObjectNode());
            addEnv("demoGenerate", demo);
        }else {
            ENV_HOLDER.put(ALL_ENV, envs);
        }
        return envs;
    }

    @Override
    public JsonNode getEnv(String uuid) {
        JsonNode env = ENV_HOLDER.get(uuid);
        if (env != null) {
            return env.deepCopy();
        }
        env = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.ENV, uuid + ".json");
        if (env == null) {
            env = MissingNode.getInstance();
        }
        ENV_HOLDER.put(uuid, env);
        return env;
    }

    @Override
    public boolean addEnv(String name, ObjectNode config) {
        ArrayNode configs = (ArrayNode) getEnvConfigMenu();
        String uuid = UUID.randomUUID().toString();
        ObjectNode newConfig = config.objectNode();
        newConfig.put(ConfigField.UUID, uuid);
        newConfig.put(ConfigField.ENV_NAME, name);
        configs.add(newConfig);
        GableFileUtils.saveFile(configs.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, ENV_FILE_NAME);
        GableFileUtils.saveFile(config.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.ENV, uuid + ".json");
        ENV_HOLDER.put(uuid, config);
        return true;
    }

    @Override
    public boolean updateEnv(String uuid, String name, ObjectNode config) {
        ArrayNode detailConfig = (ArrayNode) getEnvConfigMenu();
        for (int i1 = 0; i1 < detailConfig.size(); i1++) {
            ObjectNode conf = (ObjectNode) detailConfig.get(i1);
            if (StringUtils.equals(conf.path(ConfigField.UUID).asText(), uuid)) {
                conf.put(ConfigField.ENV_NAME, name);
                GableFileUtils.saveFile(detailConfig.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, ENV_FILE_NAME);
                GableFileUtils.saveFile(config.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.ENV, uuid + ".json");
                ENV_HOLDER.put(uuid, config);
                return true;
            }
        }
        return false;
    }

    /*
    *
    *
        {
            "host_replace": "127.0.0.1",
            "protocol_replace": "http",
            "port_replace": 81,
            "path_pre_append": [
                "a",
                "b"
            ],
            "header_replace_or_add": [
                {
                    "key": "Content-Type",
                    "value": "application/json"
                }
            ],
            "auth_param_replace": [
                {
                    "key": "key",
                    "value": "new Key's value"
                },
                {
                    "key": "key2",
                    "value": "new Key2's value"
                }
            ]
        }
    * */

    @Override
    public void handleConfig(JsonNode in, JsonNode envConfig) {
        if (in == null || envConfig == null) {
            return;
        }
        if (!(in instanceof ObjectNode) || !(envConfig instanceof ObjectNode)) {
            return;
        }
        JsonDiffUtils.doDiffHandle(in.path(ConfigField.DETAIL), envConfig);
        ((ObjectNode) in).put(ConfigField.IS_UNMODIFY, true);
    }

    @Override
    public String getEnvNameByUuid(String envUuid) {
        ArrayNode configs = (ArrayNode) getEnvConfigMenu();
        for (JsonNode config : configs) {
            if (StringUtils.equals(config.path(ConfigField.UUID).asText(), envUuid)) {
                return config.path(ConfigField.ENV_NAME).asText();
            }
        }
        return null;
    }

    private void handleAsHttp(JsonNode detailConfig, ObjectNode env) {
        if (detailConfig == null || !detailConfig.isObject()) {
            return;
        }
        ObjectNode config = (ObjectNode) detailConfig;
        String newHost = env.path(HttpEnvField.HOST_REPLACE).asText();
        if (!StringUtils.isEmpty(newHost)) {
            config.put(ConfigField.HTTP_HOST, newHost);
        }
        String newProtocol = env.path(HttpEnvField.PROTOCOL_REPLACE).asText();
        if (!StringUtils.isEmpty(newProtocol)) {
            config.put(ConfigField.HTTP_PROTOCOL, newProtocol);
        }
        JsonNode pathPreAppend = env.path(HttpEnvField.PATH_PRE_APPEND);
        if (pathPreAppend.isArray() && pathPreAppend.size() > 0) {
            ArrayNode newPathArray = (ArrayNode) pathPreAppend;
            JsonNode originPath = config.path(ConfigField.HTTP_PATH);
            if (originPath.isArray() && originPath.size() > 0) {
                for (int i = 0; i < originPath.size(); i++) {
                    newPathArray.add(originPath.get(i).asText());
                }
            }
            config.set(ConfigField.HTTP_PATH, newPathArray);
        }
        int newPort = env.path(HttpEnvField.PORT_REPLACE).asInt();
        if (newPort != 0) {
            config.put(ConfigField.HTTP_PORT, newPort);
        }

    }


}
