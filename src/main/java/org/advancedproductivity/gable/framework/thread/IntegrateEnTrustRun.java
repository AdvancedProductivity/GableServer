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

package org.advancedproductivity.gable.framework.thread;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.*;
import org.advancedproductivity.gable.framework.core.GlobalVar;
import org.advancedproductivity.gable.framework.core.TestType;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zzq
 */
@Slf4j
public class IntegrateEnTrustRun extends Thread {
    private boolean isStop = false;
    private ObjectNode integrateItem;
    private ThreadListener listener;
    private String testUuid;
    private ArrayNode define;
    private int historyId = 0;
    // use while run
    private ObjectMapper objectMapper;
    private ObjectNode instance;
    private ObjectNode lastOut;
    private ObjectNode global;
    private Map<Integer, ObjectNode> ins = new HashMap<>();
    private String env = null;
    private EnTrustHandle handle = null;
    private ArrayList<Integer> testIndexArr;
    private String lastType = null;
    private int runningIndex = 0;
    private String server = "/";

    public IntegrateEnTrustRun(ObjectNode item, ThreadListener listener,
                               EnTrustHandle handle,
                               String env, String server) {
        this.server = server;
        this.integrateItem = item;
        this.testUuid = item.path(IntegrateField.UUID).asText();
        this.listener = listener;
        this.env = env;
        this.handle = handle;
    }

    public void makeStop(){
        this.isStop = true;
    }

    @Override
    public void run() {
        try {
            initStatusData();
            // record running log take a historyId.for query while running
            if (this.handle != null) {
                this.historyId = handle.takeHistory(this.testUuid, define);
            }
            integrateItem.put(IntegrateField.STATUS, IntegrateStepStatus.RUNNING.getValue());
            executeIntegrateTest();
        } catch (Throwable e) {
            log.error("error happens while entrust in index: {} the uuid is: {}", this.runningIndex,
                    this.testUuid, e);
            integrateItem.put(IntegrateField.STATUS, IntegrateStepStatus.FAILED.getValue());
        }finally {
            if (this.handle != null) {
                boolean isSucceed = handle.recordHistory(this.server, this.historyId, this.testUuid, define);
                if (isSucceed) {
                    integrateItem.put(IntegrateField.STATUS, IntegrateStepStatus.SUCCESS.getValue());
                }else {
                    integrateItem.put(IntegrateField.STATUS, IntegrateStepStatus.FAILED.getValue());
                }
            }else {
                integrateItem.put(IntegrateField.STATUS, IntegrateStepStatus.SUCCESS.getValue());
            }
            if (this.listener != null) {
                this.listener.onFinished(integrateItem);
            }
        }
    }

    private void executeIntegrateTest() {
        int TOTAL = define.size();
        while (runningIndex < TOTAL) {
            if (isStop) {
                log.info("receive stop command in the {} step", (runningIndex + 1));
                break;
            }
            ObjectNode item = (ObjectNode) define.path(runningIndex);
            String uuid = item.path(IntegrateField.UUID).asText();
            ObjectNode nextIn = this.getNextIn(runningIndex);
            item.put(ConfigField.START_TIME, System.currentTimeMillis());
            String type = item.path(IntegrateField.TYPE).asText();
            if (StringUtils.equals(type, TestType.STEP.name())) {
                executeAsStep(uuid, item, nextIn, runningIndex);
            }else if (StringUtils.equals(type, TestType.JSON_SCHEMA.name())) {
                executeAsJsonSchema(uuid, item);
            }else {
                executeAsTest(uuid, item, nextIn);
            }
            runningIndex++;
        }
    }

    private void executeAsTest(String uuid, ObjectNode item, ObjectNode nextIn) {
        String type = item.path(ConfigField.TEST_TYPE).asText();
        ObjectNode data = this.objectMapper.createObjectNode();
        if (StringUtils.equals(type, TestType.GROOVY_SCRIPT.name())) {
            log.info("add id for groovy test: {}", uuid);
            nextIn.put(CodeField.GROOVY_TEST_UUID, uuid);
            nextIn.put(CodeField.GROOVY_TEST_CODE, "");
        }
        data.set(ConfigField.DETAIL, nextIn);
        data.set(IntegrateField.INSTANCE, this.instance);
        ObjectNode out = this.handle.runTest(GableConfig.PUBLIC_PATH, uuid, type, data);
        handleExecuteResult(out, item);
        this.lastOut = out;
        this.lastType = type;
    }

    private void executeAsStep(String uuid, ObjectNode item, ObjectNode nextIn, Integer curIndex) {
        ObjectNode out = this.handle.runStep(uuid, this.instance, this.global, nextIn, this.lastOut);
        handleExecuteResult(out, item);
        JsonNode newInstance = out.path(IntegrateField.AFTER).path(IntegrateField.INSTANCE);
        if (newInstance.isObject()) {
            this.updateInstance((ObjectNode) newInstance);
        }
        JsonNode newNextIn = out.path(IntegrateField.AFTER).path(IntegrateField.NEXT_IN);
        if (newNextIn.isObject()) {
            this.updateNextIn((ObjectNode) newNextIn, this.testIndexArr.get(curIndex));
        }
        log.info("step uuid: {} finished", uuid);
    }

