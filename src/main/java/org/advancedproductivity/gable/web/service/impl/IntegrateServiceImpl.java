package org.advancedproductivity.gable.web.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.IntegrateField;
import org.advancedproductivity.gable.framework.config.IntegrateStepStatus;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.thread.IntegrateEnTrustRun;
import org.advancedproductivity.gable.framework.thread.ThreadListener;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.entity.Result;
import org.advancedproductivity.gable.web.service.IntegrateService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zzq
 */
@Service
@Slf4j
public class IntegrateServiceImpl implements IntegrateService {
    private static final String INTEGRATE_TEST_FILE = "integrate.json";
    private static final String MENU = "LIST";
    private static final Map<String, IntegrateEnTrustRun> EN_TRUST_RUN_MAP = new ConcurrentHashMap<>();
    private static final Cache<String, JsonNode> CACHE = CacheBuilder.newBuilder()
            .maximumSize(1024)
            .removalListener(new RemovalListener<Object, Object>() {
                @Override
                public void onRemoval(RemovalNotification<Object, Object> notification) {
                    log.info("remove integrate cache key: {}", notification.getKey().toString());
                }
            })
            .build();

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public ArrayNode list() {
        JsonNode integrateList = CACHE.getIfPresent(MENU);
        if (integrateList != null) {
            return (ArrayNode) integrateList;
        }
        return readFromFile(true);
    }

