import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang3.StringUtils

import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData

String url = in.path("url").asText()
if (StringUtils.isEmpty(url)) {
    out.put("errors", "url can not empty");
    return
}
String driver = in.path("driver").asText()
if (StringUtils.isEmpty(driver)) {
    out.put("errors", "driver can not empty");
    return
}
String userName = in.path("userName").asText()
if (StringUtils.isEmpty(userName)) {
    out.put("errors", "userName can not empty");
    return
}
String password = in.path("password").asText()
if (StringUtils.isEmpty(userName)) {
    out.put("errors", "password can not empty");
    return
}
String sqlWaitForExecute = in.path("sql").asText()
if (StringUtils.isEmpty(sqlWaitForExecute)) {
    out.put("errors", "sql can not empty");
    return
}
if (StringUtils.containsIgnoreCase(sqlWaitForExecute, "UPDATE")) {
    out.put("errors", "can not execute update sql");
    return
}
if (StringUtils.containsIgnoreCase(sqlWaitForExecute, "SELECT")) {
    out.put("errors", "can not execute select sql");
    return
}
if (!StringUtils.containsIgnoreCase(sqlWaitForExecute, "DELETE")) {
    out.put("errors", "only execute delete sql");
    return
}
JsonNode param = in.path("param")
if (!param.isArray()) {
    out.put("errors", "param array myst exist");
    return
}
Class.forName(driver);
connection = null
PreparedStatement statement = null
try {
    connection = DriverManager.getConnection(url, userName, password);
    statement = connection.prepareStatement(sqlWaitForExecute);
    for (i in 0..<param.size()) {
        JsonNode item = param.path(i)
        println("hand param: " + item.toString())
        if (item.isInt()) {
            statement.setInt((i + 1), item.asInt())
        } else if (item.isLong()) {
            statement.setLong((i + 1), item.asLong())
        } else if (item.isDouble()) {
            statement.setDouble((i + 1), item.asDouble())
        } else if (item.isTextual()) {
            statement.setString((i + 1), item.asText())
        } else {
            println("do not know how to handle")
        }
    }
    int count = statement.executeUpdate();
    out.put("count", count);
} catch (Exception e) {
    out.put("errors", e.getMessage());
    e.printStackTrace()
} finally {
    if (statement != null) {
        statement.close();
        println("close statement")
    }
    if (connection != null) {
        connection.close();
        println("close connection")
    }
}