package org.advancedproductivity.gable.framework.runner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.advancedproductivity.gable.framework.auth.AuthHandler;
import org.advancedproductivity.gable.framework.auth.AuthHolder;
import org.advancedproductivity.gable.framework.config.ConfigField;
import org.advancedproductivity.gable.framework.config.HttpResponseField;
import org.advancedproductivity.gable.framework.core.HttpBodyType;
import org.advancedproductivity.gable.framework.core.HttpMethodType;
import org.advancedproductivity.gable.framework.core.TestType;
import org.apache.commons.lang3.StringUtils;
import kotlin.Pair;
import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;

/**
 * @author zzq
 */
@Slf4j
public class HttpRunner implements TestAction {
    private static final int DEFAULT_HTTP_PORT = 80;
    private static final ObjectMapper mapper = new ObjectMapper();
    @Override
    public void execute(JsonNode in, JsonNode out, ObjectNode instance, ObjectNode global) {
        // handle auth
        AuthHandler authHandler = AuthHolder.HOLDER.get(
                in.path(ConfigField.HTTP_AUTH)
                .path(ConfigField.HTTP_AUTH_TYPE).asText());
        if (authHandler != null) {
            authHandler.handle(in, instance, global);
        }
        ObjectNode response = (ObjectNode) out;
        OkHttpClient client = new OkHttpClient().newBuilder()
                .readTimeout(Duration.ZERO)
                .callTimeout(Duration.ZERO)
                .build();
        Request.Builder builder = new Request.Builder().url(parserToUrlPath(in));
        String method = in.path(ConfigField.HTTP_METHOD).asText();
        if (StringUtils.equals(method, HttpMethodType.GET.name())) {
            builder.method(method, null);
        } else if (StringUtils.equals(method, HttpMethodType.DELETE.name())) {
            builder.method(method, null);
        } else if (StringUtils.equals(method, HttpMethodType.POST.name())) {
            RequestBody body = parserHttpBody(in.path(ConfigField.HTTP_BODY));
            if (body == null) {
                response.put("error", "unknown http body" + in.path(ConfigField.HTTP_BODY_TYPE).asText());
                return;
            }
            builder.method(method, body);
        } else if (StringUtils.equals(method, HttpMethodType.PUT.name())) {
            RequestBody body = parserHttpBody(in.path(ConfigField.HTTP_BODY));
            if (body == null) {
                response.put("error", "unknown http body" + in.path(ConfigField.HTTP_BODY_TYPE).asText());
                return;
            }
            builder.method(method, body);
        } else {
            response.put("error", "unknown http method" + method);
            return;
        }
        setRequestHeader(in, builder);
        try {
            Response res = client.newCall(builder.build()).execute();
            long startAt = res.sentRequestAtMillis();
            long endAt = res.receivedResponseAtMillis();
            response.put(HttpResponseField.CODE, res.code());
            response.put(HttpResponseField.MESSAGE, res.message());
            response.put(HttpResponseField.START_AT, startAt);
            response.put(HttpResponseField.END_AT, endAt);
            response.put(HttpResponseField.TIME_TAKES, (endAt - startAt));
            ResponseBody body = res.body();
            String bodyType = body.contentType().toString();
            response.put(HttpResponseField.CONTENT_TYPE, bodyType);
            if (StringUtils.contains(bodyType, "json")) {
                response.set(HttpResponseField.CONTENT, mapper.readTree(body.string()));
            }else {
                response.put(HttpResponseField.CONTENT, body.string());
            }
            response.put(HttpResponseField.SIZE, body.contentLength());
            handleHeaders(res.headers(), response);
        } catch (IOException e) {
            log.error("error happens while execute http request", e);
            response.put("error", e.getMessage());
        }
    }

    private void handleHeaders(Headers headers, ObjectNode response) {
        Iterator<Pair<String, String>> iterator = headers.iterator();
        HttpBodyType bodyType = HttpBodyType.TEXT;
        ArrayNode arrayNode = response.arrayNode();
        while (iterator.hasNext()) {
            Pair<String, String> next = iterator.next();
            ObjectNode content = response.objectNode();
            String key = next.getFirst();
            String value = next.getSecond();
            if (StringUtils.equalsIgnoreCase("cookie", key)) {
                setCookie(response, value);
            }
            content.put("key", key);
            content.put("value", value);
            arrayNode.add(content);
            if (StringUtils.equalsIgnoreCase(key, "content-type")) {
                if (StringUtils.contains(value, "application/json")) {
                    bodyType = HttpBodyType.JSON;
                } else if (StringUtils.contains(value, "html")) {
                    bodyType = HttpBodyType.HTML;
                } else if (StringUtils.contains(value, "xml")) {
                    bodyType = HttpBodyType.XML;
                }
            }
        }
        log.info("find content type: {}", bodyType.toString());
        response.set(HttpResponseField.HEADERS, arrayNode);
    }


