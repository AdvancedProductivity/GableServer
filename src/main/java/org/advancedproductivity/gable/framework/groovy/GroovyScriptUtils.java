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

package org.advancedproductivity.gable.framework.groovy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.config.IntegrateField;
import org.advancedproductivity.gable.framework.config.UserDataType;
import org.advancedproductivity.gable.framework.config.ValidateField;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zzq
 */
@Slf4j
public class GroovyScriptUtils {
    private static final ConcurrentHashMap<String, GroovyScriptEngine> engineMap = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static Object runSample(String namespace) throws IOException {
        File file = FileUtils.getFile(GableConfig.getGablePath(), namespace, UserDataType.GROOVY, GroovyType.SAMPLE_TYPE);
        if (!file.exists()) {
            return null;
        }
        GroovyScriptEngine engine = engineMap.get(namespace);
        if (engine == null) {
            engine = new GroovyScriptEngine(file.getAbsolutePath());
            engineMap.put(namespace, engine);
        }
        Binding binding = new Binding();
        try {
            return engine.run(GroovyType.SAMPLE_TYPE, binding);
        }catch (Throwable e) {
            System.out.println(e.getMessage());
            log.error("error happens while run sample groovy script", e);
            return e.getMessage();
        }
    }

    public static Object runTest(String namespace, ObjectNode in, ObjectNode out, String scriptUuid)
            throws IOException {
        File file = FileUtils.getFile(GableConfig.getGablePath(), namespace, UserDataType.GROOVY, scriptUuid);
        if (!file.exists()) {
            return null;
        }
        GroovyScriptEngine engine = engineMap.get(namespace);
        if (engine == null) {
            engine = new GroovyScriptEngine(file.getAbsolutePath());
            engineMap.put(namespace, engine);
        }
        Binding binding = new Binding();
        binding.setVariable("in", in);
        binding.setVariable("out", out);
        try {
            return engine.run(scriptUuid, binding);
        } catch (Throwable e) {
            out.put("error", e.getMessage());
            log.error("error happens while run sample groovy script", e);
            return e.getMessage();
        }
    }

    public static ObjectNode runStep(String namespace, JsonNode nextIn, JsonNode lastOut, JsonNode instance,
                                     JsonNode global, String scriptUuid){
        File file = FileUtils.getFile(GableConfig.getGablePath(), namespace, UserDataType.GROOVY, scriptUuid);
        if (!file.exists()) {
            return null;
        }
        ObjectNode result = mapper.createObjectNode();
        try {
            GroovyScriptEngine engine = engineMap.get(namespace);
            if (engine == null) {
                engine = new GroovyScriptEngine(file.getAbsolutePath());
                engineMap.put(namespace, engine);
            }
            Binding binding = new Binding();
            binding.setVariable(IntegrateField.NEXT_IN, nextIn);
            binding.setVariable(IntegrateField.LAST_OUT, lastOut);
            binding.setVariable(IntegrateField.INSTANCE, instance);
            binding.setVariable(IntegrateField.GLOBAL, global);
            engine.run(scriptUuid, binding);
            result.put(ValidateField.RESULT, true);
        } catch (Throwable e) {
            result.put(ValidateField.RESULT, false);
            result.put(ValidateField.CODE_ERROR, e.getMessage());
            log.error("error happens while run sample groovy script", e);
        }
        return result;
    }

    public static void runPreScript(String uuid, JsonNode in, ObjectNode param, JsonNode instance, JsonNode global) {
        File file = FileUtils.getFile(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH,
                UserDataType.GROOVY, uuid + ".groovy");
        if (!file.exists()) {
            return;
        }
        try {
            GroovyScriptEngine engine = engineMap.get(GableConfig.PUBLIC_PATH);
            if (engine == null) {
                engine = new GroovyScriptEngine(file.getAbsolutePath());
                engineMap.put(GableConfig.PUBLIC_PATH, engine);
            }
            Binding binding = new Binding();
            binding.setVariable("in", in);
            binding.setVariable("param", param);
            binding.setVariable("instance", instance);
            binding.setVariable("global", global);
            engine.run(uuid + ".groovy", binding);
        } catch (Throwable e) {
            log.error("error happens while run sample groovy script", e);
        }
    }

    public static void runPostScript(String uuid, JsonNode out, ObjectNode param, JsonNode instance, JsonNode global) {
        File file = FileUtils.getFile(GableConfig.getGablePath(), GableConfig.PUBLIC_PATH,
                UserDataType.GROOVY, uuid + ".groovy");
        if (!file.exists()) {
            return;
        }
        try {
            GroovyScriptEngine engine = engineMap.get(GableConfig.PUBLIC_PATH);
            if (engine == null) {
                engine = new GroovyScriptEngine(file.getAbsolutePath());
                engineMap.put(GableConfig.PUBLIC_PATH, engine);
            }
            Binding binding = new Binding();
            binding.setVariable("out", out);
            binding.setVariable("param", param);
            binding.setVariable("instance", instance);
            binding.setVariable("global", global);
            engine.run(uuid + ".groovy", binding);
        } catch (Throwable e) {
            log.error("error happens while run sample groovy script", e);
            JsonNode validateResult = out.path(ValidateField.VALIDATE);
            if (validateResult.isMissingNode()) {
                validateResult = mapper.createObjectNode();
                ((ObjectNode) out).set(ValidateField.VALIDATE, validateResult);
            }
            ((ObjectNode) validateResult).put(ValidateField.POST_VALIDATE, e.getMessage())
                    .put(ValidateField.RESULT, false);
        }
    }
}
