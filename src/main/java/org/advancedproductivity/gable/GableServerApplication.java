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

package org.advancedproductivity.gable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.advancedproductivity.gable.framework.config.GableConfig;
import org.advancedproductivity.gable.framework.utils.DatabaseUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author zzq
 */
// @SpringBootApplication
public class GableServerApplication {

    private static BigDecimal V = new BigDecimal("1000000");
    public static void main(String[] args) throws IOException {
//        SpringApplication.run(GableServerApplication.class, args);
//        GableConfig.initConfig();
        PrintStream fileWriter = new PrintStream(new FilterOutputStream(FileUtils.openOutputStream(FileUtils.getFile("C:\\Users\\16943\\Desktop\\plugin", "0621update.sql"))));
        PrintStream originWriter = new PrintStream(new FilterOutputStream(FileUtils.openOutputStream(FileUtils.getFile("C:\\Users\\16943\\Desktop\\plugin", "0621origin.sql"))));
        final ObjectMapper mapper = new ObjectMapper();
        ObjectNode databaseConfig = getDatabaseConfig(mapper);
        List<Long> orderIds = getAllIds(databaseConfig, mapper);
        int i = 0;
        for (Long orderId : orderIds) {
            reGenerateOrderBoxNums(databaseConfig, mapper, fileWriter, originWriter, orderId);
            if (i++ % 500 == 0) {
                originWriter.flush();
                fileWriter.flush();
            }
        }
        // handleBoxWeight(databaseConfig, mapper, fileWriter, originWriter);
        // handleOrder(databaseConfig, mapper, fileWriter, originWriter);
    }

    private static void reGenerateOrderBoxNums(ObjectNode databaseConfig, ObjectMapper mapper, PrintStream fileWriter, PrintStream originWriter, Long orderId) {
        String originSql = getOriginData(databaseConfig, mapper, orderId);
        databaseConfig.put("sql", "SELECT box_no,box_qty,box_qty_actual,aggregator,date_created FROM pcl_box where order_id = " + orderId);
        final JsonNode query = DatabaseUtils.query(databaseConfig, null);
        final JsonNode result = query.path("result");
        boolean allNull = true;
        Set<String> orderNos = new HashSet<>();
        int boxQtyTotal = -1;
        int boxQtyActualTotal = -1;
        for (JsonNode jsonNode : result) {
            String box_qty = jsonNode.path("box_qty").asText();
            String box_qty_actual = jsonNode.path("box_qty_actual").asText();
            String box_no = jsonNode.path("box_no").asText();
            if (!StringUtils.equals(box_qty, "null") || !StringUtils.equals(box_qty_actual, "null")) {
                allNull = false;
            }
            int boxQty = StringUtils.equals(box_qty, "null") ? -1 : Integer.parseInt(box_qty);
            int boxQtyActual = StringUtils.equals(box_qty_actual, "null") ? -1 : Integer.parseInt(box_qty_actual);
            if (boxQty >= 0 && !orderNos.contains(box_no)) {
                orderNos.add(box_no);
                if (boxQtyTotal == -1) {
                    boxQtyTotal = boxQty;
                }else{
                    boxQtyTotal += boxQty;
                }
                if (boxQtyActualTotal == -1) {
                    boxQtyActualTotal = boxQtyActual;
                }else{
                    boxQtyActualTotal += boxQtyActual;
                }
            }
        }
        if (allNull) {
            System.out.println(orderId + " is all null");
            return;
        }
        String boxQtyField = "null";
        if (boxQtyTotal != -1) {
            boxQtyField = boxQtyTotal + "";
        }
        String boxQtyFieldActual = "null";
        if (boxQtyActualTotal != -1) {
            boxQtyFieldActual = boxQtyActualTotal + "";
        }
        originWriter.printf("%s;\n", originSql);
        fileWriter.printf("update pcl_order set box_number = %s,box_number_actual = %s where id = %s;\n",
                boxQtyField, boxQtyFieldActual, orderId + "");

    }

    private static String getOriginData(ObjectNode databaseConfig, ObjectMapper mapper, Long orderId) {
        String sql = "SELECT box_number,box_number_actual FROM pcl_order where id = " + orderId;
        databaseConfig.put("sql", sql);
        final JsonNode query = DatabaseUtils.query(databaseConfig, null);
        final JsonNode result = query.path("result");
        final String box_number = result.path(0).path("box_number").asText();
        final String box_number_actual = result.path(0).path("box_number_actual").asText();
        return String.format("update pcl_order set box_number = %s,box_number_actual = %s where id = %s",
                box_number, box_number_actual, orderId + "");
    }

