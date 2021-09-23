package org.advancedproductivity.gable.web.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.config.ConfigField;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.core.TestType;
import org.advancedproductivity.gable.framework.urils.GableFileUtils;
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
            envs = objectMapper.createArrayNode().add(
                    objectMapper.createObjectNode().put("typeName", TestType.HTTP.name())
                            .set("configs", objectMapper.createArrayNode())
            );
            GableFileUtils.saveFile(envs.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, ENV_FILE_NAME);
        }
        ENV_HOLDER.put(ALL_ENV, envs);
        return envs;
    }

    @Override
    public JsonNode getEnv(String uuid) {
        JsonNode env = ENV_HOLDER.get(uuid);
        if (env != null) {
            return env;
        }
        env = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.ENV, uuid + ".json");
        if (env == null) {
            env = MissingNode.getInstance();
        }
        ENV_HOLDER.put(uuid, env);
        return env;
    }

    @Override
    public boolean addEnv(String type, String name, ObjectNode config) {
        JsonNode envConfigMenu = getEnvConfigMenu();
        for (int i = 0; i < envConfigMenu.size(); i++) {
            JsonNode envGroup = envConfigMenu.get(i);
            if (StringUtils.equals(envGroup.path(ConfigField.ENV_TYPE).asText(), type)) {
                ArrayNode configs = (ArrayNode) envGroup.path("configs");
                String uuid = UUID.randomUUID().toString();
                ObjectNode newConfig = config.objectNode();
                newConfig.put(ConfigField.UUID, uuid);
                newConfig.put(ConfigField.ENV_NAME, name);
                configs.add(newConfig);
                GableFileUtils.saveFile(envConfigMenu.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, ENV_FILE_NAME);
                GableFileUtils.saveFile(config.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.ENV, uuid + ".json");
                ENV_HOLDER.put(uuid, config);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateEnv(String uuid, String name, ObjectNode config) {
        JsonNode envConfigMenu = getEnvConfigMenu();
        for (int i = 0; i < envConfigMenu.size(); i++) {
            ArrayNode detailConfig = (ArrayNode) envConfigMenu.get(i).path("configs");
            for (int i1 = 0; i1 < detailConfig.size(); i1++) {
                ObjectNode conf = (ObjectNode) detailConfig.get(i1);
                if (StringUtils.equals(conf.path(ConfigField.UUID).asText(), uuid)) {
                    conf.put(ConfigField.ENV_NAME, name);
                    GableFileUtils.saveFile(envConfigMenu.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, ENV_FILE_NAME);
                    GableFileUtils.saveFile(config.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.ENV, uuid + ".json");
                    ENV_HOLDER.put(uuid, config);
                    return true;
                }
            }
        }
        return false;
    }


}