    private ArrayNode readFromFile(boolean isCache) {
        JsonNode integrateList = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, INTEGRATE_TEST_FILE);
        if (integrateList == null) {
            integrateList = objectMapper.createArrayNode();
            GableFileUtils.saveFile(integrateList.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, INTEGRATE_TEST_FILE);
        }
        ArrayNode newMenu = integrateList.deepCopy();
        for (JsonNode menu : newMenu) {
            ((ObjectNode) menu).put(IntegrateField.STATUS, IntegrateStepStatus.NOT_RUN.getValue());
        }
        if (isCache) {
            log.info("set integrate menu from file");
            CACHE.put(MENU, newMenu);
        }
        return newMenu;
    }

    @Override
    public String addIntegrate(ArrayNode records, String name) {
        String uuid = UUID.randomUUID().toString();
        for (JsonNode record : records) {
            handleUuid(record);
        }
        GableFileUtils.saveFile(records.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.INTEGRATE, uuid, "define.json");
        JsonNode node = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, INTEGRATE_TEST_FILE);
        ObjectNode integrateItem = objectMapper.createObjectNode();
        integrateItem.put(IntegrateField.UUID, uuid);
        integrateItem.put(IntegrateField.NAME, name);
        integrateItem.set(IntegrateField.TAG, objectMapper.createArrayNode());
        if (node == null) {
            node = objectMapper.createArrayNode().add(integrateItem);
        }else {
            ((ArrayNode) node).add(integrateItem);
        }
        GableFileUtils.saveFile(node.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, INTEGRATE_TEST_FILE);
        JsonNode cacheMenu = CACHE.getIfPresent(MENU);
        ObjectNode addItem = integrateItem.deepCopy();
        addItem.put(IntegrateField.STATUS, IntegrateStepStatus.NOT_RUN.getValue());
        if (cacheMenu == null) {
            cacheMenu = objectMapper.createArrayNode().add(addItem);
            log.info("update integrate while add integrate");
            CACHE.put(MENU, cacheMenu);
        }else {
            ((ArrayNode) cacheMenu).add(addItem);
        }
        return uuid;
    }

    private void handleUuid(JsonNode record) {
        String type = record.path(IntegrateField.TYPE).asText();
        if (StringUtils.equals(type, IntegrateField.STEP_TYPE)) {
            JsonNode codeNode = record.path(IntegrateField.CODE);
            if (codeNode.isMissingNode() || codeNode.isNull()) {
                return;
            }
            String code = codeNode.asText();
            String stepUuid = record.path(IntegrateField.UUID).asText();
            if (StringUtils.isEmpty(stepUuid)) {
                stepUuid = "public_" + UUID.randomUUID().toString();
                ((ObjectNode) record).put(IntegrateField.UUID, stepUuid);
            }
            GableFileUtils.saveFile(code, GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.GROOVY, stepUuid + ".groovy");
        } else if (StringUtils.equals(type, IntegrateField.JSON_SCHEMA_TYPE)) {
            String stepUuid = record.path(IntegrateField.UUID).asText();
            if (StringUtils.isEmpty(stepUuid)) {
                stepUuid = "public_" + UUID.randomUUID().toString();
                ((ObjectNode) record).put(IntegrateField.UUID, stepUuid);
            }
        }
    }

    @Override
    public boolean updateIntegrate(ArrayNode records, String uuid) {
        for (JsonNode record : records) {
            handleUuid(record);
        }
        GableFileUtils.saveFile(records.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.INTEGRATE, uuid, "define.json");
        return true;
    }

    @Override
    public int delete(String uuid) {
        ArrayNode list = this.readFromFile(false);
        if (list == null) {
            return 0;
        }
        // handle cache
        JsonNode cacheMenu = CACHE.getIfPresent(MENU);
        if (cacheMenu == null) {
            return 0;
        }
        ArrayNode newList = remove(list, uuid);
        File file = FileUtils.getFile(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.INTEGRATE, uuid);
        if (file.exists()) {
            FileUtils.deleteQuietly(file);
        }
        GableFileUtils.saveFile(newList.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, INTEGRATE_TEST_FILE);
        ArrayNode newCache = remove((ArrayNode) cacheMenu, uuid);
        CACHE.invalidate(uuid);
        log.info("update integrate while remove integrate");
        CACHE.put(MENU, newCache);
        return list.size() - newList.size();
    }

    @Override
    public ObjectNode getItem(String uuid) {
        JsonNode define = CACHE.getIfPresent(uuid);
        if (define != null) {
            if (define.isNull()) {
                return null;
            }
            return (ObjectNode) define;
        }
        JsonNode jsonNode = CACHE.getIfPresent(MENU);
        if (jsonNode == null) {
            return null;
        }
        ArrayNode arr = (ArrayNode) jsonNode;
        for (JsonNode node : arr) {
            ObjectNode item = (ObjectNode) node;
            String itemUuid = item.path(IntegrateField.UUID).asText();
            if (StringUtils.equals(uuid, itemUuid)) {
                CACHE.put(uuid, item);
                return item;
            }
        }
        CACHE.put(uuid, NullNode.getInstance());
        return null;
    }

    @Override
    public Result entrustRun(String uuid) {
        ObjectNode item = this.getItem(uuid);
        if (item == null) {
            return Result.error("Test Not Exist");
        }
        if (EN_TRUST_RUN_MAP.containsKey(uuid)) {
            return Result.error("Test Is Running");
        }
        IntegrateEnTrustRun runner = new IntegrateEnTrustRun(item, new ThreadListener() {
            @Override
            public void onFinished(ObjectNode item) {
                String uuid = item.path(IntegrateField.UUID).asText();
                log.info("remove entrust runner thread: {}", uuid);
                EN_TRUST_RUN_MAP.remove(uuid);
            }
        });
        EN_TRUST_RUN_MAP.put(uuid, runner);
        runner.start();
        return Result.success();
    }

    private ArrayNode remove(ArrayNode list, String uuid) {
        ArrayNode newList = objectMapper.createArrayNode();
        for (JsonNode jsonNode : list) {
            String itemUuid = jsonNode.path(IntegrateField.UUID).asText();
            if (StringUtils.equals(uuid, itemUuid)) {
                continue;
            }
            newList.add(jsonNode);
        }
        return newList;
    }

    @Override
    public JsonNode getIntegrateDefine(String uuid) {
        return GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.INTEGRATE, uuid, "define.json");
    }

    @Override
    public boolean addTag(String tagName, String uuid) {
        JsonNode allTests = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, INTEGRATE_TEST_FILE);
        if (allTests == null) {
            return false;
        }
        for (int i = 0; i < allTests.size(); i++) {
            JsonNode node = allTests.get(i);
            if (!node.isObject()) {
                continue;
            }
            ObjectNode item = (ObjectNode) node;
            if (!StringUtils.equals(item.path(IntegrateField.UUID).asText(), uuid)) {
                continue;
            }
            JsonNode tags = item.path(IntegrateField.TAG);
            if (!tags.isArray()) {
                ArrayNode newTags = objectMapper.createArrayNode();
                newTags.add(tagName);
                item.set(IntegrateField.TAG, newTags);
            }else {
                ArrayNode newTags = (ArrayNode) tags;
                for (JsonNode newTag : newTags) {
                    if (StringUtils.equals(newTag.asText(), tagName)) {
                        return false;
                    }
                }
                newTags.add(tagName);
            }
            GableFileUtils.saveFile(allTests.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, INTEGRATE_TEST_FILE);
            return true;
        }
        return false;
    }
}