    private static List<Long> getAllIds(ObjectNode databaseConfig, ObjectMapper mapper) {
        databaseConfig.put("sql", "SELECT DISTINCT order_id\n" +
                "FROM (\n" +
                "         SELECT COUNT(1) as c, box_id, order_id\n" +
                "         FROM pcl_product\n" +
                "         GROUP BY box_id\n" +
                "     ) T\n" +
                "where T.c > 1");
        final JsonNode query = DatabaseUtils.query(databaseConfig, null);
        final JsonNode result = query.path("result");
        List<Long> ids = new ArrayList<>(result.size());
        for (JsonNode jsonNode : result) {
            ids.add(Long.parseLong(jsonNode.path("order_id").asText()));
        }
        return ids;
    }

    private static void handleOrder(ObjectNode databaseConfig, ObjectMapper mapper, PrintStream fileWriter, PrintStream originWriter) {
        databaseConfig.put("sql", "SELECT distinct order_id as id FROM pcl_box where pao_weight is not null");
        final JsonNode query = DatabaseUtils.query(databaseConfig, null);
        final JsonNode result = query.path("result");
        int i = 0;
        for (JsonNode jsonNode : result) {
            final String id = jsonNode.path("id").asText();
            System.out.println("=========== " + (i++) + " ============ handle id: " + id);
            databaseConfig.put("sql", "SELECT id,pao_weight,box_no FROM pcl_box where pao_weight is not null and order_id = " + id);
            final JsonNode boxQueryResult = DatabaseUtils.query(databaseConfig, null);
            final JsonNode boxexs = boxQueryResult.path("result");
            if (boxexs.size() == 0) {
                continue;
            }
            BigDecimal newPaoW = BigDecimal.ZERO;
            Set<String> boxNos = new HashSet<>();
            for (JsonNode boxex : boxexs) {
                final String boxNo = boxex.path("box_no").asText();
                if (boxNos.contains(boxNo)) {
                    continue;
                } else {
                    boxNos.add(boxNo);
                }
                final String pao_weight = boxex.path("pao_weight").asText();
                final BigDecimal newV = getNewV(pao_weight);
                System.out.println(newPaoW.toString() + " + " + newV);
                newPaoW = newV.add(newV);
            }
            final BigDecimal bigDecimal = newPaoW.setScale(3, RoundingMode.HALF_UP);
            System.out.printf("UPDATE pcl_order set total_pao_weight = %s where id = %s;\n", bigDecimal.toString(), id);
            fileWriter.printf("UPDATE pcl_order set total_pao_weight = %s where id = %s;\n", bigDecimal.toString(), id);
            databaseConfig.put("sql", "SELECT total_pao_weight FROM pcl_order where id = " + id);
            final JsonNode orderPaoWeight = DatabaseUtils.query(databaseConfig, null);
            String originValue = orderPaoWeight.path("result").path(0).path("total_pao_weight").asText();
            originWriter.printf("UPDATE pcl_order set total_pao_weight = %s where id = %s;\n", originValue, id);
            if ((i) % 500 == 0) {
                fileWriter.flush();
                originWriter.flush();
            }
        }
        fileWriter.flush();
        originWriter.flush();
    }

    private static void handleBoxWeight(ObjectNode databaseConfig, ObjectMapper mapper, PrintStream fileWriter, PrintStream originWriter) {

        databaseConfig.put("sql", "SELECT id,pao_weight\n" +
                "FROM pcl_box\n" +
                "where pao_weight is not null");
        final ObjectNode objectNode = mapper.createObjectNode();
        final JsonNode query = DatabaseUtils.query(databaseConfig, objectNode);
        final JsonNode result = query.path("result");
        int i = 0;
        for (JsonNode jsonNode : result) {
            final String id = jsonNode.path("id").asText();
            final String paoWeight = jsonNode.path("pao_weight").asText();
            BigDecimal paoWeightValue = getNewV(paoWeight).setScale(3, RoundingMode.HALF_UP);;
            System.out.printf("UPDATE pcl_box set pao_weight = %s where id = %s;\n", paoWeightValue, id);
            fileWriter.printf("UPDATE pcl_box set pao_weight = %s where id = %s;\n", paoWeightValue, id);
            originWriter.printf("UPDATE pcl_box set pao_weight = %s where id = %s;\n", paoWeight, id);
            if ((i++) % 500 == 0) {
                originWriter.flush();
                fileWriter.flush();
            }
        }
        fileWriter.flush();
        originWriter.flush();
    }

    private static BigDecimal getNewV(String paoWeight) {
        if (StringUtils.isEmpty(paoWeight) || StringUtils.equals(paoWeight, "null")) {
            return BigDecimal.ZERO;
        }
        BigDecimal paoWeightValue = new BigDecimal(paoWeight);
        paoWeightValue = paoWeightValue.divide(V);
        return paoWeightValue;
    }

    private static ObjectNode getDatabaseConfig(ObjectMapper mapper) {
        final ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("url", "jdbc:mysql://172.29.15.88:3306/cfs?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai");
        objectNode.put("userName", "readonly");
        objectNode.put("password", "cFs123!@#");
        objectNode.put("driver", "com.mysql.jdbc.Driver");
        objectNode.put("param", mapper.createArrayNode());
        return objectNode;
    }


}
