package org.advancedproductivity.gable.framework.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.ConfigField;
import org.advancedproductivity.gable.framework.runner.HttpRunner;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

/**
 * @author zzq
 */
@Slf4j
public class WallTechAuth implements AuthHandler {
    private static final String TIME_DIFFERENCE = "timeDifference";
    private static final String TOKEN = "token";
    private static final String KEY = "key";
    private static final String DATE_HEADER = "X-WallTech-Date";
    private static final String AUTH_HEADER = "Authorization";
    private static final String MAC_NAME = "HmacSHA1";
    private static final String ENCODING = "UTF-8";
    private static SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ", Locale.ENGLISH);

    /**
     * param have timeDifference
     * param have token
     * param have key
     * param have url
     */
    @Override
    public void handle(JsonNode in, JsonNode instance, JsonNode global) {
        JsonNode http_auth_param = in.path(ConfigField.HTTP_AUTH).path(ConfigField.HTTP_AUTH_PARAM);
        String url = HttpRunner.parserToUrlPath(in);
        log.info("raise url: {}", url);
        long timeDifference = http_auth_param.path(TIME_DIFFERENCE).asLong();
        String date = format.format(new Date(System.currentTimeMillis() - (long) timeDifference * 60 * 60 * 1000)) + "GMT";
        String auth = getAuthorization(date, http_auth_param.path(TOKEN).asText(), http_auth_param.path(KEY).asText(), url);
        JsonNode headerNodes = in.path(ConfigField.HTTP_HEADER);
        if (!headerNodes.isArray()) {
            return;
        }
        ArrayNode headers = (ArrayNode) headerNodes;
        headers.add(headers.objectNode().put("key", DATE_HEADER).put("value", date).put("disabled", false));
        headers.add(headers.objectNode().put("key", AUTH_HEADER).put("value", auth).put("disabled", false));
    }

    @Override
    public AuthType getAuthType() {
        return AuthType.WALL_TECH;
    }

    String getAuthorization(String dateString, String token, String key, String url) {
        String signedString = "POST\n" + dateString + "\n" + url;
        String authorization = "";
        try {
            byte[] signSha1 = HmacSHA1Encrypt(signedString, key);
            String sign = Base64.getEncoder().encodeToString(signSha1);
            authorization = "WallTech " + token + ":" + sign;
        } catch (Exception e) {
            log.error("cal auth err: ", e);
        }
        return authorization;
    }


    private static byte[] HmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
        byte[] data = encryptKey.getBytes(ENCODING);
        // 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        // 生成一个指定 Mac 算法 的 Mac 对象
        Mac mac = Mac.getInstance(MAC_NAME);
        // 用给定密钥初始化 Mac 对象
        mac.init(secretKey);
        byte[] text = encryptText.getBytes(ENCODING);
        // 完成 Mac 操作
        return mac.doFinal(text);
    }
}
