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

package org.advancedproductivity.gable.framework.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.utils.GableFileUtils;
import org.advancedproductivity.gable.web.service.MenuService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author zzq
 */
@Slf4j
public class GableConfig {
    private static ObjectNode config = null;
    public static final String PERSISTENCE = "operationDir";
    public static final String PUBLIC_PATH = "public";
    public static final String CONFIG_FILE_NAME = "config.json";

    public static void initConfig(){
        String jarPath = getConfigPath();
        final JsonNode configJsonNode = GableFileUtils.readFileAsJson(jarPath, CONFIG_FILE_NAME);
        if (configJsonNode == null || !configJsonNode.isObject()) {
            config = generateDefaultConfig(jarPath, new ObjectMapper());
            GableFileUtils.saveFile(config.toPrettyString(), jarPath, CONFIG_FILE_NAME);
        } else {
            config = (ObjectNode) configJsonNode;
        }
        checkNecessaryFile();
    }

    public static class A {

        public synchronized void  s1(){
            System.out.println("s1 start " + System.currentTimeMillis());
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("s1 end "  + System.currentTimeMillis());
        }

        public synchronized void  s2(){
            System.out.println("s2 end "  + System.currentTimeMillis());
        }

        public static synchronized void  ss1(){
            System.out.println("ss1 start " + System.currentTimeMillis());
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("ss1 end "  + System.currentTimeMillis());
        }

        public static synchronized void  ss2(){
            System.out.println("ss2 end "  + System.currentTimeMillis());
            ss1();
        }
    }

    public static void main(String[] args) {
        A a = new A();
        A b = new A();
        new Thread(() -> {
            A.ss1();
        }).start();
        new Thread(() -> {
            A.ss2();
        }).start();
//        Map<String, BigDecimal> map = getA();
//        Map<String, BigDecimal> map2640 = get2640();
//        Map<String, BigDecimal> map2890 = get2890();
//        for (String s : map.keySet()) {
//            BigDecimal t = null;
//            String s00 = "";
//            if (map2640.containsKey(s)) {
//                t = map.get(s).multiply(map2640.get(s)).setScale(3, RoundingMode.HALF_UP);
//                s00 = "2640";
//            } else if (map2890.containsKey(s)) {
//                t = map.get(s).multiply(map2890.get(s)).setScale(3, RoundingMode.HALF_UP);
//                s00 = "2890";
//            } else {
//                System.out.println("not fond");
//            }
//            if (t != null) {
//                System.out.printf("\n# %s * %s = %s", map.get(s).toString(), s00, t.toString());
//                System.out.printf("\nUPDATE  act_bill_detail set amount = %s, counter_weight = %s where order_no = '%s';",
//                        t.toString(), map.get(s).toString(), s);
//            }
//        }
    }

    private static Map<String, BigDecimal> get2890() {
        Map<String, BigDecimal> map = new HashMap<>();
        BigDecimal _2890 = new BigDecimal("2890");
        map.put("E12927220314000012O", _2890);
        map.put("E12916220307000006O", _2890);
        map.put("E12916220307000005O", _2890);
        map.put("E12927220418000005O", _2890);
        return map;

    }

    private static Map<String, BigDecimal> get2640() {
        Map<String, BigDecimal> map = new HashMap<>();
        BigDecimal _2640 = new BigDecimal("2640");
        map.put("E12916220307000003O", _2640);
        map.put("E12916220307000002O", _2640);
        map.put("E12916220310000001O", _2640);
        map.put("E12927220314000002O", _2640);
        map.put("E12927220314000003O", _2640);
        map.put("E12927220314000005O", _2640);
        map.put("E12927220314000006O", _2640);
        map.put("E12927220314000008O", _2640);
        map.put("E12908220329000003O", _2640);
        map.put("E12927220420000007O", _2640);
        map.put("E12927220420000006O", _2640);
        map.put("E12927220420000002O", _2640);
        map.put("E12927220418000028O", _2640);
        map.put("E12927220418000027O", _2640);
        map.put("E12927220418000022O", _2640);
        map.put("E12927220415000007O", _2640);
        map.put("E12933220329000001O", _2640);
        map.put("E13155220324000007O", _2640);
        map.put("E13155220324000005O", _2640);
        map.put("E12933220314000004O", _2640);
        map.put("E12935220309000001O", _2640);
        return map;
    }

