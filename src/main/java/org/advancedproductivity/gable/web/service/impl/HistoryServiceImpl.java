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
import org.advancedproductivity.gable.framework.config.*;
import org.advancedproductivity.gable.framework.core.TestType;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.service.HistoryService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zzq
 */
@Service
public class HistoryServiceImpl implements HistoryService {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#.00");

    private static final ConcurrentHashMap<String, AtomicInteger> RECORDER = new ConcurrentHashMap<>();

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public int recordUnitTest(String nameSpace, String uuid, String content) {
        AtomicInteger atomicInteger = RECORDER.get(uuid);
        int id = 0;
        if (atomicInteger == null) {
            File hisPath = FileUtils.getFile(GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, uuid, UserDataType.HISTORY);
            if (hisPath.exists()) {
                String[] list = hisPath.list();
                if (list != null) {
                    id = list.length;
                }
            }
            atomicInteger = new AtomicInteger(id);
            RECORDER.put(uuid, atomicInteger);
        } else {
            id = atomicInteger.incrementAndGet();
        }
        GableFileUtils.saveFile(content, GableConfig.getGablePath(), nameSpace, UserDataType.UNIT, uuid, UserDataType.HISTORY, id + ".json");
        return id;
    }

    @Override
    public int recordGroovy(String nameSpace, String uuid, String content) {
        AtomicInteger atomicInteger = RECORDER.get(uuid);
        int id = 0;
        if (atomicInteger == null) {
            File hisPath = FileUtils.getFile(GableConfig.getGablePath(), nameSpace, UserDataType.GROOVY_HIS, uuid, UserDataType.HISTORY);
            if (hisPath.exists()) {
                String[] list = hisPath.list();
                if (list != null) {
                    id = list.length;
                }
            }
            atomicInteger = new AtomicInteger(id);
            RECORDER.put(uuid, atomicInteger);
        } else {
            id = atomicInteger.incrementAndGet();
        }
        GableFileUtils.saveFile(content, GableConfig.getGablePath(), nameSpace, UserDataType.GROOVY_HIS, uuid, UserDataType.HISTORY, id + ".json");
        return id;
    }

    @Override
    public int recordJsonSchemaStep(String nameSpace, String uuid, String content) {
        AtomicInteger atomicInteger = RECORDER.get(uuid);
        int id = 0;
        if (atomicInteger == null) {
            File hisPath = FileUtils.getFile(GableConfig.getGablePath(), nameSpace, UserDataType.JSON_SCHEMA_HIS,
                    uuid, UserDataType.HISTORY);
            if (hisPath.exists()) {
                String[] list = hisPath.list();
                if (list != null) {
                    id = list.length;
                }
            }
            atomicInteger = new AtomicInteger(id);
            RECORDER.put(uuid, atomicInteger);
        } else {
            id = atomicInteger.incrementAndGet();
        }
        GableFileUtils.saveFile(content, GableConfig.getGablePath(), nameSpace, UserDataType.JSON_SCHEMA_HIS,
                uuid, UserDataType.HISTORY, id + ".json");
        return id;
    }

    @Override
    public int recordIntegrateTest(String nameSpace, String uuid, String content) {
        AtomicInteger atomicInteger = RECORDER.get(uuid);
        int id = 0;
        if (atomicInteger == null) {
            File hisPath = FileUtils.getFile(GableConfig.getGablePath(), nameSpace, UserDataType.INTEGRATE, uuid, UserDataType.HISTORY);
            if (hisPath.exists()) {
                String[] list = hisPath.list();
                if (list != null) {
                    id = list.length;
                }
            }
            atomicInteger = new AtomicInteger(id);
            RECORDER.put(uuid, atomicInteger);
        } else {
            id = atomicInteger.incrementAndGet();
        }
        GableFileUtils.saveFile(content, GableConfig.getGablePath(), nameSpace, UserDataType.INTEGRATE, uuid, UserDataType.HISTORY, id + ".json");
        return id;
    }

    @Override
    public boolean recordIntegrateTest(int historyId, String nameSpace, String uuid, String content) {
        return  GableFileUtils.saveFile(content, GableConfig.getGablePath(), nameSpace, UserDataType.INTEGRATE, uuid, UserDataType.HISTORY, historyId + ".json");
    }

