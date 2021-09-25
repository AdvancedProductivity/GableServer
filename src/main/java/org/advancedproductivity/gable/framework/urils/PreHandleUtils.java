package org.advancedproductivity.gable.framework.urils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zzq
 */
@Slf4j
public class PreHandleUtils {
    public static Pattern ASSERT_JSON_PATTERN = Pattern.compile("\\((.*?)\\)");

    public static void main(String[] args) {
        String s1 = "{\n" +
                "    \"/body/content/0/addressLine1\": \"{{random(letter,10)}}\"\n" +
                "}";
        s1 = StringUtils.remove(s1, " ");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode node = objectMapper.readTree(s1);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static void preHandleInJson(JsonNode in, ObjectNode instance, ObjectNode global) {
        if (in.isArray()) {
            preHandleAsArray((ArrayNode) in, instance, global);
        } else if (in.isObject()) {
            preHandleAsObject((ObjectNode) in, instance, global);
        }else {
            log.info("ignore handle type: {}", in.getNodeType());
        }
    }

    private static void preHandleAsObject(ObjectNode in, ObjectNode instance, ObjectNode global) {
        Iterator<String> fieldNames = in.fieldNames();
        while (fieldNames.hasNext()) {
            String field = fieldNames.next();
            JsonNode item = in.path(field);
            if (item.isObject()) {
                preHandleAsObject((ObjectNode) item, instance, global);
            } else if (item.isArray()) {
                preHandleAsArray((ArrayNode) item, instance, global);
            } else if (item.isTextual()) {
                String originValue = item.textValue();
                Object newValue = preHandle(originValue, instance, global);
                if (newValue instanceof String) {
                    in.put(field, (String) newValue);
                }else if (newValue instanceof Integer) {
                    in.put(field, (int) newValue);
                }else if (newValue instanceof Long) {
                    in.put(field, (long) newValue);
                }
            }
        }
    }

    private static void preHandleAsArray(ArrayNode in, ObjectNode instance, ObjectNode global) {
        for (int i = 0; i < in.size(); i++) {
            JsonNode item = in.path(i);
            if (item.isObject()) {
                preHandleAsObject((ObjectNode) item, instance, global);
            } else if (item.isArray()) {
                preHandleAsArray((ArrayNode) item, instance, global);
            } else if (item.isTextual()) {
                String originValue = item.textValue();
                Object newValue = preHandle(originValue, instance, global);
                if (newValue instanceof String) {
                    in.set(i, TextNode.valueOf((String) newValue));
                }else if (newValue instanceof Integer) {
                    in.set(i, IntNode.valueOf((int) newValue));
                }else if (newValue instanceof Long) {
                    in.set(i, LongNode.valueOf((long) newValue));
                }
            }
        }
    }

    private static String preHandle(String var, ObjectNode instance, ObjectNode global) {
        var = var.trim();
        if (!StringUtils.startsWith(var, "{{")) {
            return var;
        }
        if (!StringUtils.endsWith(var, "}}")) {
            return var;
        }
        var = StringUtils.substringBetween(var, "{{", "}}");
        StringBuilder stringBuilder = new StringBuilder();
        String[] split = var.split("\\+");
        for (String s : split) {
            stringBuilder.append(doPreHandle(s, instance, global));
        }
        return stringBuilder.toString();
    }

    private static String doPreHandle(String var, ObjectNode instance, ObjectNode global){
        String originVar = var;
        var = var.trim();
        Matcher matcher = ASSERT_JSON_PATTERN.matcher(var);
        if (StringUtils.startsWith(var, "dateTime")) {
            if (matcher.find()) {
                String format = matcher.group().replace("(", "").replace(")", "");
                format = StringUtils.trim(format);
                if (StringUtils.equals(format, "timestamp")) {
                    return System.currentTimeMillis() + "";
                }
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
                    return simpleDateFormat.format(new Date());
                } catch (Exception e) {
                    e.printStackTrace();
                    return format;
                }
            }
        }else if (StringUtils.startsWith(var, "instance")) {
            if (matcher.find()) {
                String r = matcher.group().replace("(", "").replace(")", "");
                if (!StringUtils.startsWith(r, "/")) {
                    return originVar;
                }
                JsonNode at = instance.at(r);
                if (!at.isMissingNode()) {
                    return at.asText();
                }
            }
        } else if (StringUtils.startsWith(var, "global")) {
            if (matcher.find()) {
                String r = matcher.group().replace("(", "").replace(")", "");
                if (!StringUtils.startsWith(r, "/")) {
                    return originVar;
                }
                JsonNode at = global.at(r);
                if (!at.isMissingNode()) {
                    return at.asText();
                }
            }
        }else if (StringUtils.startsWith(var, "static")) {
            if (matcher.find()) {
                return matcher.group().replace("(", "").replace(")", "");
            }
        }else if (StringUtils.startsWith(var, "random")) {
            if (matcher.find()) {
                String c = matcher.group().replace("(", "").replace(")", "");
                String[] args = c.split(",");
                for (int i = 0; i < args.length; i++) {
                    args[i] = StringUtils.trim(args[i]);
                }
                if (StringUtils.equals(args[0], "boolean")) {
                    return String.valueOf(RandomUtils.nextBoolean());
                } else if (StringUtils.isNumeric(args[1]) && StringUtils.length(args[1]) < 4) {
                    Integer firstCount = Integer.parseInt(args[1]);
                    if (StringUtils.equals(args[0], "string")) {
                        return RandomStringUtils.random(firstCount, args[2]);
                    } else if (StringUtils.equals(args[0], "letter")) {
                        return RandomStringUtils.random(firstCount, true, false);
                    } else if (StringUtils.equals(args[0], "numberStr")) {
                        return RandomStringUtils.random(firstCount, false, true);
                    } else if (StringUtils.equals(args[0], "letterAndNumber")) {
                        return RandomStringUtils.random(firstCount, true, true);
                    } else {
                        if (StringUtils.isNumeric(args[2])) {
                            Integer secondNumber = Integer.parseInt(args[2]);
                            if (StringUtils.equals(args[0], "int")) {
                                return String.valueOf(RandomUtils.nextInt(firstCount, secondNumber));
                            } else if (StringUtils.equals(args[0], "double")) {
                                return String.valueOf(RandomUtils.nextDouble(firstCount.doubleValue(), secondNumber.doubleValue()));
                            } else if (StringUtils.equals(args[0], "float")) {
                                return String.valueOf(RandomUtils.nextFloat(firstCount.floatValue(), secondNumber.floatValue()));
                            } else if (StringUtils.equals(args[0], "long")) {
                                return String.valueOf(RandomUtils.nextLong(firstCount, secondNumber));
                            }
                        }
                    }
                }
            }
        }
        return originVar;
    }
}