    private static Map<String, BigDecimal> getA() {
        Map<String, BigDecimal> map = new HashMap<>();
        map.put("E12927220420000007O",	new BigDecimal("19.241"));
        map.put("E12927220420000006O",	new BigDecimal("10.731"));
        map.put("E12927220420000002O",	new BigDecimal("2.108"));
        map.put("E12933220329000001O",	new BigDecimal("8.693"));
        map.put("E12908220329000003O",	new BigDecimal("18.637"));
        map.put("E13155220324000007O",	new BigDecimal("6.342"));
        map.put("E13155220324000005O",	new BigDecimal("9.895"));
        map.put("E12927220418000028O",	new BigDecimal("3.985"));
        map.put("E12927220418000027O",	new BigDecimal("3.724"));
        map.put("E12927220418000022O",	new BigDecimal("17.030"));
        map.put("E12927220418000005O",	new BigDecimal("12.253"));
        map.put("E12927220415000007O",	new BigDecimal("2.627"));
        map.put("E12927220314000008O",	new BigDecimal("3.188"));
        map.put("E12916220307000005O",	new BigDecimal("3.293"));
        map.put("E12916220307000006O",	new BigDecimal("3.702"));
        map.put("E12916220307000003O",	new BigDecimal("2.433"));
        map.put("E12916220307000002O",	new BigDecimal("2.117"));
        map.put("E12916220310000001O",	new BigDecimal("4.953"));
        map.put("E12927220314000012O",	new BigDecimal("2.055"));
        map.put("E12935220309000001O",	new BigDecimal("7.091"));
        map.put("E12927220314000005O",	new BigDecimal("30.780"));
        map.put("E12933220314000004O",	new BigDecimal("7.843"));
        map.put("E12927220314000006O",	new BigDecimal("7.665"));
        map.put("E12927220314000003O",	new BigDecimal("5.083"));
        map.put("E12927220314000002O",	new BigDecimal("2.394"));
        return map;
    }

    private static void checkNecessaryFile() {
        File persistenceFilePath = new File(getGablePath());
        if (!persistenceFilePath.exists()) {
            persistenceFilePath.mkdirs();
        }
        File publicPath = FileUtils.getFile(getGablePath(), PUBLIC_PATH, UserDataType.UNIT);
        if (!publicPath.exists()) {
            publicPath.mkdirs();
        }
        File publicUnitMenu = FileUtils.getFile(getGablePath(), PUBLIC_PATH, UserDataType.UNIT, MenuService.UnitMenuFileName);
        if (!publicUnitMenu.exists()) {
            GableFileUtils.saveFile("[]",getGablePath(), PUBLIC_PATH, UserDataType.UNIT, MenuService.UnitMenuFileName);
        }
    }

    public static ObjectNode getConfig() {
        if (config == null) {
            return null;
        }
        return config.deepCopy();
    }

    public static void updateConfig(ObjectNode config) {
        GableConfig.config = config;
        checkNecessaryFile();
        GableFileUtils.saveFile(config.toPrettyString(), getConfigPath(), CONFIG_FILE_NAME);
    }

    public static String checkRequired(ObjectNode config) {
        if (!config.path(PERSISTENCE).isTextual()) {
            return PERSISTENCE;
        }
        return null;
    }

    private static ObjectNode generateDefaultConfig(String jarPath, ObjectMapper mapper) {
        ObjectNode config = mapper.createObjectNode();
        String persistenceFolderName = "Persistence";
        config.put(PERSISTENCE, jarPath + File.separator + persistenceFolderName);
        return config;
    }

    public static String getAsString(String key) {
        if (config == null) {
            return "";
        }
        return config.path(key).asText();
    }

    public static String getGablePath(){
        return getAsString(PERSISTENCE);
    }

    private static String getConfigPath(){
        return SystemUtils.getUserDir().getAbsolutePath();
    }
}
