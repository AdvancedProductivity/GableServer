package org.advancedproductivity.gable.framework.utils;

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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author zzq
 */
@Slf4j
public class GableFileUtils {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Cache<String, JsonNode> CACHE = CacheBuilder.newBuilder()
            .maximumSize(1024)
            .removalListener(new RemovalListener<Object, Object>() {
                @Override
                public void onRemoval(RemovalNotification<Object, Object> notification) {
                    log.info("remove cache key: {}", notification.getKey().toString());
                }
            })
            .build();

    public static void main(String[] args) {
        ArrayNode arrayNode = mapper.createArrayNode();
        ObjectNode objectNode = mapper.createObjectNode().put("a", 10);
        arrayNode.add(objectNode);
        boolean a = cacheValue(arrayNode, "a");
        System.out.println(getCache("a").toPrettyString());
        objectNode.put("a", 8);
        System.out.println(getCache("a").toPrettyString());
    }

    public static boolean saveFile(final String content, final String... names) {
        try {
            File file = FileUtils.getFile(names);
            FileUtils.write(file, content, StandardCharsets.UTF_8);
            String key = buildKey(names);
            CACHE.invalidate(key);
            return true;
        } catch (IOException e) {
            log.error("error happens while write str to file: {}", Arrays.toString(names), e);
        }
        return false;
    }

    public static String readFileAsString(final String... names) {
        File file = FileUtils.getFile(names);
        if (!file.exists()) {
            return null;
        }
        try {
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("read file as string errors: {}", Arrays.toString(names), e);
            return null;
        }
    }

    public static JsonNode readFileAsJson(final String... names) {
        String key = buildKey(names);
        JsonNode r = CACHE.getIfPresent(key);
        if (r != null) {
            if (r.isNull()) {
                return null;
            }else {
                return r;
            }
        }
        File file = FileUtils.getFile(names);
        if (!file.exists()) {
            CACHE.put(key, NullNode.getInstance());
            return null;
        }
        try {
            JsonNode jsonNode = mapper.readTree(file);
            CACHE.put(key, jsonNode);
            return jsonNode;
        } catch (Exception e) {
            log.error("read file as json errors: {}", Arrays.toString(names), e);
            return null;
        }
    }

    private static String buildKey(String... names) {
        if (names == null) {
            return null;
        }
        return StringUtils.joinWith(File.separator, names);
    }

    public static boolean cacheValue(JsonNode waitForCache, String... keys) {
        if (waitForCache == null) {
            return false;
        }
        if (keys == null) {
            return false;
        }
        String key = buildKey(keys);
        CACHE.put(key, waitForCache);
        return true;
    }

    public static JsonNode getCache(String... keys) {
        String key = buildKey(keys);
        JsonNode r = CACHE.getIfPresent(key);
        if (r != null) {
            if (r.isNull()) {
                return null;
            }else {
                return r;
            }
        }
        return null;
    }

}