    private RequestBody parserHttpBody(JsonNode in) {
        String bodyType = in.path(ConfigField.HTTP_BODY_TYPE).asText();
        if (StringUtils.equals(bodyType, HttpBodyType.NONE.name())) {
            return RequestBody.create(MediaType.parse("text/plain"), "");
        } else if (StringUtils.equals(bodyType, HttpBodyType.JSON.name())) {
            MediaType mediaType = MediaType.parse("application/json");
            JsonNode content = in.path(ConfigField.HTTP_BODY_CONTENT);
            if (content.isTextual()) {
                return RequestBody.create(mediaType, content.asText());
            }else {
                return RequestBody.create(mediaType, content.toString());
            }
        } else if (StringUtils.equals(bodyType, HttpBodyType.TEXT.name())) {
            MediaType mediaType = MediaType.parse("text/plain");
            JsonNode content = in.path(ConfigField.HTTP_BODY_CONTENT);
            return RequestBody.create(mediaType, content.asText());
        } else if (StringUtils.equals(bodyType, HttpBodyType.FORM_DATA.name())) {
            MultipartBody.Builder formDataBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            JsonNode forms = in.path(ConfigField.HTTP_BODY_FORM_DATA);
            for (int i = 0; i < forms.size(); i++) {
                JsonNode item = forms.get(i);
                boolean disabled = item.path("disabled").asBoolean();
                if (!disabled) {
                    formDataBuilder.addFormDataPart(item.path("key").asText(),
                            item.path("value").asText());
                }
            }
            return formDataBuilder.build();
        } else if (StringUtils.equals(bodyType, HttpBodyType.URLENCODED.name())) {
            String urlEncodedContent = parserToUrlPath(in.path(ConfigField.URLENCIDED));
            return RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), urlEncodedContent);
        }
        return null;
    }



    private void setRequestHeader(JsonNode in, Request.Builder builder) {
        JsonNode path = in.path(ConfigField.HTTP_HEADER);
        for (int i = 0; i < path.size(); i++) {
            JsonNode item = path.get(i);
            boolean disabled = item.path("disabled").asBoolean();
            if (!disabled) {
                String key = item.path("key").asText();
                String value = item.path("value").asText();
                log.info("http add header: {} is {}", key, value);
                builder.addHeader(key, value);
            }
        }
    }

    public static String parserToUrlPath(JsonNode in) {
        String protocol = in.path(ConfigField.HTTP_PROTOCOL).asText();
        String host = in.path(ConfigField.HTTP_HOST).asText();
        int port = in.path(ConfigField.HTTP_PORT).asInt();
        String path = "";
        JsonNode paths = in.path(ConfigField.HTTP_PATH);

        for (JsonNode node : paths) {
            if (node.isTextual()) {
                path += "/" + node.asText();
            }
        }
        JsonNode queries = in.path(ConfigField.HTTP_QUERY);
        String queryStr = mapKeyValue(queries);
        if (!StringUtils.isEmpty(queryStr)) {
            queryStr = "?" + queryStr;
        }
        if (port == 0 || port == DEFAULT_HTTP_PORT) {
            return protocol + "://" + host + path + queryStr;
        }
        return protocol + "://" + host + ":" + port + path + queryStr;
    }

    private static String mapKeyValue(JsonNode queries) {
        int index = 0;
        String queryStr = "";
        for (int i = 0; i < queries.size(); i++) {
            JsonNode item = queries.get(i);
            if (!item.isObject()) {
                continue;
            }
            if (index != 0) {
                queryStr += "&";
            }
            queryStr += item.path("key").asText() + "=" + item.path("value").asText();
            index++;
        }
        return queryStr;
    }

    @Override
    public TestType getTestType() {
        return TestType.HTTP;
    }

    private void setCookie(ObjectNode response, String value) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        String[] split = StringUtils.split(value, ";");
        ArrayNode arrayNode = response.arrayNode();
        for (String s : split) {
            if (!StringUtils.contains(s, "=")) {
                continue;
            }
            String[] kv = StringUtils.trim(s).split("=");
            if (kv.length != 2) {
                continue;
            }
            ObjectNode content = response.objectNode();
            content.put("key", kv[0]);
            content.put("value", kv[1]);
            arrayNode.add(content);
        }
        response.set(HttpResponseField.COOKIE, arrayNode);
    }

}