    private void executeAsJsonSchema(String uuid, ObjectNode item) {
        JsonNode schema = NullNode.getInstance();
        try {
            schema = objectMapper.readTree(item.path(IntegrateField.CODE).asText());
        } catch (Exception e) {
            log.error("parser jsonschema exp to json error in Entrust Runner", e);
        }
        JsonNode lastOut = null;
        if (StringUtils.equals(this.lastType, TestType.HTTP.name())) {
            lastOut = this.lastOut.path(ConfigField.HTTP_BODY_CONTENT);
        }else {
            lastOut = this.lastOut;
        }
        ObjectNode out = this.handle.runJsonScheam(uuid, lastOut, schema);
        handleExecuteResult(out, item);
    }

    private void handleExecuteResult(ObjectNode out, ObjectNode itemNode) {
        boolean isPassed = out.path(ValidateField.VALIDATE).path(ValidateField.RESULT).asBoolean();
        if (isPassed) {
            itemNode.put(IntegrateField.STATUS, IntegrateStepStatus.SUCCESS.getValue());
        } else {
            itemNode.put(IntegrateField.STATUS, IntegrateStepStatus.FAILED.getValue());
        }
        itemNode.set(ConfigField.HISTORY_ID, out.path(ConfigField.HISTORY_ID).deepCopy());
        itemNode.put(ConfigField.END_TIME, System.currentTimeMillis());
    }

    private ObjectNode getNextIn(int runningIndex) {
        Integer index = testIndexArr.get(runningIndex);
        JsonNode runningItem = define.path(index);
        String stepName = runningItem.path(IntegrateField.NAME).asText();
        if (index == -1) {
            log.info("get empty next in for {} runningIndex: {}", stepName, runningIndex + 1);
            return this.objectMapper.createObjectNode();
        }
        ObjectNode nextIn = this.ins.get(index);
        if (nextIn != null) {
            log.info("get next in from cache for {} testIndex: {} runningIndex: {}", stepName, index + 1, runningIndex + 1);
            return nextIn;
        }
        String uuid = runningItem.path(IntegrateField.UUID).asText();
        String caseId = runningItem.path(IntegrateField.CASE_ID).asText();
        int caseVersion = runningItem.path(IntegrateField.CASE_VERSION).asInt();
        log.info("get next in from file for {} testIndex: {} runningIndex: {}", stepName, index + 1, runningIndex + 1);
        ObjectNode in = this.handle.getConfig(GableConfig.PUBLIC_PATH, uuid, this.env, caseId, caseVersion);
        if (in != null) {
            in = (ObjectNode) in.path(ConfigField.DETAIL);
            this.ins.put(index,in);
            return in;
        }else {
            log.error("can not find config in  testIndex: {} runningIndex: {}", index, runningIndex);
            return this.objectMapper.createObjectNode();
        }
    }

    private void readDefine() throws Exception {
        JsonNode j = GableFileUtils.readFileAsJson(GableConfig.getGablePath(),
                GableConfig.PUBLIC_PATH,
                UserDataType.INTEGRATE,
                this.testUuid, "define.json");
        if (j == null) {
            throw new Exception("Test is not exist");
        }
        if (!j.isArray()) {
            throw new Exception("Test Define Incorrect");
        }
        this.define = j.deepCopy();
    }

    public ArrayNode getDefine() {
        return define;
    }

    private void initStatusData() throws Exception {
        this.objectMapper = new ObjectMapper();
        this.instance = this.objectMapper.createObjectNode();
        this.lastOut = this.objectMapper.createObjectNode();
        this.lastType = null;
        this.runningIndex = 0;
        this.global = GlobalVar.globalVar.deepCopy();
        this.ins.clear();
        readDefine();
        this.testIndexArr = getTestInIntegrate(define);
    }

    private ArrayList<Integer> getTestInIntegrate(ArrayNode define) {
        log.info("define size: {}", define.size());
        ArrayList<Integer> indexArr = new ArrayList<>(define.size());
        int lastTestIndex = -1;
        for (int i = 0; i < define.size(); i++) {
            indexArr.add(lastTestIndex);
        }
        for (int i = define.size() - 1; i >= 0; i--) {
            ObjectNode jsonNode = (ObjectNode) define.get(i);
            jsonNode.put(IntegrateField.STATUS, IntegrateStepStatus.NOT_RUN.getValue());
            String type = jsonNode.path(ConfigField.TEST_TYPE).asText();
            if (!StringUtils.equals(TestType.STEP.name(), type) && !StringUtils.equals(TestType.JSON_SCHEMA.name(), type)) {
                lastTestIndex = i;
            }
            indexArr.set(i, lastTestIndex);
        }
        log.info("find {} test in integrate test", indexArr.size());
        return indexArr;
    }

    private void updateNextIn(ObjectNode newInstance, Integer index) {
        this.ins.put(index, newInstance);
    }

    private void updateInstance(ObjectNode newInstance) {
        this.instance = newInstance;
    }
}
