package org.advancedproductivity.gable.framework.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;

/**
 * @author zzq
 */
@Slf4j
public class DatabaseUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String UPDATE = "UPDATE";
    private static final String DELETE = "DELETE";
    private static final String SELECT = "SELECT";
    private static final String INSERT = "INSERT";

    @Data
    private static class DatabaseHolder{
        Connection connection;
        String sql;
    }

    public static JsonNode query(JsonNode in, ObjectNode out) {
        if (out == null) {
            out = MAPPER.createObjectNode();
        }
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            DatabaseHolder holder = getConnection(in, SELECT);
            connection = holder.getConnection();
            String sqlWaitForExecute = holder.getSql();
            statement = connection.prepareStatement(sqlWaitForExecute);
            JsonNode param = in.path("param");
            setSqlParam(param, statement);
            resultSet = statement.executeQuery();
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode arr = objectMapper.createArrayNode();
            while (resultSet.next()) {
                ObjectNode tmpResponse = objectMapper.createObjectNode();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int j = 0; j < columnCount; j++) {
                    String columnName = metaData.getColumnName(j + 1);
                    Object columnValue = resultSet.getObject(columnName);
                    String value = columnValue == null ? "null" : columnValue.toString();
                    tmpResponse.put(columnName, value);
                }
                arr.add(tmpResponse);
            }
            out.set("result", arr);
        } catch (Exception e) {
            out.put("errors", e.getMessage());
        } finally {
            closeQuiet(connection, statement, resultSet);
        }
        return out;
    }

    public static JsonNode insert(JsonNode in, ObjectNode out){
        return executeOtherSql(in, out, INSERT);
    }

    public static JsonNode update(JsonNode in, ObjectNode out){
        return executeOtherSql(in, out, UPDATE);
    }

    public static JsonNode delete(JsonNode in, ObjectNode out){
        return executeOtherSql(in, out, DELETE);
    }


    private static JsonNode executeOtherSql(JsonNode in, ObjectNode out,String type) {
        if (out == null) {
            out = MAPPER.createObjectNode();
        }
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            DatabaseHolder holder = getConnection(in, type);
            connection = holder.getConnection();
            String sqlWaitForExecute = holder.getSql();
            statement = connection.prepareStatement(sqlWaitForExecute);
            JsonNode param = in.path("param");
            setSqlParam(param, statement);
            int count = statement.executeUpdate();
            out.put("count", count);
        } catch (Exception e) {
            out.put("errors", e.getMessage());
        } finally {
            closeQuiet(connection, statement, resultSet);
        }
        return out;
    }

    private static void setSqlParam(JsonNode param, PreparedStatement statement) throws SQLException {
        for (int i = 0; i < param.size(); i++) {
            JsonNode item = param.path(i);
            log.info("hand param: " + item.toString());
            if (item.isInt()) {
                statement.setInt((i + 1), item.asInt());
            } else if (item.isLong()) {
                statement.setLong((i + 1), item.asLong());
            } else if (item.isDouble()) {
                statement.setDouble((i + 1), item.asDouble());
            } else if (item.isTextual()) {
                statement.setString((i + 1), item.asText());
            } else {
                log.warn("do not know how to handle");
            }
        }
    }

    private static void closeQuiet(Connection connection, PreparedStatement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
                log.info("close resultSet");
            }
            if (statement != null) {
                statement.close();
                log.info("close statement");
            }
            if (connection != null) {
                connection.close();
                log.info("close connection");
            }
        } catch (Exception e) {
            log.error("close resource error", e);
        }
    }

    private static DatabaseHolder getConnection(JsonNode in,String type) throws Exception {
        String url = in.path("url").asText();
        if (StringUtils.isEmpty(url)) {
            throw new Exception("url can not empty");
        }
        String driver = in.path("driver").asText();
        if (StringUtils.isEmpty(driver)) {
            throw new Exception("driver can not empty");
        }
        String userName = in.path("userName").asText();
        if (StringUtils.isEmpty(userName)) {
            throw new Exception("userName can not empty");
        }
        String password = in.path("password").asText();
        if (StringUtils.isEmpty(userName)) {
            throw new Exception("password can not empty");
        }
        StringBuilder sqlBuilder = new StringBuilder();
        JsonNode sqlJson = in.path("sql");
        if (sqlJson.isTextual()) {
            sqlBuilder.append(sqlJson.asText());
        } else if (sqlJson.isArray()) {
            for (JsonNode jsonNode : sqlJson) {
                if (jsonNode.isTextual()) {
                    sqlBuilder.append(jsonNode.asText()).append(" ");
                }
            }
        }
        String sqlWaitForExecute = StringUtils.trim(sqlBuilder.toString());
        if (StringUtils.isEmpty(sqlWaitForExecute)) {
            throw new Exception("sql can not empty");
        }
        validateSqlType(sqlWaitForExecute, type);
        JsonNode param = in.path("param");
        if (!param.isArray()) {
            throw new Exception("param array myst exist");
        }
        Class.forName(driver);
        DatabaseHolder holder = new DatabaseHolder();
        holder.setSql(sqlWaitForExecute);
        holder.setConnection(DriverManager.getConnection(url, userName, password));
        return holder;
    }

    private static void validateSqlType(String sqlWaitForExecute, String supportType) throws Exception  {
        if (StringUtils.equals(supportType, SELECT)) {
            if (StringUtils.containsIgnoreCase(sqlWaitForExecute, UPDATE)) {
                throw new Exception( "can not execute update sql");
            }
            if (StringUtils.containsIgnoreCase(sqlWaitForExecute, DELETE)) {
                throw new Exception( "can not execute delete sql");
            }
            if (StringUtils.containsIgnoreCase(sqlWaitForExecute, INSERT)) {
                throw new Exception( "can not execute insert sql");
            }
            if (!StringUtils.containsIgnoreCase(sqlWaitForExecute, SELECT)) {
                throw new Exception("only execute select sql");
            }
        }else if (StringUtils.equals(supportType, UPDATE)) {
            if (!StringUtils.containsIgnoreCase(sqlWaitForExecute, UPDATE)) {
                throw new Exception( "only execute update sql");
            }
            if (StringUtils.containsIgnoreCase(sqlWaitForExecute, DELETE)) {
                throw new Exception( "can not execute delete sql");
            }
            if (StringUtils.containsIgnoreCase(sqlWaitForExecute, INSERT)) {
                throw new Exception( "can not execute insert sql");
            }
            if (StringUtils.containsIgnoreCase(sqlWaitForExecute, SELECT)) {
                throw new Exception("can not execute select sql");
            }
        }else if (StringUtils.equals(supportType, DELETE)) {
            if (StringUtils.containsIgnoreCase(sqlWaitForExecute, UPDATE)) {
                throw new Exception( "can not execute update sql");
            }
            if (!StringUtils.containsIgnoreCase(sqlWaitForExecute, DELETE)) {
                throw new Exception( "only execute delete sql");
            }
            if (StringUtils.containsIgnoreCase(sqlWaitForExecute, INSERT)) {
                throw new Exception( "can not execute insert sql");
            }
            if (StringUtils.containsIgnoreCase(sqlWaitForExecute, SELECT)) {
                throw new Exception("can not execute select sql");
            }
        }else if (StringUtils.equals(supportType, INSERT)) {
            if (StringUtils.containsIgnoreCase(sqlWaitForExecute, UPDATE)) {
                throw new Exception( "can not execute update sql");
            }
            if (StringUtils.containsIgnoreCase(sqlWaitForExecute, DELETE)) {
                throw new Exception( "can not execute delete sql");
            }
            if (!StringUtils.containsIgnoreCase(sqlWaitForExecute, INSERT)) {
                throw new Exception( "only execute insert sql");
            }
            if (StringUtils.containsIgnoreCase(sqlWaitForExecute, SELECT)) {
                throw new Exception("can not execute select sql");
            }
        }
    }
}