    @Override
    public ObjectNode analysis(ArrayNode records, String server, String uuid) {
        ObjectNode mapperObjectNode = objectMapper.createObjectNode();
        mapperObjectNode.put("uuid", uuid);
        mapperObjectNode.put("server", server);
        boolean noError = true;
        int total = 0;
        int successTotal = 0;
        int failedTotal = 0;
        int ignoreTotal = 0;
        int timeTotal = 0;
        int testSuccessCount = 0;
        int testFailedCount = 0;
        int testIgnoreCount = 0;
        int testTotal = 0;
        int testTimeTotal = 0;
        int stepSuccessCount = 0;
        int stepFailedCount = 0;
        int stepIgnoreCount = 0;
        int stepTotal = 0;
        int stepTimeTotal = 0;
        int jsonSchemaSuccessCount = 0;
        int jsonSchemaFailedCount = 0;
        int jsonSchemaIgnoreCount = 0;
        int jsonSchemaTotal = 0;
        int jsonSchemaTimeTotal = 0;
        String startTime = null;
        long endTime = 0;
        for (int i = 0; i < records.size(); i++) {
            total++;
            JsonNode item = records.path(i);
            int result = item.path(ConfigField.STATUS).asInt();
            String type = item.path(ConfigField.TEST_TYPE).asText();
            long itemStart = item.path(ConfigField.START_TIME).asLong();
            long itemEnd = item.path(ConfigField.END_TIME).asLong();
            if (itemStart == 0) {
                ((ObjectNode) item).put("startTimeStr", "");
            }else {
                ((ObjectNode) item).put("startTimeStr", FORMAT.format(new Date(itemStart)));
            }
            if (itemEnd == 0) {
                ((ObjectNode) item).put("endTimeStr", "");
            }else {
                ((ObjectNode) item).put("endTimeStr", FORMAT.format(new Date(itemEnd)));
            }
            if (startTime == null && itemStart != 0) {
                startTime = FORMAT.format(new Date(itemStart));
            }
            if (itemEnd != 0) {
                endTime = itemEnd;
            }
            long timeTakes =  itemEnd - itemStart;
            timeTotal += timeTakes;
            if (result == IntegrateStepStatus.FAILED.getValue()) {
                failedTotal++;
                noError = false;
                if (StringUtils.equals(type, TestType.STEP.name())) {
                    stepFailedCount++;
                    stepTotal++;
                    stepTimeTotal += timeTakes;
                } else if (StringUtils.equals(type, TestType.JSON_SCHEMA.name())) {
                    jsonSchemaFailedCount++;
                    jsonSchemaTotal++;
                    jsonSchemaTimeTotal += timeTakes;
                } else {
                    testFailedCount++;
                    testTotal++;
                    testTimeTotal += timeTakes;
                }
            } else if (result == IntegrateStepStatus.SUCCESS.getValue()) {
                successTotal++;
                if (StringUtils.equals(type, TestType.STEP.name())) {
                    stepSuccessCount++;
                    stepTotal++;
                    stepTimeTotal += timeTakes;
                } else if (StringUtils.equals(type, TestType.JSON_SCHEMA.name())) {
                    jsonSchemaSuccessCount++;
                    jsonSchemaTotal++;
                    jsonSchemaTimeTotal += timeTakes;
                } else {
                    testSuccessCount++;
                    testTotal++;
                    testTimeTotal += timeTakes;
                }
            } else if (result == IntegrateStepStatus.NOT_RUN.getValue()) {
                ignoreTotal++;
                if (StringUtils.equals(type, TestType.STEP.name())) {
                    stepIgnoreCount++;
                    stepTotal++;
                    stepTimeTotal += timeTakes;
                } else if (StringUtils.equals(type, TestType.JSON_SCHEMA.name())) {
                    jsonSchemaIgnoreCount++;
                    jsonSchemaTotal++;
                    jsonSchemaTimeTotal += timeTakes;
                } else {
                    testIgnoreCount++;
                    testTotal++;
                    testTimeTotal += timeTakes;
                }
            }
        }

        mapperObjectNode.put(IntegrateField.NO_ERROR, noError);
        mapperObjectNode.put("total", total);
        mapperObjectNode.put("successTotal", successTotal);
        mapperObjectNode.put("failedTotal", failedTotal);
        mapperObjectNode.put("ignoreTotal", ignoreTotal);
        mapperObjectNode.put("timeTotal", timeTotal);
        mapperObjectNode.put("testSuccessCount", testSuccessCount);
        mapperObjectNode.put("testFailedCount", testFailedCount);
        mapperObjectNode.put("testIgnoreCount", testIgnoreCount);
        mapperObjectNode.put("testTotal", testTotal);
        mapperObjectNode.put("testTimeTotal", testTimeTotal);
        mapperObjectNode.put("stepSuccessCount", stepSuccessCount);
        mapperObjectNode.put("stepFailedCount", stepFailedCount);
        mapperObjectNode.put("stepIgnoreCount", stepIgnoreCount);
        mapperObjectNode.put("stepTotal", stepTotal);
        mapperObjectNode.put("stepTimeTotal", stepTimeTotal);
        mapperObjectNode.put("jsonSchemaSuccessCount", jsonSchemaSuccessCount);
        mapperObjectNode.put("jsonSchemaFailedCount", jsonSchemaFailedCount);
        mapperObjectNode.put("jsonSchemaIgnoreCount", jsonSchemaIgnoreCount);
        mapperObjectNode.put("jsonSchemaTotal", jsonSchemaTotal);
        mapperObjectNode.put("jsonSchemaTimeTotal", jsonSchemaTimeTotal);
        mapperObjectNode.put("startAt", startTime);
        mapperObjectNode.put("endAt", FORMAT.format(new Date(endTime)));

        if (total != 0) {
            mapperObjectNode.put("totalAvg", DOUBLE_FORMAT.format( timeTotal / (double)total));
        }else {
            mapperObjectNode.put("totalAvg", 0);
        }
        if (stepTotal != 0) {
            mapperObjectNode.put("stepAvg", DOUBLE_FORMAT.format(stepTimeTotal / (double) stepTotal));
        }else {
            mapperObjectNode.put("stepAvg", 0);
        }
        if (jsonSchemaTotal != 0) {
            mapperObjectNode.put("jsonSchemaAvg", DOUBLE_FORMAT.format(jsonSchemaTimeTotal / (double) jsonSchemaTotal));
        }else {
            mapperObjectNode.put("jsonSchemaAvg", 0);
        }
        if (testTotal != 0) {
            mapperObjectNode.put("testAvg", DOUBLE_FORMAT.format(testTimeTotal / (double)testTotal));
        }else {
            mapperObjectNode.put("testAvg", 0);
        }
        mapperObjectNode.set("detail", records);
        mapperObjectNode.put("createdAt", FORMAT.format(new Date()));
        return mapperObjectNode;
    }

    @Override
    public void indexHistory(String uuid, int historyId, boolean noError, String origin, String startAt, String endAt,
                             String envName) {
        ObjectNode item = objectMapper.createObjectNode();
        item.put("uuid", uuid);
        item.put("id", historyId + "");
        item.put("origin", origin);
        item.put("noError", noError);
        item.put("startAt", startAt);
        item.put("endAt", endAt);
        item.put("env", envName);
        ArrayNode history = readOverviewHistory(uuid);
        ArrayNode newArray = objectMapper.createArrayNode();
        newArray.add(item);
        for (JsonNode jsonNode : history) {
            newArray.add(jsonNode);
        }
        GableFileUtils.saveFile(newArray.toPrettyString(), GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.INTEGRATE,
                uuid,
                UserDataType.HISTORY + ".json");
    }

    @Override
    public ArrayNode readOverviewHistory(String uuid) {
        JsonNode arr = GableFileUtils.readFileAsJson(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH, UserDataType.INTEGRATE,
                uuid,
                UserDataType.HISTORY + ".json");
        if (arr == null) {
            return objectMapper.createArrayNode();
        }
        return (ArrayNode) arr;
    }
}
